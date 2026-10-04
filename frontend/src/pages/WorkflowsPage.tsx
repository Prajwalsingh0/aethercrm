import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Workflow = { id: string; name: string; description?: string; isActive: boolean; triggerType: string };

export default function WorkflowsPage() {
  const [items, setItems] = useState<Workflow[]>([]);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', description: '', triggerType: 'ENTITY_EVENT' });
  const [loading, setLoading] = useState(false);

  async function load() {
    try { const res = await api<{ data: Workflow[] }>('/api/v1/workflows'); setItems(res.data); }
    catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }
  useEffect(() => { load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault(); setLoading(true);
    try {
      await api('/api/v1/workflows', { method: 'POST', body: JSON.stringify({ name: form.name, description: form.description, triggerType: form.triggerType, triggerConfigJson: '{"entity":"LEAD","event":"CREATED"}', actionsJson: '[{"type":"NOTIFY","message":"New event"}]', isActive: true }) });
      setShowForm(false); setForm({ name: '', description: '', triggerType: 'ENTITY_EVENT' }); await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Create failed'); }
    finally { setLoading(false); }
  }

  async function toggle(w: Workflow) {
    try { await api(`/api/v1/workflows/${w.id}`, { method: 'PUT', body: JSON.stringify({ isActive: !w.isActive }) }); await load(); }
    catch (err) { setError(err instanceof Error ? err.message : 'Update failed'); }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div><h1 className="text-2xl font-semibold text-slate-900">Workflows</h1><p className="text-sm text-slate-500">Automation rule definitions</p></div>
        <button onClick={() => setShowForm(true)} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">New workflow</button>
      </div>
      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={create} className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md space-y-3">
            <h2 className="text-lg font-semibold">New workflow</h2>
            <input required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Name" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <input value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Description" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <select value={form.triggerType} onChange={e => setForm({ ...form, triggerType: e.target.value })} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
              <option value="ENTITY_EVENT">Entity event</option><option value="SCHEDULE">Schedule</option>
            </select>
            <div className="flex justify-end gap-2">
              <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 text-sm text-slate-600">Cancel</button>
              <button type="submit" disabled={loading} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium disabled:opacity-50">{loading ? 'Saving…' : 'Create'}</button>
            </div>
          </form>
        </div>
      )}
      <div className="space-y-2">
        {items.length === 0 && <div className="bg-white rounded-xl border border-slate-200 px-4 py-10 text-center text-slate-400">No workflows yet</div>}
        {items.map(w => (
          <div key={w.id} className="bg-white rounded-xl border border-slate-200 px-4 py-3 flex items-center gap-3">
            <div className="flex-1">
              <div className="font-medium text-slate-900 text-sm">{w.name}</div>
              <div className="text-xs text-slate-400 mt-0.5">{w.triggerType} {w.description ? `· ${w.description}` : ''}</div>
            </div>
            <button onClick={() => toggle(w)} className={`text-xs px-2 py-1 rounded-full ${w.isActive ? 'bg-green-100 text-green-800' : 'bg-slate-100 text-slate-600'}`}>{w.isActive ? 'Active' : 'Inactive'}</button>
          </div>
        ))}
      </div>
    </div>
  );
}
