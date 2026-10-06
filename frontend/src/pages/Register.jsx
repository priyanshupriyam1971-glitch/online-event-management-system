import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ATTENDEE');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { user } = await register({ name, email, password, role });
      navigate(user.role === 'ORGANIZER' ? '/organizer' : '/');
    } catch (err) {
      const data = err.response?.data;
      setError(data?.message || (data?.errors ? Object.values(data.errors).join(', ') : 'Registration failed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Create account</h1>
        <p className="muted">Join the platform as an attendee or event organizer</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={submit}>
          <label>Full name</label>
          <input value={name} onChange={(e) => setName(e.target.value)} required />

          <label>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

          <label>Password (min 6 characters)</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

          <label>I want to</label>
          <select value={role} onChange={(e) => setRole(e.target.value)}>
            <option value="ATTENDEE">Attend events</option>
            <option value="ORGANIZER">Organize events</option>
          </select>

          <button className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating account…' : 'Sign Up'}
          </button>
        </form>

        <p className="muted center">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
