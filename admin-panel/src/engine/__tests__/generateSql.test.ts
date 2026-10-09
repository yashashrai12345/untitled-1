import { describe, it, expect } from 'vitest';
import fs from 'fs';
import path from 'path';
import { generate100VerifiedLevels } from '../../lib/batchLevelGenerator';

describe('Generate SQL Migration with 100 Verified Levels', () => {
  it('Generates and writes 100 verified solvable levels into supabase_schema.sql', () => {
    const levels = generate100VerifiedLevels();
    expect(levels.length).toBe(100);

    let sql = `-- Supabase SQL Schema for Arrows Puzzle Escape Admin Pattern Designer
-- Contains complete 100-level custom collection (IDs 1001 to 1100)

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

CREATE INDEX IF NOT EXISTS idx_custom_patterns_status ON public.custom_patterns (status);
CREATE INDEX IF NOT EXISTS idx_custom_patterns_published_at ON public.custom_patterns (published_at DESC);

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

-- Reset previous published custom levels cleanly
DELETE FROM public.custom_patterns WHERE status = 'published';

-- Insert 100 Verified Solvable Custom Levels
`;

    levels.forEach((lvl) => {
      const jsonStr = JSON.stringify(lvl).replace(/'/g, "''");
      sql += `INSERT INTO public.custom_patterns (name, description, width, height, difficulty, status, level_data, published_at)
VALUES ('${lvl.name.replace(/'/g, "''")}', 'Handcrafted level ${lvl.id} created via Admin Pattern Designer', ${lvl.board.width}, ${lvl.board.height}, ${lvl.difficulty}, 'published', '${jsonStr}'::jsonb, now());\n\n`;
    });

    const outputPath = path.join(__dirname, '..', '..', '..', '..', 'supabase_schema.sql');
    fs.writeFileSync(outputPath, sql, 'utf8');

    expect(fs.existsSync(outputPath)).toBe(true);
  });
});
