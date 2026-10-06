import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import client from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function EventDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [event, setEvent] = useState(null);
  const [error, setError] = useState('');
  const [quantities, setQuantities] = useState({});
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    client.get(`/events/${id}`)
      .then(({ data }) => setEvent(data))
      .catch(() => setError('Event not found or not published yet.'));
  }, [id]);

  if (error) {
    return (
      <div className="empty">
        <h2>{error}</h2>
        <Link to="/" className="btn btn-primary">Back to events</Link>
      </div>
    );
  }
  if (!event) return <div className="empty"><p className="muted">Loading…</p></div>;

  const setQty = (ticketId, value) => {
    setQuantities((prev) => ({ ...prev, [ticketId]: Math.max(0, Number(value) || 0) }));
  };

  const selected = event.tickets
    .filter((t) => (quantities[t.id] || 0) > 0)
    .map((t) => ({ ticketTypeId: t.id, quantity: quantities[t.id] }));

  const total = event.tickets.reduce(
    (sum, t) => sum + Number(t.price) * (quantities[t.id] || 0), 0
  );

  const register = async (e) => {
    e.preventDefault();
    setError(''); setMessage('');
    if (!user) { navigate('/login'); return; }
    if (selected.length === 0) { setError('Select at least one ticket.'); return; }
    setSubmitting(true);
    try {
      const { data } = await client.post(`/events/${id}/register`, { items: selected });
      setMessage(`Registration submitted — awaiting admin confirmation. Total: $${data.totalAmount}. See it under "My Tickets".`);
      setQuantities({});
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="event-hero">
        <div className="event-meta">
          <span className="badge badge-category">{event.category || 'General'}</span>
          <span>{new Date(event.eventDate).toLocaleString(undefined, {
            weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
          })}</span>
        </div>
        <h1>{event.title}</h1>
        <p className="muted">&#128205; {event.venue || 'Venue to be announced'} &nbsp;•&nbsp; Organized by {event.organizerName}</p>
        <p className="event-description">{event.description}</p>
      </div>

      <div className="panel">
        <h2>Tickets</h2>

        {event.tickets.length === 0 ? (
          <p className="muted">No tickets available for this event yet.</p>
        ) : (
          <form onSubmit={register}>
            {event.tickets.map((ticket) => (
              <div key={ticket.id} className="ticket-row">
                <div>
                  <strong>{ticket.name}</strong>
                  <div className="muted">
                    ${Number(ticket.price).toFixed(2)} &nbsp;•&nbsp; {ticket.available} of {ticket.quantity} left
                  </div>
                </div>
                <input
                  type="number"
                  min="0"
                  max={ticket.available}
                  placeholder="Qty"
                  value={quantities[ticket.id] || ''}
                  onChange={(e) => setQty(ticket.id, e.target.value)}
                />
              </div>
            ))}

            {error && <div className="alert alert-error">{error}</div>}
            {message && <div className="alert alert-success">{message}</div>}

            <div className="ticket-footer">
              <div className="total">Total: <strong>${total.toFixed(2)}</strong></div>
              <button className="btn btn-primary" disabled={submitting || !user}>
                {!user ? 'Sign in to register' : submitting ? 'Registering…' : 'Register & Pay (demo)'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
