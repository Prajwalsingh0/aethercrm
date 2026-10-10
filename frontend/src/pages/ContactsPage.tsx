import { type FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Contact = { id: string; fullName: string; email?: string; title?: string; status: string };

export default function ContactsPage() {
  const [items, setItems] = useState<Contact[]>([]);
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '' });
  const [error, setError] = useState('');

  async function load() {
    try {
      const res = await api<{ data: Contact[] }>('/api/v1/contacts');
      setItems(res.data);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }
  useEffect(() => { load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault();
    try {
      await api('/api/v1/contacts', { method: 'POST', body: JSON.stringify(form) });
      setForm({ firstName: '', lastName: '', email: '' }); await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Create failed'); }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-4">Contacts</h1>
      {error && <div className="text-red-600 text-sm mb-2">{error}</div>}
      <form onSubmit={create} className="flex gap-2 mb-4 flex-wrap">
        <input required placeholder="First" className="border rounded-lg px-3 py-2 text-sm" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
        <input required placeholder="Last" className="border rounded-lg px-3 py-2 text-sm" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
        <input type="email" placeholder="Email" className="border rounded-lg px-3 py-2 text-sm" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        <button className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm">Add</button>
      </form>
      <div className="bg-white border rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500">
            <tr><th className="px-4 py-3">Name</th><th className="px-4 py-3">Email</th><th className="px-4 py-3">Title</th></tr>
          </thead>
          <tbody>
            {items.map((c) => (
              <tr key={c.id} className="border-t">
                <td className="px-4 py-3 font-medium">{c.fullName}</td>
                <td className="px-4 py-3">{c.email || '—'}</td>
                <td className="px-4 py-3">{c.title || '—'}</td>
              </tr>
            ))}
            {items.length === 0 && <tr><td colSpan={3} className="px-4 py-8 text-center text-slate-400">No contacts</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
