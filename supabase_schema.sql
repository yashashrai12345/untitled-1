-- Supabase SQL Schema for Arrows Puzzle Escape Admin Pattern Designer
-- Execute this script in your Supabase SQL Editor (https://supabase.com/dashboard/project/_/sql)

-- 1. Create the custom_patterns table
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

-- 2. Create index on status for fast lookup by game clients
CREATE INDEX IF NOT EXISTS idx_custom_patterns_status ON public.custom_patterns (status);
CREATE INDEX IF NOT EXISTS idx_custom_patterns_published_at ON public.custom_patterns (published_at DESC);

-- 3. Enable Row Level Security (RLS)
ALTER TABLE public.custom_patterns ENABLE ROW LEVEL SECURITY;

-- 4. Create RLS Policies
-- Policy 1: Everyone (anon) can view published patterns
CREATE POLICY "Allow public read access to published patterns"
ON public.custom_patterns
FOR SELECT
USING (status = 'published');

-- Policy 2: Allow full access when using service_role key or server operations
-- (Service role key automatically bypasses RLS in Supabase)

-- 5. Enable Realtime on custom_patterns table
ALTER PUBLICATION supabase_realtime ADD TABLE public.custom_patterns;

-- 6. Insert sample pre-validated custom patterns
INSERT INTO public.custom_patterns (name, description, width, height, difficulty, status, level_data, published_at)
VALUES
(
  'Spiral Escape',
  'A handcrafted spiral pattern designed via Admin Designer',
  8,
  8,
  2,
  'published',
  '{
    "id": 1001,
    "name": "Spiral Escape",
    "board": {"width": 8, "height": 8},
    "arrows": [
      {
        "id": "arrow_1",
        "direction": "RIGHT",
        "points": [{"x": 1, "y": 1}, {"x": 6, "y": 1}]
      },
      {
        "id": "arrow_2",
        "direction": "DOWN",
        "points": [{"x": 6, "y": 2}, {"x": 6, "y": 6}]
      },
      {
        "id": "arrow_3",
        "direction": "LEFT",
        "points": [{"x": 5, "y": 6}, {"x": 1, "y": 6}]
      },
      {
        "id": "arrow_4",
        "direction": "UP",
        "points": [{"x": 1, "y": 5}, {"x": 1, "y": 2}]
      }
    ],
    "difficulty": 2,
    "parMoves": 4,
    "patternType": "CUSTOM",
    "seed": 1001,
    "difficultyScore": 2.0
  }'::jsonb,
  now()
)
ON CONFLICT DO NOTHING;
