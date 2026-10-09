import { describe, it, expect } from 'vitest';
import fs from 'fs';
import path from 'path';
import { generate100VerifiedLevels } from '../../lib/batchLevelGenerator';

describe('Generate Optional Sample SQL Migration with 100 Verified Levels', () => {
  it('Generates 100 verified solvable levels into sample_100_levels.sql', () => {
    const levels = generate100VerifiedLevels();
    expect(levels.length).toBe(100);

    let sql = `-- Sample SQL with 100 Verified Solvable Levels\n`;

    levels.forEach((lvl) => {
      const jsonStr = JSON.stringify(lvl).replace(/'/g, "''");
      sql += `INSERT INTO public.custom_patterns (name, description, width, height, difficulty, status, level_data, published_at)
VALUES ('${lvl.name.replace(/'/g, "''")}', 'Handcrafted level ${lvl.id}', ${lvl.board.width}, ${lvl.board.height}, ${lvl.difficulty}, 'published', '${jsonStr}'::jsonb, now());\n\n`;
    });

    const outputPath = path.join(__dirname, '..', '..', 'sample_100_levels.sql');
    fs.writeFileSync(outputPath, sql, 'utf8');

    expect(fs.existsSync(outputPath)).toBe(true);
  });
});
