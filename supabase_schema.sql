-- Supabase SQL Schema for Arrows Puzzle Escape Admin Pattern Designer
-- Creates custom_patterns table, configures RLS, and clears published custom level feed.

CREATE TABLE IF NOT EXISTS public.custom_patterns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numeric_id SERIAL UNIQUE,
    name TEXT NOT NULL,
    description TEXT DEFAULT '',
    width INTEGER NOT NULL DEFAULT 8,
    height INTEGER NOT NULL DEFAULT 8,
    level_data JSONB NOT NULL,
    difficulty INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'published')),
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    published_at TIMESTAMPTZ
);

-- Index for fast lookup
CREATE INDEX IF NOT EXISTS idx_custom_patterns_status ON public.custom_patterns (status);
CREATE INDEX IF NOT EXISTS idx_custom_patterns_published_at ON public.custom_patterns (published_at DESC);

-- Row Level Security Policies
ALTER TABLE public.custom_patterns ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE policyname = 'Allow public read access to published patterns'
    ) THEN
        CREATE POLICY "Allow public read access to published patterns"
        ON public.custom_patterns
        FOR SELECT
        USING (status = 'published');
    END IF;
END $$;

-- Enable Realtime
ALTER PUBLICATION supabase_realtime ADD TABLE public.custom_patterns;

-- Clear all pre-populated levels (User will draw and publish custom patterns via Web Admin Panel)
TRUNCATE TABLE public.custom_patterns;
