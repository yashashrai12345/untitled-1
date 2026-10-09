import type { VercelRequest, VercelResponse } from '@vercel/node';
import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = process.env.VITE_SUPABASE_URL || process.env.SUPABASE_URL || '';
const SUPABASE_SERVICE_ROLE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.VITE_SUPABASE_ANON_KEY || '';
const ADMIN_PUBLISH_SECRET = process.env.ADMIN_PUBLISH_SECRET;

const supabase = SUPABASE_URL && SUPABASE_SERVICE_ROLE_KEY
  ? createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)
  : null;

export default async function handler(req: VercelRequest, res: VercelResponse) {
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

  if (ADMIN_PUBLISH_SECRET && ADMIN_PUBLISH_SECRET.trim() !== '') {
    if (requestSecret !== ADMIN_PUBLISH_SECRET) {
      return res.status(401).json({ success: false, message: 'Invalid admin publishing secret' });
    }
  }

  if (!supabase) {
    return res.status(503).json({ success: false, message: 'Database client not connected' });
  }

  // Expects items: [{ id: string, level_number: number }]
  const { items } = req.body || {};

  if (!Array.isArray(items) || items.length === 0) {
    return res.status(400).json({ success: false, message: 'Array of items with id and level_number required' });
  }

  try {
    for (const item of items) {
      if (item.id && typeof item.level_number === 'number') {
        await supabase
          .from('custom_patterns')
          .update({
            level_number: item.level_number,
            updated_at: new Date().toISOString()
          })
          .eq('id', item.id);
      }
    }

    return res.status(200).json({
      success: true,
      message: 'Level order updated successfully',
    });
  } catch (err: any) {
    return res.status(500).json({ success: false, message: err.message });
  }
}
