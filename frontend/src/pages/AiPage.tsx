import { type FormEvent, useEffect, useState } from 'react';
import { api } from '../api/client';

type Msg = { role: 'user' | 'assistant'; content: string; demoMode?: boolean };
type AiAction = { type: string; label: string; params?: Record<string, unknown>; requiresConfirm?: boolean };

export default function AiPage() {
  const [messages, setMessages] = useState<Msg[]>([
    {
      role: 'assistant',
      content:
        'AetherCRM Command Center — I use your tenant data and can propose real actions (score leads, create tasks, log email). Confirm an action below to execute.',
      demoMode: true,
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [actions, setActions] = useState<AiAction[]>([]);
  const [executing, setExecuting] = useState<string | null>(null);
  const [lastResult, setLastResult] = useState('');

  useEffect(() => {
    api<AiAction[]>('/api/v1/ai/actions').then(setActions).catch(() => {});
  }, []);

  async function send(e: FormEvent) {
    e.preventDefault();
    if (!input.trim() || loading) return;
    const userMsg = input.trim();
    setInput('');
    setMessages((m) => [...m, { role: 'user', content: userMsg }]);
    setLoading(true);
    try {
      const res = await api<{ reply: string; demoMode: boolean; proposedActions?: AiAction[] }>(
        '/api/v1/ai/chat',
        { method: 'POST', body: JSON.stringify({ message: userMsg }) }
      );
      setMessages((m) => [...m, { role: 'assistant', content: res.reply, demoMode: res.demoMode }]);
      if (res.proposedActions?.length) setActions(res.proposedActions);
    } catch (err) {
      setMessages((m) => [
        ...m,
        { role: 'assistant', content: err instanceof Error ? err.message : 'Request failed' },
      ]);
    } finally {
      setLoading(false);
    }
  }

  async function runAction(a: AiAction) {
    if (!confirm(`Execute: ${a.label}?`)) return;
    setExecuting(a.type);
    setLastResult('');
    try {
      const res = await api<{ message: string; status: string; affected?: number }>(
        '/api/v1/ai/actions/execute',
        { method: 'POST', body: JSON.stringify({ actionType: a.type, params: a.params || {} }) }
      );
      const text = res.message || `${res.status} (${res.affected ?? 0} affected)`;
      setLastResult(text);
      setMessages((m) => [...m, { role: 'assistant', content: `Action executed: ${text}` }]);
    } catch (err) {
      setLastResult(err instanceof Error ? err.message : 'Action failed');
    } finally {
      setExecuting(null);
    }
  }

  return (
    <div className="flex flex-col h-[calc(100vh-6rem)]">
      <div className="mb-4">
        <h1 className="text-2xl font-semibold text-slate-900">AI Command Center</h1>
        <p className="text-sm text-slate-500">
          Chat + <strong>executable actions</strong> on your tenant · not a passive chatbot
        </p>
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 flex-1 min-h-0">
        <div className="lg:col-span-2 flex flex-col min-h-0">
          <div className="flex-1 overflow-y-auto bg-white border border-slate-200 rounded-xl p-4 space-y-3 shadow-sm">
            {messages.map((m, i) => (
              <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-[85%] rounded-2xl px-4 py-2.5 text-sm whitespace-pre-wrap ${
                    m.role === 'user' ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-800'
                  }`}
                >
                  {m.content}
                  {m.demoMode && m.role === 'assistant' && (
                    <div className="text-[10px] mt-1 opacity-60">Demo AI · actions still real</div>
                  )}
                </div>
              </div>
            ))}
          </div>
          <form onSubmit={send} className="mt-3 flex gap-2">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder='e.g. "score leads" or "create follow-up tasks"'
              className="flex-1 rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
            />
            <button type="submit" disabled={loading} className="rounded-lg bg-indigo-600 text-white px-5 py-2.5 text-sm font-medium disabled:opacity-60">
              {loading ? '…' : 'Send'}
            </button>
          </form>
        </div>
        <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-sm overflow-y-auto">
          <h2 className="font-semibold text-slate-900 mb-1">AI Actions</h2>
          <p className="text-xs text-slate-500 mb-3">Confirm to mutate CRM data (scored leads, tasks, email log).</p>
          <div className="space-y-2">
            {actions.length === 0 && <p className="text-sm text-slate-400">Ask something or wait for suggestions…</p>}
            {actions.map((a) => (
              <button
                key={a.type}
                onClick={() => runAction(a)}
                disabled={!!executing}
                className="w-full text-left rounded-lg border border-indigo-100 bg-indigo-50 hover:bg-indigo-100 px-3 py-2.5 text-sm disabled:opacity-50"
              >
                <div className="font-medium text-indigo-900">{executing === a.type ? 'Running…' : a.label}</div>
                <div className="text-[10px] text-indigo-600 mt-0.5 font-mono">{a.type}</div>
              </button>
            ))}
          </div>
          {lastResult && (
            <div className="mt-4 text-xs rounded-lg bg-green-50 border border-green-100 text-green-800 px-3 py-2">{lastResult}</div>
          )}
          <div className="mt-6 pt-4 border-t border-slate-100">
            <p className="text-[11px] text-slate-400 leading-relaxed">
              <strong>Differentiator:</strong> actions write to Activities, Leads, Email, and Audit — multi-tenant safe.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
