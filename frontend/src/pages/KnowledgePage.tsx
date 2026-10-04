import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Article = { id: string; title: string; body: string; status: string; category?: string };

export default function KnowledgePage() {
  const [items, setItems] = useState<Article[]>([]);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', body: '', category: '', status: 'DRAFT' });
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState<Article | null>(null);

  async function load() {
    try { const res = await api<{ data: Article[] }>('/api/v1/knowledge'); setItems(res.data); }
    catch (e) { setError(e instanceof Error ? e.message : 'Failed'); }
  }
  useEffect(() => { load(); }, []);

  async function create(e: FormEvent) {
    e.preventDefault(); setLoading(true);
    try {
      await api('/api/v1/knowledge', { method: 'POST', body: JSON.stringify(form) });
      setShowForm(false); setForm({ title: '', body: '', category: '', status: 'DRAFT' }); await load();
    } catch (err) { setError(err instanceof Error ? err.message : 'Create failed'); }
    finally { setLoading(false); }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div><h1 className="text-2xl font-semibold text-slate-900">Knowledge base</h1><p className="text-sm text-slate-500">Help articles for support and AI</p></div>
        <button onClick={() => setShowForm(true)} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium hover:bg-indigo-700">New article</button>
      </div>
      {error && <div className="mb-4 text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={create} className="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg space-y-3">
            <h2 className="text-lg font-semibold">New article</h2>
            <input required value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="Title" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <textarea required value={form.body} onChange={e => setForm({ ...form, body: e.target.value })} placeholder="Body" rows={6} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" />
            <div className="flex gap-2">
              <input value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} placeholder="Category" className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm" />
              <select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })} className="rounded-lg border border-slate-300 px-3 py-2 text-sm"><option value="DRAFT">Draft</option><option value="PUBLISHED">Published</option></select>
            </div>
            <div className="flex justify-end gap-2">
              <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 text-sm text-slate-600">Cancel</button>
              <button type="submit" disabled={loading} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium disabled:opacity-50">{loading ? 'Saving…' : 'Create'}</button>
            </div>
          </form>
        </div>
      )}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="space-y-2">
          {items.length === 0 && <div className="bg-white rounded-xl border border-slate-200 px-4 py-10 text-center text-slate-400">No articles yet</div>}
          {items.map(a => (
            <button key={a.id} onClick={() => setSelected(a)} className={`w-full text-left bg-white rounded-xl border px-4 py-3 hover:border-indigo-200 ${selected?.id === a.id ? 'border-indigo-400' : 'border-slate-200'}`}>
              <div className="font-medium text-slate-900 text-sm">{a.title}</div>
              <div className="text-xs text-slate-400 mt-1">{a.status} {a.category ? `· ${a.category}` : ''}</div>
            </button>
          ))}
        </div>
        <div className="bg-white rounded-xl border border-slate-200 p-5 min-h-[200px]">
          {selected ? (<><h2 className="font-semibold text-slate-900 mb-2">{selected.title}</h2><p className="text-sm text-slate-600 whitespace-pre-wrap">{selected.body}</p></>) : (<p className="text-slate-400 text-sm">Select an article to read</p>)}
        </div>
      </div>
    </div>
  );
}
