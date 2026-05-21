-- ============================================================================
-- Trigger: set completed_at server-side when is_completed changes
-- ============================================================================
-- Run this in the Supabase SQL editor.
-- Ensures completed_at is always set by the server clock, never the device.
-- ============================================================================

CREATE OR REPLACE FUNCTION public.set_adventure_attempt_completed_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_completed IS DISTINCT FROM OLD.is_completed THEN
        NEW.completed_at = now();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS adventure_attempts_completed_at_trigger ON public.adventure_attempts;

CREATE TRIGGER adventure_attempts_completed_at_trigger
BEFORE UPDATE ON public.adventure_attempts
FOR EACH ROW
EXECUTE FUNCTION public.set_adventure_attempt_completed_at();
