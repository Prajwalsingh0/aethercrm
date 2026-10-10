import { type FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Campaign = { id: string; name: string; type: string; status: string; budget?: number; currency?: string };
const statuses = ['DRAFT', 'SCHEDULED', 'ACTIVE', 'PAUSED', 'COMPLETED'];
const types = ['EMAIL', 'SOCIAL', 'ADS', 'EVENT', 'OTHER'];

export default function CampaignsPage() {
  const [items, setItems] = useState<Campaign[]>([]);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', type: 'EMAIL', status: 'DRAFT', budget: '', targetAudience: '' });
  const [loading, setLoading] = useState(false);

  async function load() {
    try { const res = await api<{ data: Campaign[] }>('/api/v1/campaigns'); setItems(res.data); }
    catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }
  useEffect(() => { load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault(); setLoading(true);
    try {
      await api('/api/v1/campaigns', { method: 'POST', body: JSON.stringify({ name: form.name, type: form.type, status: form.status, budget: form.budget ? Number(form.budget) : null, targetAudience: form.targetAudience || null }) });
      setShowForm(false); setForm({ name: '', type: 'EMAIL', status: 'DRAFT', budget: '', targetAudience: '' }); await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Create failed'); }
    finally { setLoading(false); }
  }

  async function setStatus(id: string, status: string) {
    try { await api(`/api/v1/campaigns/${id}`, { method: 'PUT', body: JSON.stringify({ status }) }); await load(); }
    catch (err) { setError(err instanceof Error ? err.message : 'Update failed'); }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div><h1 className="text-2xl font-semibold text-slate-900">Campaigns</h1><p className="text-sm text-slate-500">Marketing campaigns</p></div>
        <button onClick={() => setShowForm(true)} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">New campaign</button>
      </div>
      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={create} className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md space-y-3">
            <h2 className="text-lg font-semibold">New campaign</h2>
            <input required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Name" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <div className="flex gap-2">
              <select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })} className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm">{types.map(t => <option key={t} value={t}>{t}</option>)}</select>
              <input value={form.budget} onChange={e => setForm({ ...form, budget: e.target.value })} placeholder="Budget" className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            </div>
            <div className="flex justify-end gap-2">
              <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 text-sm text-slate-600">Cancel</button>
              <button type="submit" disabled={loading} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium disabled:opacity-50">{loading ? 'Saving…' : 'Create'}</button>
            </div>
          </form>
        </div>
      )}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500"><tr><th className="px-4 py-3 font-medium">Name</th><th className="px-4 py-3 font-medium">Type</th><th className="px-4 py-3 font-medium">Budget</th><th className="px-4 py-3 font-medium">Status</th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {items.length === 0 && <tr><td colSpan={4} className="px-4 py-8 text-center text-slate-400">No campaigns yet</td></tr>}
            {items.map(c => (
              <tr key={c.id} className="hover:bg-slate-50">
                <td className="px-4 py-3 font-medium text-slate-900">{c.name}</td>
                <td className="px-4 py-3 text-slate-500">{c.type}</td>
                <td className="px-4 py-3">{c.budget != null ? `${c.currency || 'INR'} ${Number(c.budget).toLocaleString()}` : '—'}</td>
                <td className="px-4 py-3"><select value={c.status} onChange={e => setStatus(c.id, e.target.value)} className="text-xs border border-slate-200 rounded px-2 py-1">{statuses.map(s => <option key={s} value={s}>{s}</option>)}</select></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
