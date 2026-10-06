import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner container">
        <Link to="/" className="brand">
          <span className="brand-icon">&#127909;</span>
          EventSphere
        </Link>

        <nav className="nav-links">
          <Link to="/">Browse Events</Link>

          {!user && (
            <>
              <Link to="/login" className="btn btn-ghost">Login</Link>
              <Link to="/register" className="btn btn-primary">Sign Up</Link>
            </>
          )}

          {user && user.role === 'ADMIN' && (
            <>
              <Link to="/admin">Admin Dashboard</Link>
              <Link to="/admin/users">Users</Link>
            </>
          )}

          {user && user.role === 'ORGANIZER' && (
            <>
              <Link to="/organizer">Organizer Dashboard</Link>
            </>
          )}

          {user && (
            <>
              <Link to="/attendee">My Tickets</Link>
              <span className="nav-user">
                {user.name} <span className={`badge badge-${user.role.toLowerCase()}`}>{user.role}</span>
              </span>
              <button className="btn btn-ghost" onClick={handleLogout}>Logout</button>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
