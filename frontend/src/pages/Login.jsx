import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { user } = await login(email, password);
      navigate(user.role === 'ADMIN' ? '/admin' : user.role === 'ORGANIZER' ? '/organizer' : '/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Welcome back</h1>
        <p className="muted">Sign in to manage and attend events</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={submit}>
          <label>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

          <label>Password</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

          <button className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p className="muted center">
          No account? <Link to="/register">Create one</Link>
        </p>

        <div className="demo-hints">
          <strong>Demo accounts</strong>
          <div>admin@event.com / admin123</div>
          <div>organizer@event.com / organizer123</div>
          <div>attendee@event.com / attendee123</div>
        </div>
      </div>
    </div>
  );
}
