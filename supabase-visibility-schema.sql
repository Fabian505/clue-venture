-- Visibility: public/private flag and ownership for adventures.
-- Run this in the Supabase SQL editor on any environment that doesn't have these columns yet.

ALTER TABLE public.adventures
    ADD COLUMN IF NOT EXISTS is_public boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS created_by text;

-- All adventures that existed before this migration have no owner and should
-- remain publicly visible so nothing disappears for existing users.
UPDATE public.adventures
SET is_public = true
WHERE is_public = false;

CREATE INDEX IF NOT EXISTS adventures_created_by_idx
    ON public.adventures(created_by);

CREATE INDEX IF NOT EXISTS adventures_is_public_idx
    ON public.adventures(is_public)
    WHERE is_public = true;
