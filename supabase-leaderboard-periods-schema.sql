-- ============================================================================
-- Leaderboard by period: all-time, weekly, monthly
-- ============================================================================
-- Run this in the Supabase SQL editor.
-- Requires: user_profiles, adventure_attempts, users tables.
-- ============================================================================

CREATE OR REPLACE FUNCTION public.get_leaderboard_by_period(
    p_period TEXT DEFAULT 'all',
    p_limit  INT  DEFAULT 50
)
RETURNS TABLE(display_name TEXT, points BIGINT)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF p_period = 'week' THEN
        RETURN QUERY
            SELECT
                COALESCE(u.username, u.email)::TEXT          AS display_name,
                COALESCE(SUM(aa.points_earned), 0)::BIGINT   AS points
            FROM adventure_attempts aa
            JOIN users u ON u.id = aa.user_id
            WHERE aa.completed_at >= now() - interval '7 days'
              AND aa.is_completed = true
            GROUP BY u.id, u.username, u.email
            ORDER BY points DESC
            LIMIT p_limit;

    ELSIF p_period = 'month' THEN
        RETURN QUERY
            SELECT
                COALESCE(u.username, u.email)::TEXT          AS display_name,
                COALESCE(SUM(aa.points_earned), 0)::BIGINT   AS points
            FROM adventure_attempts aa
            JOIN users u ON u.id = aa.user_id
            WHERE aa.completed_at >= now() - interval '30 days'
              AND aa.is_completed = true
            GROUP BY u.id, u.username, u.email
            ORDER BY points DESC
            LIMIT p_limit;

    ELSE -- 'all'
        RETURN QUERY
            SELECT
                COALESCE(u.username, u.email)::TEXT          AS display_name,
                COALESCE(up.total_points, 0)::BIGINT         AS points
            FROM users u
            LEFT JOIN user_profiles up ON up.user_id = u.id
            ORDER BY points DESC
            LIMIT p_limit;
    END IF;
END;
$$;

-- Grant execute to the anon role used by postgrest
GRANT EXECUTE ON FUNCTION public.get_leaderboard_by_period(TEXT, INT) TO anon;
GRANT EXECUTE ON FUNCTION public.get_leaderboard_by_period(TEXT, INT) TO authenticated;
