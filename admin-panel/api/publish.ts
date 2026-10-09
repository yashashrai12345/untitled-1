import type { VercelRequest, VercelResponse } from '@vercel/node';
import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = process.env.VITE_SUPABASE_URL || process.env.SUPABASE_URL || '';
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.VITE_SUPABASE_ANON_KEY || '';
const ADMIN_PUBLISH_SECRET = process.env.ADMIN_PUBLISH_SECRET;

const supabase = SUPABASE_URL && SUPABASE_SERVICE_ROLE_KEY
  ? createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)
  : null;

export default async function handler(req: VercelRequest, res: VercelResponse) {
  // CORS Headers
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,POST');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, X-Admin-Secret');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  const requestSecret = req.headers['x-admin-secret'];

  // Verify ADMIN_PUBLISH_SECRET if configured
  if (ADMIN_PUBLISH_SECRET && ADMIN_PUBLISH_SECRET.trim() !== '') {
    if (requestSecret !== ADMIN_PUBLISH_SECRET) {
      return res.status(401).json({ success: false, message: 'Invalid admin publishing secret' });
    }
  }

  if (!supabase) {
    return res.status(503).json({ success: false, message: 'Database client not connected' });
  }

  const { name, description, width, height, level_data, difficulty, status } = req.body || {};

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
      return res.status(500).json({ success: false, message: error.message });
    }

    return res.status(200).json({
      success: true,
      data: data ? data[0] : null,
      message: 'Pattern published successfully',
    });
  } catch (err: any) {
    return res.status(500).json({ success: false, message: err.message });
  }
}
