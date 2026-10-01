import { FormEvent, useState } from 'react';
import { api } from '../api/client';

type Msg = { role: 'user' | 'assistant'; content: string; demoMode?: boolean };

export default function AiPage() {
  const [messages, setMessages] = useState<Msg[]>([{
    role: 'assistant',
    content: 'Hi — I am the AetherCRM copilot (demo mode). Ask about leads, pipeline, or request a draft email.',
    demoMode: true,
  }]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  async function send(e: FormEvent) {
    e.preventDefault();
    if (!input.trim() || loading) return;
    const userMsg = input.trim();
    setInput('');
    setMessages((m) => [...m, { role: 'user', content: userMsg }]);
    setLoading(true);
    try {
      const res = await api<{ reply: string; demoMode: boolean }>('/api/v1/ai/chat', {
        method: 'POST', body: JSON.stringify({ message: userMsg }),
      });
      setMessages((m) => [...m, { role: 'assistant', content: res.reply, demoMode: res.demoMode }]);
    } catch (err) {
      setMessages((m) => [...m, { role: 'assistant', content: err instanceof Error ? err.message : 'Request failed' }]);
    } finally { setLoading(false); }
  }

  return (
    <div className="flex flex-col h-[calc(100vh-6rem)]">
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">AI Copilot</h1>
      <p className="text-sm text-slate-500 mb-4">Permission-aware assistant · demo provider</p>
      <div className="flex-1 overflow-y-auto bg-white border border-slate-200 rounded-xl p-4 space-y-3 shadow-sm">
        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] rounded-2xl px-4 py-2.5 text-sm whitespace-pre-wrap ${
              m.role === 'user' ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-800'
            }`}>
              {m.content}
              {m.demoMode && m.role === 'assistant' && <div className="text-[10px] mt-1 opacity-60">Demo AI</div>}
            </div>
          </div>
        ))}
      </div>
      <form onSubmit={send} className="mt-3 flex gap-2">
        <input value={input} onChange={(e) => setInput(e.target.value)} placeholder="e.g. Show pipeline summary"
          className="flex-1 rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
        <button type="submit" disabled={loading} className="rounded-lg bg-indigo-600 text-white px-5 py-2.5 text-sm font-medium disabled:opacity-60">
          {loading ? '…' : 'Send'}
        </button>
      </form>
    </div>
  );
}
