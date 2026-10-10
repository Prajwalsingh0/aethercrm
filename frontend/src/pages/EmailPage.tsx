import { type FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type EmailRow = {
  id: string; status: string; from: string; to: string; subject: string;
  body?: string; provider?: string; relatedType?: string; sentAt?: string; createdAt?: string; errorMessage?: string;
};

type MailStatus = { enabled: boolean; configured: boolean; from: string; host?: string; mode: string };

export default function EmailPage() {
  const [items, setItems] = useState<EmailRow[]>([]);
  const [mailStatus, setMailStatus] = useState<MailStatus | null>(null);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ to: '', cc: '', subject: '', body: '', relatedType: '' });
  const [loading, setLoading] = useState(false);

  async function load() {
    try {
      const [list, st] = await Promise.all([
        api<{ data: EmailRow[] }>('/api/v1/emails'),
        api<MailStatus>('/api/v1/emails/status'),
      ]);
      setItems(list.data);
      setMailStatus(st);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load');
    }
  }

  useEffect(() => { load(); }, []);

  async function send(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api('/api/v1/emails/send', {
        method: 'POST',
        body: JSON.stringify({
          to: form.to,
          cc: form.cc || undefined,
          subject: form.subject,
          body: form.body,
          relatedType: form.relatedType || undefined,
        }),
      });
      setShowForm(false);
      setForm({ to: '', cc: '', subject: '', body: '', relatedType: '' });
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Send failed');
    } finally {
      setLoading(false);
    }
  }

  const statusBadge = (s: string) => {
    const map: Record<string, string> = {
      SENT: 'bg-green-100 text-green-800',
      LOGGED: 'bg-blue-100 text-blue-800',
      FAILED: 'bg-red-100 text-red-800',
      QUEUED: 'bg-amber-100 text-amber-800',
    };
    return map[s] || 'bg-slate-100 text-slate-700';
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Email</h1>
          <p className="text-sm text-slate-500">Send and track CRM emails</p>
        </div>
        <button onClick={() => setShowForm(true)} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">Compose</button>
      </div>

      {mailStatus && (
        <div className={`mb-4 rounded-lg border px-4 py-3 text-sm ${
          mailStatus.configured ? 'bg-green-50 border-green-100 text-green-800' : 'bg-amber-50 border-amber-100 text-amber-900'
        }`}>
          {mailStatus.configured ? (
            <>SMTP connected · From <strong>{mailStatus.from}</strong> · Host {mailStatus.host}</>
          ) : (
            <>
              <strong>Log-only mode</strong> — emails are recorded in the CRM (and as activities) but not sent via SMTP.
              Set MAIL_ENABLED, MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, and MAIL_FROM to send real mail.
            </>
          )}
        </div>
      )}

      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={send} className="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg space-y-3">
            <h2 className="text-lg font-semibold">Compose email</h2>
            <input required type="email" value={form.to} onChange={(e) => setForm({ ...form, to: e.target.value })} placeholder="To" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <input value={form.cc} onChange={(e) => setForm({ ...form, cc: e.target.value })} placeholder="Cc (optional)" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <input required value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} placeholder="Subject" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <textarea required value={form.body} onChange={(e) => setForm({ ...form, body: e.target.value })} placeholder="Message" rows={6} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <select value={form.relatedType} onChange={(e) => setForm({ ...form, relatedType: e.target.value })} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
              <option value="">Related to (optional)</option>
              <option value="LEAD">Lead</option>
              <option value="CONTACT">Contact</option>
              <option value="ACCOUNT">Account</option>
              <option value="OPPORTUNITY">Opportunity</option>
              <option value="TICKET">Ticket</option>
            </select>
            <div className="flex justify-end gap-2">
              <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 text-sm text-slate-600">Cancel</button>
              <button type="submit" disabled={loading} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
                {loading ? 'Sending…' : mailStatus?.configured ? 'Send' : 'Log email'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500">
            <tr>
              <th className="px-4 py-3 font-medium">Subject</th>
              <th className="px-4 py-3 font-medium">To</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">When</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {items.length === 0 && (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-slate-400">No emails yet</td></tr>
            )}
            {items.map((m) => (
              <tr key={m.id} className="hover:bg-slate-50">
                <td className="px-4 py-3">
                  <div className="font-medium text-slate-900">{m.subject}</div>
                  {m.errorMessage && <div className="text-xs text-red-500 mt-0.5">{m.errorMessage}</div>}
                </td>
                <td className="px-4 py-3 text-slate-600 text-xs">{m.to}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs px-2 py-0.5 rounded-full ${statusBadge(m.status)}`}>{m.status}</span>
                  {m.provider && <span className="text-xs text-slate-400 ml-1">{m.provider}</span>}
                </td>
                <td className="px-4 py-3 text-xs text-slate-400">
                  {m.sentAt || m.createdAt ? new Date(m.sentAt || m.createdAt || '').toLocaleString() : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
