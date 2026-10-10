import { type FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Product = { id: string; name: string; sku?: string; unitPrice: number; currency?: string; isActive: boolean; category?: string };

export default function ProductsPage() {
  const [items, setItems] = useState<Product[]>([]);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', sku: '', unitPrice: '0', category: '', currency: 'INR' });
  const [loading, setLoading] = useState(false);

  async function load() {
    try {
      const res = await api<{ data: Product[] }>('/api/v1/products');
      setItems(res.data);
    } catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }
  useEffect(() => { load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await api('/api/v1/products', { method: 'POST', body: JSON.stringify({ ...form, unitPrice: Number(form.unitPrice) || 0, isActive: true }) });
      setShowForm(false);
      setForm({ name: '', sku: '', unitPrice: '0', category: '', currency: 'INR' });
      await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Create failed'); }
    finally { setLoading(false); }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div><h1 className="text-2xl font-semibold text-slate-900">Products</h1><p className="text-sm text-slate-500">Catalog for deals and quotes</p></div>
        <button onClick={() => setShowForm(true)} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">New product</button>
      </div>
      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={create} className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md space-y-3">
            <h2 className="text-lg font-semibold">New product</h2>
            <input required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Name" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <input value={form.sku} onChange={e => setForm({ ...form, sku: e.target.value })} placeholder="SKU" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <div className="flex gap-2">
              <input value={form.unitPrice} onChange={e => setForm({ ...form, unitPrice: e.target.value })} placeholder="Unit price" className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm" />
              <input value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} placeholder="Category" className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm" />
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
          <thead className="bg-slate-50 text-left text-slate-500"><tr><th className="px-4 py-3 font-medium">Name</th><th className="px-4 py-3 font-medium">SKU</th><th className="px-4 py-3 font-medium">Price</th><th className="px-4 py-3 font-medium">Category</th><th className="px-4 py-3 font-medium">Status</th></tr></thead>
          <tbody className="divide-y divide-slate-100">
            {items.length === 0 && <tr><td colSpan={5} className="px-4 py-8 text-center text-slate-400">No products yet</td></tr>}
            {items.map(p => (
              <tr key={p.id} className="hover:bg-slate-50">
                <td className="px-4 py-3 font-medium text-slate-900">{p.name}</td>
                <td className="px-4 py-3 text-slate-500 font-mono text-xs">{p.sku || '—'}</td>
                <td className="px-4 py-3">{p.currency || 'INR'} {Number(p.unitPrice).toLocaleString()}</td>
                <td className="px-4 py-3 text-slate-500">{p.category || '—'}</td>
                <td className="px-4 py-3"><span className={`text-xs px-2 py-0.5 rounded-full ${p.isActive ? 'bg-green-100 text-green-800' : 'bg-slate-100 text-slate-600'}`}>{p.isActive ? 'Active' : 'Inactive'}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
