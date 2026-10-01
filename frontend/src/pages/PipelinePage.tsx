import { useEffect, useState } from 'react';
import { api } from '../api/client';

type Stage = { id: string; name: string; probability: number; color?: string; won: boolean; lost: boolean };
type Opp = { id: string; name: string; stageId: string; amount: number; currency: string; probability: number; status: string };

export default function PipelinePage() {
  const [stages, setStages] = useState<Stage[]>([]);
  const [opps, setOpps] = useState<Opp[]>([]);
  const [error, setError] = useState('');
  const [name, setName] = useState('');
  const [amount, setAmount] = useState('');

  async function load() {
    try {
      const [s, o] = await Promise.all([
        api<Stage[]>('/api/v1/pipeline/stages'),
        api<{ data: Opp[] }>('/api/v1/opportunities?status=OPEN'),
      ]);
      setStages(s); setOpps(o.data);
    } catch (e) { setError(e instanceof Error ? e.message : 'Load failed'); }
  }

  useEffect(() => { load(); }, []);

  async function createOpp() {
    if (!name.trim()) return;
    try {
      await api('/api/v1/opportunities', { method: 'POST', body: JSON.stringify({ name, amount: amount || 0 }) });
      setName(''); setAmount(''); await load();
    } catch (e) { setError(e instanceof Error ? e.message : 'Create failed'); }
  }

  async function move(oppId: string, stageId: string) {
    try {
      await api(`/api/v1/opportunities/${oppId}/move`, { method: 'POST', body: JSON.stringify({ stageId }) });
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : 'Move failed'); }
  }

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Sales pipeline</h1>
      <p className="text-sm text-slate-500 mb-6">Stage moves persist to the database with audit history</p>
      {error && <div className="mb-4 text-sm text-red-600">{error}</div>}
      <div className="mb-4 flex gap-2 flex-wrap">
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Opportunity name" className="rounded-lg border border-slate-300 px-3 py-2 text-sm w-48" />
        <input value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="Amount (₹)" className="rounded-lg border border-slate-300 px-3 py-2 text-sm w-32" />
        <button onClick={createOpp} className="rounded-lg bg-indigo-600 text-white px-4 py-2 text-sm font-medium">Add deal</button>
      </div>
      <div className="flex gap-3 overflow-x-auto pb-4">
        {stages.map((stage) => {
          const cards = opps.filter((o) => o.stageId === stage.id);
          return (
            <div key={stage.id} className="w-64 shrink-0 bg-slate-100 rounded-xl p-3">
              <div className="flex items-center gap-2 mb-3">
                <div className="h-2 w-2 rounded-full" style={{ background: stage.color || '#64748b' }} />
                <span className="text-sm font-semibold text-slate-700">{stage.name}</span>
                <span className="text-xs text-slate-400 ml-auto">{cards.length}</span>
              </div>
              <div className="space-y-2 min-h-[120px]">
                {cards.map((o) => (
                  <div key={o.id} className="bg-white rounded-lg border border-slate-200 p-3 shadow-sm">
                    <div className="font-medium text-sm text-slate-900">{o.name}</div>
                    <div className="text-xs text-slate-500 mt-1">₹{Number(o.amount || 0).toLocaleString()} · {o.probability}%</div>
                    <div className="mt-2 flex flex-wrap gap-1">
                      {stages.map((s) => s.id !== stage.id ? (
                        <button key={s.id} onClick={() => move(o.id, s.id)}
                          className="text-[10px] px-1.5 py-0.5 rounded bg-slate-50 border border-slate-200 hover:bg-indigo-50"
                          title={`Move to ${s.name}`}>→ {s.name.split(' ')[0]}</button>
                      ) : null)}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
