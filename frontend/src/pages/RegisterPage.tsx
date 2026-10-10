import { type FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, storeAuth, type AuthTokens } from '../api/client';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    organizationName: '', organizationSlug: '', email: '', password: '', firstName: '', lastName: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function set(field: string, value: string) {
    setForm((f) => ({ ...f, [field]: value }));
    if (field === 'organizationName' && !form.organizationSlug) {
      const slug = value.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
      setForm((f) => ({ ...f, organizationName: value, organizationSlug: slug }));
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await api<AuthTokens>('/api/v1/auth/register', {
        method: 'POST', body: JSON.stringify(form),
      });
      storeAuth(data);
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100 px-4 py-10">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-lg border border-slate-200 p-8">
        <div className="mb-6 text-center">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-600 text-white font-bold text-xl mb-3">A</div>
          <h1 className="text-2xl font-semibold text-slate-900">Create your workspace</h1>
          <p className="text-slate-500 text-sm mt-1">Start managing customers with AetherCRM</p>
        </div>
        <form onSubmit={onSubmit} className="space-y-4">
          {error && <div className="rounded-lg bg-red-50 text-red-700 text-sm px-3 py-2 border border-red-100">{error}</div>}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">First name</label>
              <input required className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500 outline-none"
                value={form.firstName} onChange={(e) => set('firstName', e.target.value)} />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Last name</label>
              <input required className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500 outline-none"
                value={form.lastName} onChange={(e) => set('lastName', e.target.value)} />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Work email</label>
            <input type="email" required className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500 outline-none"
              value={form.email} onChange={(e) => set('email', e.target.value)} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Password (min 8)</label>
            <input type="password" required minLength={8} className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500 outline-none"
              value={form.password} onChange={(e) => set('password', e.target.value)} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Organization name</label>
            <input required className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500 outline-none"
              value={form.organizationName} onChange={(e) => set('organizationName', e.target.value)} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Slug (URL identifier)</label>
            <input required pattern="[a-z0-9-]+" className="w-full rounded-lg border border-slate-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500 outline-none"
              value={form.organizationSlug} onChange={(e) => set('organizationSlug', e.target.value)} />
          </div>
          <button type="submit" disabled={loading}
            className="w-full rounded-lg bg-indigo-600 text-white py-2.5 font-medium hover:bg-indigo-700 disabled:opacity-60">
            {loading ? 'Creating…' : 'Create workspace'}
          </button>
        </form>
        <p className="text-center text-sm text-slate-500 mt-6">
          Already have an account? <Link to="/login" className="text-indigo-600 font-medium hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
