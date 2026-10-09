import { createClient, RealtimeChannel } from '@supabase/supabase-js';
import { CustomPatternRecord } from '../types/game';

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
    .order('level_number', { ascending: true });

  if (error) {
    console.error('Error fetching published patterns:', error);
    return [];
  }

  return data as CustomPatternRecord[];
}

export function subscribeToRealtimePatterns(
  onUpdate: (patterns: CustomPatternRecord[]) => void
): RealtimeChannel | null {
  if (!supabaseUrl || !supabaseAnonKey) return null;

  const channel = supabase
    .channel('custom_patterns_realtime')
    .on(
      'postgres_changes',
      { event: '*', schema: 'public', table: 'custom_patterns' },
      async () => {
        const latest = await fetchPublishedPatterns();
        onUpdate(latest);
      }
    )
    .subscribe();

  return channel;
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

export async function deletePatternViaApi(
  id: string,
  publishSecret: string
): Promise<{ success: boolean; message?: string }> {
  try {
    const response = await fetch('/api/delete', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Admin-Secret': publishSecret,
      },
      body: JSON.stringify({ id }),
    });

    const result = await response.json();
    return result;
  } catch (err: any) {
    if (supabaseUrl && supabaseAnonKey) {
      const { error } = await supabase.from('custom_patterns').delete().eq('id', id);
      if (!error) return { success: true };
    }
    return { success: false, message: err.message || 'Failed to delete pattern' };
  }
}

export async function reorderPatternsViaApi(
  items: Array<{ id: string; level_number: number }>,
  publishSecret: string
): Promise<{ success: boolean; message?: string }> {
  try {
    const response = await fetch('/api/reorder', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Admin-Secret': publishSecret,
      },
      body: JSON.stringify({ items }),
    });

    const result = await response.json();
    return result;
  } catch (err: any) {
    // Direct client fallback
    if (supabaseUrl && supabaseAnonKey) {
      for (const item of items) {
        await supabase.from('custom_patterns').update({ level_number: item.level_number }).eq('id', item.id);
      }
      return { success: true };
    }
    return { success: false, message: err.message || 'Failed to reorder patterns' };
  }
}
