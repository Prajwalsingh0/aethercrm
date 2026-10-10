import { type FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Account = { id: string; name: string; industry?: string; email?: string; status: string };

export default function AccountsPage() {
  const [items, setItems] = useState<Account[]>([]);
  const [name, setName] = useState('');
  const [error, setError] = useState('');

  async function load() {
    try {
      const res = await api<{ data: Account[] }>('/api/v1/accounts');
      setItems(res.data);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }
  useEffect(() => { load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault();
    try {
      await api('/api/v1/accounts', { method: 'POST', body: JSON.stringify({ name }) });
      setName(''); await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Create failed'); }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-4">Accounts</h1>
      {error && <div className="text-red-600 text-sm mb-2">{error}</div>}
      <form onSubmit={create} className="flex gap-2 mb-4">
        <input value={name} onChange={(e) => setName(e.target.value)} required placeholder="Account name" className="border rounded-lg px-3 py-2 text-sm" />
        <button className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm">Add</button>
      </form>
      <div className="bg-white border rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500">
            <tr><th className="px-4 py-3">Name</th><th className="px-4 py-3">Industry</th><th className="px-4 py-3">Status</th></tr>
          </thead>
          <tbody>
            {items.map((a) => (
              <tr key={a.id} className="border-t">
                <td className="px-4 py-3 font-medium">{a.name}</td>
                <td className="px-4 py-3">{a.industry || '—'}</td>
                <td className="px-4 py-3">{a.status}</td>
              </tr>
            ))}
            {items.length === 0 && <tr><td colSpan={3} className="px-4 py-8 text-center text-slate-400">No accounts</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
