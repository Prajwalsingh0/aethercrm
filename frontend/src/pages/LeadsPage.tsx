import { type FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Lead = { id: string; fullName: string; email?: string; company?: string; status: string; score: number; source?: string };

export default function LeadsPage() {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [q, setQ] = useState('');
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', company: '', source: 'MANUAL' });
  const [loading, setLoading] = useState(false);

  async function load() {
    try {
      const res = await api<{ data: Lead[] }>(`/api/v1/leads?q=${encodeURIComponent(q)}`);
      setLeads(res.data);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed to load'); }
  }

  useEffect(() => { load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await api('/api/v1/leads', { method: 'POST', body: JSON.stringify(form) });
      setShowForm(false);
      setForm({ firstName: '', lastName: '', email: '', company: '', source: 'MANUAL' });
      await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Create failed'); }
    finally { setLoading(false); }
  }

  async function convert(id: string) {
    if (!confirm('Convert this lead to contact & account?')) return;
    try {
      await api(`/api/v1/leads/${id}/convert`, { method: 'POST' });
      await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Convert failed'); }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Leads</h1>
          <p className="text-sm text-slate-500">Capture, score, and convert</p>
        </div>
        <button onClick={() => setShowForm(true)} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">New lead</button>
      </div>
      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      <div className="mb-4 flex gap-2">
        <input value={q} onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && load()}
          placeholder="Search leads…" className="rounded-lg border border-slate-300 px-3 py-2 text-sm w-64 focus:ring-2 focus:ring-indigo-500 outline-none" />
        <button onClick={load} className="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50">Search</button>
      </div>
      {showForm && (
        <form onSubmit={create} className="mb-6 bg-white border border-slate-200 rounded-xl p-4 grid grid-cols-2 gap-3 shadow-sm">
          <input required placeholder="First name" className="border rounded-lg px-3 py-2 text-sm" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
          <input required placeholder="Last name" className="border rounded-lg px-3 py-2 text-sm" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          <input type="email" placeholder="Email" className="border rounded-lg px-3 py-2 text-sm" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <input placeholder="Company" className="border rounded-lg px-3 py-2 text-sm" value={form.company} onChange={(e) => setForm({ ...form, company: e.target.value })} />
          <div className="col-span-2 flex gap-2">
            <button type="submit" disabled={loading} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm">{loading ? 'Saving…' : 'Save lead'}</button>
            <button type="button" onClick={() => setShowForm(false)} className="rounded-lg border px-4 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-slate-500 text-left">
            <tr><th className="px-4 py-3 font-medium">Name</th><th className="px-4 py-3 font-medium">Company</th><th className="px-4 py-3 font-medium">Status</th><th className="px-4 py-3 font-medium">Score</th><th className="px-4 py-3 font-medium">Actions</th></tr>
          </thead>
          <tbody>
            {leads.length === 0 && <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-400">No leads yet. Create one to get started.</td></tr>}
            {leads.map((l) => (
              <tr key={l.id} className="border-t border-slate-100 hover:bg-slate-50">
                <td className="px-4 py-3"><div className="font-medium text-slate-900">{l.fullName || '—'}</div><div className="text-xs text-slate-400">{l.email}</div></td>
                <td className="px-4 py-3 text-slate-600">{l.company || '—'}</td>
                <td className="px-4 py-3"><span className="inline-flex rounded-full bg-slate-100 text-slate-700 px-2 py-0.5 text-xs font-medium">{l.status}</span></td>
                <td className="px-4 py-3"><span className="font-medium text-indigo-600">{l.score}</span></td>
                <td className="px-4 py-3">{l.status !== 'CONVERTED' && <button onClick={() => convert(l.id)} className="text-xs text-indigo-600 hover:underline">Convert</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
