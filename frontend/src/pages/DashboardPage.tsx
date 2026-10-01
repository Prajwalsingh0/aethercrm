import { useEffect, useState } from 'react';
import { api } from '../api/client';

type Summary = {
  leads: { total: number; new: number; qualified: number; converted: number };
  accounts: { total: number };
  contacts: { total: number };
  pipeline: { openDeals: number; openValue: number; weightedValue: number };
};

export default function DashboardPage() {
  const [data, setData] = useState<Summary | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api<Summary>('/api/v1/dashboard/summary').then(setData).catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="text-red-600">{error}</div>;
  if (!data) return <div className="text-slate-500">Loading dashboard…</div>;

  const cards = [
    { label: 'Total leads', value: data.leads.total, sub: `${data.leads.new} new` },
    { label: 'Qualified', value: data.leads.qualified, sub: `${data.leads.converted} converted` },
    { label: 'Accounts', value: data.accounts.total, sub: `${data.contacts.total} contacts` },
    { label: 'Open deals', value: data.pipeline.openDeals, sub: `Weighted ₹${Number(data.pipeline.weightedValue).toLocaleString()}` },
  ];

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Dashboard</h1>
      <p className="text-slate-500 text-sm mb-6">Live metrics from your organization</p>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map((c) => (
          <div key={c.label} className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <div className="text-sm text-slate-500 font-medium">{c.label}</div>
            <div className="text-3xl font-semibold text-slate-900 mt-1">{c.value}</div>
            <div className="text-xs text-slate-400 mt-2">{c.sub}</div>
          </div>
        ))}
      </div>
      <div className="mt-8 bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
        <h2 className="font-semibold text-slate-900 mb-2">Pipeline value</h2>
        <p className="text-slate-600 text-sm">
          Open value: <strong>₹{Number(data.pipeline.openValue).toLocaleString()}</strong>
          {' · '}Weighted: <strong>₹{Number(data.pipeline.weightedValue).toLocaleString()}</strong>
        </p>
      </div>
    </div>
  );
}
