import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Ticket = {
  id: string;
  ticketNumber: string;
  subject: string;
  description?: string;
  status: string;
  priority: string;
  category?: string;
};

const statuses = ['OPEN', 'IN_PROGRESS', 'WAITING', 'RESOLVED', 'CLOSED'];
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export default function SupportPage() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ subject: '', description: '', priority: 'MEDIUM', category: 'General' });
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');

  async function load() {
    try {
      const q = statusFilter ? `?status=${statusFilter}` : '';
      const res = await api<{ data: Ticket[] }>(`/api/v1/tickets${q}`);
      setTickets(res.data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load');
    }
  }

  useEffect(() => { load(); }, [statusFilter]);

  async function create(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await api('/api/v1/tickets', { method: 'POST', body: JSON.stringify(form) });
      setShowForm(false);
      setForm({ subject: '', description: '', priority: 'MEDIUM', category: 'General' });
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create failed');
    } finally {
      setLoading(false);
    }
  }

  async function updateStatus(id: string, status: string) {
    try {
      await api(`/api/v1/tickets/${id}`, { method: 'PUT', body: JSON.stringify({ status }) });
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Update failed');
    }
  }

  const badge = (s: string) => {
    const colors: Record<string, string> = {
      OPEN: 'bg-blue-100 text-blue-800',
      IN_PROGRESS: 'bg-amber-100 text-amber-800',
      WAITING: 'bg-purple-100 text-purple-800',
      RESOLVED: 'bg-green-100 text-green-800',
      CLOSED: 'bg-slate-100 text-slate-600',
      URGENT: 'bg-red-100 text-red-800',
      HIGH: 'bg-orange-100 text-orange-800',
      MEDIUM: 'bg-yellow-100 text-yellow-800',
      LOW: 'bg-slate-100 text-slate-600',
    };
    return colors[s] || 'bg-slate-100 text-slate-700';
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Support</h1>
          <p className="text-sm text-slate-500">Tickets and customer issues</p>
        </div>
        <button onClick={() => setShowForm(true)}
          className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">
          New ticket
        </button>
      </div>

      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="mb-4 flex gap-2 flex-wrap">
        <button onClick={() => setStatusFilter('')}
          className={`text-xs px-3 py-1.5 rounded-full border ${!statusFilter ? 'bg-indigo-600 text-white border-indigo-600' : 'border-slate-300 text-slate-600'}`}>All</button>
        {statuses.map(s => (
          <button key={s} onClick={() => setStatusFilter(s)}
            className={`text-xs px-3 py-1.5 rounded-full border ${statusFilter === s ? 'bg-indigo-600 text-white border-indigo-600' : 'border-slate-300 text-slate-600'}`}>
            {s.replace('_', ' ')}
          </button>
        ))}
      </div>

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={create} className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-lg font-semibold">New support ticket</h2>
            <input required value={form.subject} onChange={e => setForm({ ...form, subject: e.target.value })}
              placeholder="Subject" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <textarea value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}
              placeholder="Description" rows={3} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <div className="flex gap-2">
              <select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm flex-1">
                {priorities.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
              <input value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}
                placeholder="Category" className="rounded-lg border border-slate-300 px-3 py-2 text-sm flex-1" />
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

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500">
            <tr>
              <th className="px-4 py-3 font-medium">Ticket</th>
              <th className="px-4 py-3 font-medium">Subject</th>
              <th className="px-4 py-3 font-medium">Priority</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {tickets.length === 0 && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-400">No tickets yet</td></tr>
            )}
            {tickets.map(t => (
              <tr key={t.id} className="hover:bg-slate-50">
                <td className="px-4 py-3 font-mono text-xs text-slate-500">{t.ticketNumber}</td>
                <td className="px-4 py-3 font-medium text-slate-900">{t.subject}</td>
                <td className="px-4 py-3"><span className={`text-xs px-2 py-0.5 rounded-full ${badge(t.priority)}`}>{t.priority}</span></td>
                <td className="px-4 py-3"><span className={`text-xs px-2 py-0.5 rounded-full ${badge(t.status)}`}>{t.status.replace('_', ' ')}</span></td>
                <td className="px-4 py-3">
                  <select value={t.status} onChange={e => updateStatus(t.id, e.target.value)}
                    className="text-xs border border-slate-200 rounded px-2 py-1">
                    {statuses.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
