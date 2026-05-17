-- Bild-Hints: add image_url column to hints table and create the storage bucket.
-- Run this in the Supabase SQL editor.

ALTER TABLE public.hints
    ADD COLUMN IF NOT EXISTS image_url text;

-- Storage bucket setup (run once via Supabase Dashboard or API):
--   1. Go to Storage > New bucket
--   2. Name: hint-images
--   3. Public: true  (so images are served without auth tokens)
--
-- Or via SQL:
INSERT INTO storage.buckets (id, name, public)
VALUES ('hint-images', 'hint-images', true)
ON CONFLICT (id) DO NOTHING;

-- Allow public read on hint-images (no RLS needed for public bucket)
-- Allow authenticated + anon insert/update so the app can upload
CREATE POLICY IF NOT EXISTS "hint-images public read"
    ON storage.objects FOR SELECT USING (bucket_id = 'hint-images');

CREATE POLICY IF NOT EXISTS "hint-images upload"
    ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'hint-images');
