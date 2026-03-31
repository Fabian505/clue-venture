-- ClueVenture – Supabase database schema
-- Enable PostGIS for geographic queries
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================
-- Tables
-- ============================================================

-- Adventures: top-level scavenger hunt definitions
CREATE TABLE IF NOT EXISTS public.adventures (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            TEXT        NOT NULL,
    description      TEXT        NOT NULL DEFAULT '',
    is_active        BOOLEAN     NOT NULL DEFAULT true,
    thumbnail_url    TEXT,
    difficulty_level TEXT        NOT NULL DEFAULT 'medium'
                     CHECK (difficulty_level IN ('easy', 'medium', 'hard')),
    waypoint_count   INTEGER     NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Waypoints: geographic locations within an adventure
CREATE TABLE IF NOT EXISTS public.waypoints (
    id            UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    adventure_id  UUID    NOT NULL REFERENCES public.adventures (id) ON DELETE CASCADE,
    title         TEXT    NOT NULL,
    description   TEXT    NOT NULL DEFAULT '',
    latitude      DOUBLE PRECISION NOT NULL,
    longitude     DOUBLE PRECISION NOT NULL,
    -- PostGIS geography column for efficient spatial queries
    location      GEOGRAPHY(POINT, 4326) GENERATED ALWAYS AS (
        ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
    ) STORED,
    order_index   INTEGER NOT NULL DEFAULT 0,
    radius_meters DOUBLE PRECISION NOT NULL DEFAULT 50.0,
    clue_text     TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Questions: quiz questions attached to each waypoint
CREATE TABLE IF NOT EXISTS public.questions (
    id             UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    waypoint_id    UUID    NOT NULL REFERENCES public.waypoints (id) ON DELETE CASCADE,
    text           TEXT    NOT NULL,
    correct_answer TEXT    NOT NULL,
    options        TEXT[]  NOT NULL DEFAULT '{}',
    hint_text      TEXT,
    points_value   INTEGER NOT NULL DEFAULT 10,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- User progress: tracks each user's journey through an adventure
CREATE TABLE IF NOT EXISTS public.user_progress (
    id                     UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                UUID    NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    adventure_id           UUID    NOT NULL REFERENCES public.adventures (id) ON DELETE CASCADE,
    current_waypoint_index INTEGER NOT NULL DEFAULT 0,
    score                  INTEGER NOT NULL DEFAULT 0,
    is_completed           BOOLEAN NOT NULL DEFAULT false,
    completed_waypoints    UUID[]  NOT NULL DEFAULT '{}',
    started_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at           TIMESTAMPTZ,
    UNIQUE (user_id, adventure_id)
);

-- ============================================================
-- Indexes
-- ============================================================

-- Spatial index on waypoint locations for fast geo-queries
CREATE INDEX IF NOT EXISTS waypoints_location_idx
    ON public.waypoints USING GIST (location);

-- Index for quickly fetching waypoints in order
CREATE INDEX IF NOT EXISTS waypoints_adventure_order_idx
    ON public.waypoints (adventure_id, order_index);

-- Index for user progress lookups
CREATE INDEX IF NOT EXISTS user_progress_user_adventure_idx
    ON public.user_progress (user_id, adventure_id);

-- ============================================================
-- Functions & Triggers
-- ============================================================

-- Keep waypoint_count in sync with the actual number of waypoints
CREATE OR REPLACE FUNCTION public.sync_waypoint_count()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    UPDATE public.adventures
    SET waypoint_count = (
        SELECT COUNT(*) FROM public.waypoints WHERE adventure_id = COALESCE(NEW.adventure_id, OLD.adventure_id)
    ),
    updated_at = now()
    WHERE id = COALESCE(NEW.adventure_id, OLD.adventure_id);
    RETURN NULL;
END;
$$;

CREATE OR REPLACE TRIGGER trg_sync_waypoint_count
    AFTER INSERT OR UPDATE OR DELETE ON public.waypoints
    FOR EACH ROW EXECUTE FUNCTION public.sync_waypoint_count();

-- Keep updated_at timestamp current on adventures
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER trg_adventures_updated_at
    BEFORE UPDATE ON public.adventures
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- PostGIS helper: find waypoints within a given radius (metres)
-- ============================================================

CREATE OR REPLACE FUNCTION public.waypoints_near(
    p_lat    DOUBLE PRECISION,
    p_lon    DOUBLE PRECISION,
    p_radius DOUBLE PRECISION DEFAULT 500.0  -- metres
)
RETURNS SETOF public.waypoints
LANGUAGE sql STABLE AS $$
    SELECT *
    FROM   public.waypoints
    WHERE  ST_DWithin(
               location,
               ST_SetSRID(ST_MakePoint(p_lon, p_lat), 4326)::geography,
               p_radius
           )
    ORDER BY ST_Distance(
        location,
        ST_SetSRID(ST_MakePoint(p_lon, p_lat), 4326)::geography
    );
$$;

-- ============================================================
-- Row-Level Security (RLS)
-- ============================================================

ALTER TABLE public.adventures    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.waypoints     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.questions     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_progress ENABLE ROW LEVEL SECURITY;

-- Adventures: readable by everyone, writable only by service role
CREATE POLICY adventures_select ON public.adventures
    FOR SELECT USING (true);

-- Waypoints: readable by everyone
CREATE POLICY waypoints_select ON public.waypoints
    FOR SELECT USING (true);

-- Questions: readable by everyone
CREATE POLICY questions_select ON public.questions
    FOR SELECT USING (true);

-- User progress: each user can only see and modify their own rows
CREATE POLICY user_progress_select ON public.user_progress
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY user_progress_insert ON public.user_progress
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY user_progress_update ON public.user_progress
    FOR UPDATE USING (auth.uid() = user_id);
