import { createClient } from '@supabase/supabase-js';
import { CustomPatternRecord, Level } from '../types/game';

// Standard Supabase browser credentials (safe anon key)
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || '';
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || '';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

export async function fetchPublishedPatterns(): Promise<CustomPatternRecord[]> {
  try {
    const res = await fetch('/api/public/patterns');
    if (res.ok) {
      const data = await res.json();
      return data;
    }
  } catch (e) {
    console.warn('Fallback to direct Supabase client for published patterns');
  }

  if (!supabaseUrl || !supabaseAnonKey) return [];

  const { data, error } = await supabase
    .from('custom_patterns')
    .select('*')
    .eq('status', 'published')
    .order('published_at', { ascending: false });

  if (error) {
    console.error('Error fetching published patterns:', error);
    return [];
  }

  return data as CustomPatternRecord[];
}

export async function publishPatternViaApi(
  pattern: Partial<CustomPatternRecord>,
  publishSecret: string
): Promise<{ success: boolean; data?: any; message?: string }> {
  try {
    const response = await fetch('/api/publish', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Admin-Secret': publishSecret,
      },
      body: JSON.stringify(pattern),
    });

    const result = await response.json();
    return result;
  } catch (err: any) {
    return { success: false, message: err.message || 'Network error publishing pattern' };
  }
}
