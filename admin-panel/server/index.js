import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import cors from 'cors';
import { createClient } from '@supabase/supabase-js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(express.json({ limit: '10mb' }));

const SUPABASE_URL = process.env.VITE_SUPABASE_URL || process.env.SUPABASE_URL;
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.VITE_SUPABASE_ANON_KEY;
const ADMIN_PUBLISH_SECRET = process.env.ADMIN_PUBLISH_SECRET;

const supabase = SUPABASE_URL && SUPABASE_SERVICE_ROLE_KEY
  ? createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)
  : null;

// Public Endpoint for Android App & Web Client
app.get('/api/public/patterns', async (req, res) => {
  if (!supabase) {
    return res.status(503).json({ error: 'Supabase client not configured on server' });
  }

  try {
    const { data, error } = await supabase
      .from('custom_patterns')
      .select('*')
      .eq('status', 'published')
      .order('published_at', { ascending: false });

    if (error) {
      console.error('Supabase fetch error:', error);
      return res.status(500).json({ error: error.message });
    }

    res.json(data || []);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Secure Publishing Endpoint
app.post('/api/publish', async (req, res) => {
  const requestSecret = req.headers['x-admin-secret'];

  // Check admin publish secret if configured on server
  if (ADMIN_PUBLISH_SECRET && ADMIN_PUBLISH_SECRET.trim() !== '') {
    if (requestSecret !== ADMIN_PUBLISH_SECRET) {
      return res.status(401).json({ success: false, message: 'Invalid admin publishing secret' });
    }
  }

  if (!supabase) {
    return res.status(503).json({ success: false, message: 'Database client not connected' });
  }

  const { name, description, width, height, level_data, difficulty, status } = req.body;

  if (!level_data || !level_data.arrows || level_data.arrows.length === 0) {
    return res.status(400).json({ success: false, message: 'Invalid pattern: level_data contains no arrows' });
  }

  try {
    const payload = {
      name: name || level_data.name || 'Untitled Pattern',
      description: description || '',
      width: width || level_data.board?.width || 8,
      height: height || level_data.board?.height || 8,
      level_data: level_data,
      difficulty: difficulty || level_data.difficulty || 2,
      status: status || 'published',
      version: 1,
      published_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
    };

    const { data, error } = await supabase
      .from('custom_patterns')
      .upsert(payload)
      .select();

    if (error) {
      console.error('Error inserting into Supabase:', error);
      return res.status(500).json({ success: false, message: error.message });
    }

    res.json({ success: true, data: data ? data[0] : null, message: 'Pattern published successfully' });
  } catch (err) {
    console.error('Publish handler exception:', err);
    res.status(500).json({ success: false, message: err.message });
  }
});

// Serve Static React Build Files in Production
const distPath = path.join(__dirname, '..', 'dist');
app.use(express.static(distPath));

app.get('*', (req, res) => {
  res.sendFile(path.join(distPath, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`Arrows Admin Designer Server running on port ${PORT}`);
});
