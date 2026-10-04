import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Activity = {
  id: string;
  type: string;
  subject: string;
  description?: string;
  status: string;
  priority: string;
  dueAt?: string;
};

const types = ['TASK', 'CALL', 'MEETING', 'NOTE', 'EMAIL'];

export default function ActivitiesPage() {
  const [items, setItems] = useState<Activity[]>([]);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ type: 'TASK', subject: '', description: '', priority: 'MEDIUM', dueAt: '' });
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('OPEN');

  async function load() {
    try {
      const params = new URLSearchParams();
      if (statusFilter) params.set('status', statusFilter);
      const res = await api<{ data: Activity[] }>(`/api/v1/activities?${params}`);
      setItems(res.data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load');
    }
  }

  useEffect(() => { load(); }, [statusFilter]);

  async function create(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const body: Record<string, unknown> = {
        type: form.type,
        subject: form.subject,
        description: form.description,
        priority: form.priority,
      };
      if (form.dueAt) body.dueAt = new Date(form.dueAt).toISOString();
      await api('/api/v1/activities', { method: 'POST', body: JSON.stringify(body) });
      setShowForm(false);
      setForm({ type: 'TASK', subject: '', description: '', priority: 'MEDIUM', dueAt: '' });
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create failed');
    } finally {
      setLoading(false);
    }
  }

  async function complete(id: string) {
    try {
      await api(`/api/v1/activities/${id}/complete`, { method: 'POST' });
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed');
    }
  }

  const typeIcon: Record<string, string> = {
    TASK: '\u2713', CALL: '\uD83D\uDCDE', MEETING: '\uD83D\uDCC5', NOTE: '\uD83D\uDCDD', EMAIL: '\u2709',
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Activities</h1>
          <p className="text-sm text-slate-500">Tasks, calls, meetings, and notes</p>
        </div>
        <button onClick={() => setShowForm(true)}
          className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">
          New activity
        </button>
      </div>

      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="mb-4 flex gap-2">
        {['OPEN', 'COMPLETED', ''].map(s => (
          <button key={s || 'all'} onClick={() => setStatusFilter(s)}
            className={`text-xs px-3 py-1.5 rounded-full border ${statusFilter === s ? 'bg-indigo-600 text-white border-indigo-600' : 'border-slate-300 text-slate-600'}`}>
            {s || 'All'}
          </button>
        ))}
      </div>

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={create} className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-lg font-semibold">New activity</h2>
            <select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
              {types.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
            <input required value={form.subject} onChange={e => setForm({ ...form, subject: e.target.value })}
              placeholder="Subject" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <textarea value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}
              placeholder="Description" rows={2} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <div className="flex gap-2">
              <select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm flex-1">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
              <input type="datetime-local" value={form.dueAt} onChange={e => setForm({ ...form, dueAt: e.target.value })}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm flex-1" />
            </div>
            <div className="flex justify-end gap-2">
              <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 text-sm text-slate-600">Cancel</button>
              <button type="submit" disabled={loading}
                className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
                {loading ? 'Creating\u2026' : 'Create'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="space-y-2">
        {items.length === 0 && (
          <div className="bg-white rounded-xl border border-slate-200 px-4 py-10 text-center text-slate-400">No activities yet</div>
        )}
        {items.map(a => (
          <div key={a.id} className="bg-white rounded-xl border border-slate-200 px-4 py-3 flex items-start gap-3">
            <div className="h-9 w-9 rounded-lg bg-slate-100 flex items-center justify-center text-sm shrink-0">{typeIcon[a.type] || '\u2022'}</div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span className="font-medium text-slate-900 text-sm">{a.subject}</span>
                <span className="text-xs text-slate-400 uppercase">{a.type}</span>
                {a.status === 'COMPLETED' && <span className="text-xs bg-green-100 text-green-700 px-1.5 py-0.5 rounded">Done</span>}
              </div>
              {a.description && <p className="text-xs text-slate-500 mt-0.5">{a.description}</p>}
            </div>
            {a.status === 'OPEN' && (
              <button onClick={() => complete(a.id)} className="text-xs text-indigo-600 hover:text-indigo-800 font-medium shrink-0">Complete</button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
