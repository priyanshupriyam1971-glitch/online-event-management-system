import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client.js';
import StatCard from '../../components/StatCard.jsx';

export default function OrganizerDashboard() {
  const [events, setEvents] = useState(null);
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    client.get('/organizers/events')
      .then(({ data }) => setEvents(data))
      .catch((err) => setError(err.response?.data?.message || 'Could not load events'));
    client.get('/organizers/stats')
      .then(({ data }) => setStats(data))
      .catch(() => {});
  }, []);

  if (error) return <div className="empty"><div className="alert alert-error">{error}</div></div>;
  if (!events) return <div className="empty"><p className="muted">Loading…</p></div>;

  const statusLabel = {
    PENDING: ['⏳ Pending approval', 'badge-pending'],
    APPROVED: ['✅ Approved', 'badge-success'],
    REJECTED: ['❌ Rejected', 'badge-danger']
  };

  return (
    <div>
      <div className="page-header">
        <h1>Organizer Dashboard</h1>
        <Link to="/organizer/events/new" className="btn btn-primary">+ Create Event</Link>
      </div>

      {stats && (
        <div className="stat-grid">
          <StatCard icon="📅" label="My Events" value={stats.myEvents} />
          <StatCard icon="🎟️" label="Registrations" value={stats.totalRegistrations} />
          <StatCard icon="🧾" label="Tickets Sold" value={stats.ticketsSold} />
          <StatCard icon="💰" label="Revenue" value={`$${Number(stats.revenue).toFixed(2)}`} />
        </div>
      )}

      {events.length === 0 ? (
        <div className="empty">
          <h2>No events yet</h2>
          <p className="muted">Create your first event — it will go live after admin approval.</p>
        </div>
      ) : (
        <div className="event-grid">
          {events.map((event) => (
            <Link to={`/organizer/events/${event.id}`} key={event.id} className="event-card">
              <div className="event-meta">
                <span className={`badge ${statusLabel[event.status][1]}`}>
                  {statusLabel[event.status][0]}
                </span>
                <span>{new Date(event.eventDate).toLocaleDateString(undefined, {
                  weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
                })}</span>
              </div>
              <h3>{event.title}</h3>
              <p className="muted">{event.venue || 'Venue to be announced'}</p>
              {event.rejectionReason && (
                <p className="text-danger small">Reason: {event.rejectionReason}</p>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
