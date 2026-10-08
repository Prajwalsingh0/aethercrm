import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { Link } from 'react-router-dom';

type Summary = {
  leads: { total: number; new: number; qualified: number; converted: number };
  accounts: { total: number };
  contacts: { total: number };
  pipeline: { openDeals: number; openValue: number; weightedValue: number };
  support?: { openTickets: number; totalTickets: number };
  activity?: { openTasks: number };
};

type Insights = {
  insights: { code: string; severity: string; text: string }[];
  hotLeads: { id: string; name: string; company?: string; score: number; status: string }[];
  differentiator?: string;
};

export default function DashboardPage() {
  const [data, setData] = useState<Summary | null>(null);
  const [insights, setInsights] = useState<Insights | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      api<Summary>('/api/v1/dashboard/summary'),
      api<Insights>('/api/v1/insights').catch(() => null),
    ])
      .then(([s, i]) => {
        setData(s);
        if (i) setInsights(i);
      })
      .catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="text-red-600">{error}</div>;
  if (!data) return <div className="text-slate-500">Loading dashboard…</div>;

  const cards = [
    { label: 'Total leads', value: data.leads.total, sub: `${data.leads.new} new` },
    { label: 'Qualified', value: data.leads.qualified, sub: `${data.leads.converted} converted` },
    { label: 'Accounts', value: data.accounts.total, sub: `${data.contacts.total} contacts` },
    {
      label: 'Open deals',
      value: data.pipeline.openDeals,
      sub: `Weighted ₹${Number(data.pipeline.weightedValue || 0).toLocaleString('en-IN')}`,
    },
    { label: 'Open tickets', value: data.support?.openTickets ?? 0, sub: `${data.support?.totalTickets ?? 0} total` },
    { label: 'Open tasks', value: data.activity?.openTasks ?? 0, sub: 'Activities' },
  ];

  const severityColor: Record<string, string> = {
    success: 'bg-green-50 border-green-100 text-green-900',
    info: 'bg-blue-50 border-blue-100 text-blue-900',
    warning: 'bg-amber-50 border-amber-100 text-amber-900',
    danger: 'bg-red-50 border-red-100 text-red-900',
  };

  return (
    <div>
      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900 mb-1">Dashboard</h1>
          <p className="text-slate-500 text-sm">Live metrics · smart insights · INR-native</p>
        </div>
        <Link to="/ai" className="text-sm rounded-lg bg-indigo-600 text-white px-3 py-2 font-medium hover:bg-indigo-700">
          AI Command Center
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {cards.map((c) => (
          <div key={c.label} className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <div className="text-sm text-slate-500 font-medium">{c.label}</div>
            <div className="text-3xl font-semibold text-slate-900 mt-1">{c.value}</div>
            <div className="text-xs text-slate-400 mt-2">{c.sub}</div>
          </div>
        ))}
      </div>

      {insights && (
        <div className="mt-8 grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h2 className="font-semibold text-slate-900 mb-3">Smart insights</h2>
            <div className="space-y-2">
              {insights.insights.map((ins) => (
                <div
                  key={ins.code}
                  className={`text-sm rounded-lg border px-3 py-2 ${severityColor[ins.severity] || severityColor.info}`}
                >
                  {ins.text}
                </div>
              ))}
            </div>
            {insights.differentiator && (
              <p className="text-[11px] text-slate-400 mt-3">{insights.differentiator}</p>
            )}
          </div>
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h2 className="font-semibold text-slate-900 mb-3">Hot leads (score ≥ 60)</h2>
            {insights.hotLeads.length === 0 ? (
              <p className="text-sm text-slate-400">No high-score leads yet — add company, email, revenue.</p>
            ) : (
              <ul className="divide-y divide-slate-100">
                {insights.hotLeads.map((l) => (
                  <li key={l.id} className="py-2 flex items-center justify-between text-sm">
                    <div>
                      <div className="font-medium text-slate-900">{l.name || '—'}</div>
                      <div className="text-xs text-slate-400">{l.company || l.status}</div>
                    </div>
                    <span
                      className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                        l.score >= 70 ? 'bg-green-100 text-green-800' : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {l.score}
                    </span>
                  </li>
                ))}
              </ul>
            )}
            <Link to="/leads" className="text-xs text-indigo-600 mt-3 inline-block">
              View all leads →
            </Link>
          </div>
        </div>
      )}

      <div className="mt-8 bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
        <h2 className="font-semibold text-slate-900 mb-2">Pipeline value (INR)</h2>
        <p className="text-slate-600 text-sm">
          Open value: <strong>₹{Number(data.pipeline.openValue || 0).toLocaleString('en-IN')}</strong>
          {' · '}
          Weighted: <strong>₹{Number(data.pipeline.weightedValue || 0).toLocaleString('en-IN')}</strong>
        </p>
      </div>
    </div>
  );
}
