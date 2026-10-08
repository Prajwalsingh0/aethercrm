import { useEffect, useState } from 'react';
import { api } from '../api/client';

type AuditRow = {
  id: string;
  action: string;
  entityType: string;
  entityId?: string;
  summary: string;
  createdAt: string;
  actorId?: string;
};

export default function AuditPage() {
  const [items, setItems] = useState<AuditRow[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api<AuditRow[]>('/api/v1/audit/recent')
      .then(setItems)
      .catch((e) => setError(e.message));
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Audit trail</h1>
      <p className="text-sm text-slate-500 mb-6">
        Who changed what — tenant-scoped trust log (AI actions included)
      </p>
      {error && <div className="text-red-600 text-sm mb-4">{error}</div>}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-slate-500">
            <tr>
              <th className="px-4 py-3 font-medium">When</th>
              <th className="px-4 py-3 font-medium">Action</th>
              <th className="px-4 py-3 font-medium">Entity</th>
              <th className="px-4 py-3 font-medium">Summary</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {items.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-slate-400">
                  No audit events yet — create leads or run AI actions
                </td>
              </tr>
            )}
            {items.map((r) => (
              <tr key={r.id} className="hover:bg-slate-50">
                <td className="px-4 py-3 text-xs text-slate-400 whitespace-nowrap">
                  {r.createdAt ? new Date(r.createdAt).toLocaleString() : '—'}
                </td>
                <td className="px-4 py-3">
                  <span className="text-xs font-mono bg-slate-100 px-1.5 py-0.5 rounded">{r.action}</span>
                </td>
                <td className="px-4 py-3 text-xs text-slate-600">
                  {r.entityType}
                  {r.entityId ? ` · ${r.entityId.slice(0, 8)}…` : ''}
                </td>
                <td className="px-4 py-3 text-slate-800">{r.summary}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
