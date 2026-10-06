import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client.js';

export default function AttendeeDashboard() {
  const [registrations, setRegistrations] = useState(null);
  const [notifications, setNotifications] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    client.get('/attendees/registrations')
      .then(({ data }) => setRegistrations(data))
      .catch((err) => setError(err.response?.data?.message || 'Could not load registrations'));
    client.get('/attendees/notifications')
      .then(({ data }) => setNotifications(data))
      .catch(() => setNotifications([]));
  }, []);

  if (error) return <div className="empty"><div className="alert alert-error">{error}</div></div>;
  if (!registrations) return <div className="empty"><p className="muted">Loading…</p></div>;

  return (
    <div>
      <div className="page-header"><h1>My Tickets & Registrations</h1></div>

      {registrations.length === 0 ? (
        <div className="empty">
          <h2>No registrations yet</h2>
          <p className="muted">Browse events and register to see your tickets here.</p>
          <Link to="/" className="btn btn-primary">Browse Events</Link>
        </div>
      ) : (
        registrations.map((reg) => (
          <div key={reg.id} className="panel ticket-card">
            <div className="ticket-card-header">
              <div>
                <h2>{reg.eventTitle}</h2>
                <p className="muted">
                  {new Date(reg.eventDate).toLocaleString()} • {reg.venue || 'Venue TBA'}
                </p>
              </div>
              <span className={`badge ${
                reg.status === 'CONFIRMED' ? 'badge-success'
                : reg.status === 'PENDING' ? 'badge-pending'
                : 'badge-danger'
              }`}>{reg.status}</span>
            </div>
            <div className="ticket-card-body">
              {reg.items.map((item, i) => (
                <div key={i} className="ticket-row">
                  <div>
                    <strong>{item.ticketTypeName}</strong>
                    <div className="muted">
                      {item.quantity} × ${Number(item.unitPrice).toFixed(2)} • {item.paymentStatus}
                    </div>
                  </div>
                  <strong>${Number(item.totalAmount).toFixed(2)}</strong>
                </div>
              ))}
            </div>
            <div className="ticket-card-footer">
              <span className="muted small">
                Registered on {new Date(reg.registrationDate).toLocaleString()}
              </span>
              <span className="total">Total: <strong>${Number(reg.totalAmount).toFixed(2)}</strong></span>
            </div>
          </div>
        ))
      )}

      {notifications && notifications.length > 0 && (
        <section className="panel" style={{ marginTop: '1rem' }}>
          <h2>Updates from Organizers</h2>
          {notifications.map((n) => (
            <div key={n.id} className="message-item">
              <strong>{n.title}</strong>
              <div className="muted small">{n.eventTitle} • by {n.organizerName}</div>
              <p>{n.content}</p>
              <div className="muted small">{new Date(n.createdAt).toLocaleString()}</div>
            </div>
          ))}
        </section>
      )}
    </div>
  );
}
