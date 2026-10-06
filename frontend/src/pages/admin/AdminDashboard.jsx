import { useEffect, useState } from 'react';
import client from '../../api/client.js';
import StatCard from '../../components/StatCard.jsx';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [events, setEvents] = useState([]);
  const [pendingRegistrations, setPendingRegistrations] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    client.get('/admin/stats')
      .then(({ data }) => setStats(data))
      .catch(() => setError('Could not load dashboard'));
    client.get('/admin/events')
      .then(({ data }) => setEvents(data))
      .catch(() => {});
    client.get('/admin/registrations', { params: { status: 'PENDING' } })
      .then(({ data }) => setPendingRegistrations(data))
      .catch(() => {});
  }, []);

  const pending = events.filter((e) => e.status === 'PENDING');

  const act = async (id, action) => {
    try {
      await client.post(`/admin/events/${id}/${action}`,
        action === 'reject' ? { reason: 'Does not meet platform guidelines' } : {});
      setEvents((prev) => prev.filter((e) => e.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Action failed');
    }
  };

  const actRegistration = async (id, action) => {
    try {
      await client.post(`/admin/registrations/${id}/${action}`);
      setPendingRegistrations((prev) => prev.filter((r) => r.id !== id));
      if (stats) setStats({ ...stats, pendingRegistrations: Math.max(0, stats.pendingRegistrations - 1) });
    } catch (err) {
      setError(err.response?.data?.message || 'Action failed');
    }
  };

  if (error) {
    return (
      <div className="empty">
        <h2>Admin Dashboard</h2>
        <div className="alert alert-error">{error}</div>
      </div>
    );
  }
  if (!stats) return <div className="empty"><p className="muted">Loading…</p></div>;

  return (
    <div>
      <div className="page-header"><h1>Admin Dashboard</h1></div>

      <div className="stat-grid">
        <StatCard icon="👥" label="Users" value={stats.users} />
        <StatCard icon="📅" label="Total Events" value={stats.events} />
        <StatCard icon="⏳" label="Pending Events" value={stats.pendingEvents} />
        <StatCard icon="✅" label="Approved Events" value={stats.approvedEvents} />
        <StatCard icon="🎫" label="Registrations" value={stats.registrations} />
        <StatCard icon="💳" label="Pending Registrations" value={stats.pendingRegistrations} />
        <StatCard icon="🧾" label="Tickets Sold" value={stats.ticketsSold} />
        <StatCard icon="💰" label="Revenue" value={`$${Number(stats.revenue).toFixed(2)}`} />
      </div>

      <div className="two-col">
        <section className="panel">
          <h2>Event Approvals ({pending.length})</h2>
          {pending.length === 0 ? (
            <p className="muted">No events waiting for approval.</p>
          ) : (
            pending.map((event) => (
              <div key={event.id} className="approval-row">
                <div>
                  <strong>{event.title}</strong>
                  <div className="muted">
                    by {event.organizerName} • {event.category || 'General'} •{' '}
                    {new Date(event.eventDate).toLocaleDateString()}
                  </div>
                </div>
                <div className="row-actions">
                  <button className="btn btn-success" onClick={() => act(event.id, 'approve')}>Approve</button>
                  <button className="btn btn-danger" onClick={() => act(event.id, 'reject')}>Reject</button>
                </div>
              </div>
            ))
          )}
        </section>

        <section className="panel">
          <h2>Ticket Payment Approvals ({pendingRegistrations.length})</h2>
          <p className="muted small" style={{ marginTop: '-0.4rem' }}>
            Registrations are confirmed and paid only after your approval.
          </p>
          {pendingRegistrations.length === 0 ? (
            <p className="muted">No pending registrations.</p>
          ) : (
            pendingRegistrations.map((reg) => (
              <div key={reg.id} className="approval-row">
                <div>
                  <strong>{reg.attendeeName}</strong>
                  <span className="muted"> &lt;{reg.attendeeEmail}&gt;</span>
                  <div className="muted">
                    {reg.eventTitle} • {reg.items.map((it) => `${it.quantity}× ${it.ticketTypeName}`).join(', ')}
                  </div>
                  <div className="muted small">
                    {new Date(reg.registrationDate).toLocaleString()} • Total: ${Number(reg.totalAmount).toFixed(2)}
                  </div>
                </div>
                <div className="row-actions">
                  <button className="btn btn-success" onClick={() => actRegistration(reg.id, 'approve')}>Confirm</button>
                  <button className="btn btn-danger" onClick={() => actRegistration(reg.id, 'cancel')}>Cancel</button>
                </div>
              </div>
            ))
          )}
        </section>
      </div>

      <section className="panel">
        <h2>Recent Activity</h2>
        {stats.recentActivities.length === 0 ? (
          <p className="muted">No activity yet.</p>
        ) : (
          <ul className="activity-list">
            {stats.recentActivities.map((log, i) => (
              <li key={i}>
                <span className="activity-action">{log.action}</span>
                <div>{log.details}</div>
                <div className="muted small">
                  {log.actor} • {new Date(log.createdAt).toLocaleString()}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
