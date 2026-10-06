import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import client from '../../api/client.js';

export default function EventManage() {
  const { id } = useParams();

  const [event, setEvent] = useState(null);
  const [registrations, setRegistrations] = useState(null);
  const [error, setError] = useState('');

  // ticket form
  const [ticketForm, setTicketForm] = useState({ name: '', price: '', quantity: '' });
  const [editingTicket, setEditingTicket] = useState(null);

  // message form
  const [messageForm, setMessageForm] = useState({ title: '', content: '' });
  const [messages, setMessages] = useState([]);
  const [notice, setNotice] = useState('');

  const load = () => {
    client.get(`/organizers/events/${id}`)
      .then(({ data }) => setEvent(data))
      .catch((err) => setError(err.response?.data?.message || 'Could not load event'));
    client.get(`/organizers/events/${id}/registrations`)
      .then(({ data }) => setRegistrations(data))
      .catch(() => setRegistrations([]));
    client.get(`/organizers/events/${id}/messages`)
      .then(({ data }) => setMessages(data))
      .catch(() => {});
  };

  useEffect(load, [id]);

  const saveTicket = async (e) => {
    e.preventDefault();
    setNotice('');
    const payload = {
      name: ticketForm.name,
      price: Number(ticketForm.price),
      quantity: Number(ticketForm.quantity)
    };
    try {
      if (editingTicket) {
        await client.put(`/organizers/tickets/${editingTicket.id}`, payload);
      } else {
        await client.post(`/organizers/events/${id}/tickets`, payload);
      }
      setTicketForm({ name: '', price: '', quantity: '' });
      setEditingTicket(null);
      load();
    } catch (err) {
      setNotice(err.response?.data?.message || 'Save failed');
    }
  };

  const removeTicket = async (ticket) => {
    if (!window.confirm(`Delete ticket type "${ticket.name}"?`)) return;
    try {
      await client.delete(`/organizers/tickets/${ticket.id}`);
      load();
    } catch (err) {
      setNotice(err.response?.data?.message || 'Delete failed');
    }
  };

  const sendMessage = async (e) => {
    e.preventDefault();
    setNotice('');
    try {
      await client.post(`/organizers/events/${id}/messages`, messageForm);
      setMessageForm({ title: '', content: '' });
      load();
    } catch (err) {
      setNotice(err.response?.data?.message || 'Send failed');
    }
  };

  if (error) {
    return (
      <div className="empty">
        <div className="alert alert-error">{error}</div>
        <Link to="/organizer" className="btn btn-primary">Back to dashboard</Link>
      </div>
    );
  }
  if (!event) return <div className="empty"><p className="muted">Loading…</p></div>;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{event.title}</h1>
          <p className="muted">
            {new Date(event.eventDate).toLocaleString()} • {event.venue || 'Venue TBA'} •{' '}
            <span className={`badge ${event.status === 'APPROVED' ? 'badge-success' : event.status === 'PENDING' ? 'badge-pending' : 'badge-danger'}`}>
              {event.status}
            </span>
          </p>
        </div>
        <div>
          <Link to={`/organizer/events/${id}/edit`} className="btn btn-ghost">Edit Event</Link>
          <Link to={`/events/${id}`} className="btn btn-ghost">View Public Page</Link>
        </div>
      </div>

      {event.rejectionReason && (
        <div className="alert alert-error">Rejected: {event.rejectionReason}</div>
      )}
      {notice && <div className="alert alert-error">{notice}</div>}

      <div className="two-col">
        <section className="panel">
          <h2>{editingTicket ? 'Edit Ticket' : 'Add Ticket Type'}</h2>
          <form onSubmit={saveTicket}>
            <label>Ticket name</label>
            <input
              value={ticketForm.name}
              onChange={(e) => setTicketForm({ ...ticketForm, name: e.target.value })}
              required
            />
            <div className="form-row">
              <div>
                <label>Price ($)</label>
                <input
                  type="number" min="0" step="0.01"
                  value={ticketForm.price}
                  onChange={(e) => setTicketForm({ ...ticketForm, price: e.target.value })}
                  required
                />
              </div>
              <div>
                <label>Quantity</label>
                <input
                  type="number" min="1"
                  value={ticketForm.quantity}
                  onChange={(e) => setTicketForm({ ...ticketForm, quantity: e.target.value })}
                  required
                />
              </div>
            </div>
            <button className="btn btn-primary" type="submit">
              {editingTicket ? 'Update Ticket' : 'Add Ticket'}
            </button>
            {editingTicket && (
              <button className="btn btn-ghost" type="button" onClick={() => {
                setEditingTicket(null);
                setTicketForm({ name: '', price: '', quantity: '' });
              }}>Cancel</button>
            )}
          </form>

          <h2 style={{ marginTop: '1.5rem' }}>Tickets</h2>
          {event.tickets.length === 0 ? (
            <p className="muted">No tickets yet.</p>
          ) : (
            event.tickets.map((ticket) => (
              <div key={ticket.id} className="ticket-row">
                <div>
                  <strong>{ticket.name}</strong>
                  <div className="muted">
                    ${Number(ticket.price).toFixed(2)} • sold {ticket.soldQuantity} / {ticket.quantity}
                    • {ticket.available} left
                  </div>
                </div>
                <div className="row-actions">
                  <button className="btn btn-ghost" onClick={() => {
                    setEditingTicket(ticket);
                    setTicketForm({
                      name: ticket.name,
                      price: String(ticket.price),
                      quantity: String(ticket.quantity)
                    });
                  }}>Edit</button>
                  <button className="btn btn-danger" onClick={() => removeTicket(ticket)}>Delete</button>
                </div>
              </div>
            ))
          )}
        </section>

        <div>
          <section className="panel">
            <h2>Attendee Communication</h2>
            <form onSubmit={sendMessage}>
              <label>Message title</label>
              <input
                value={messageForm.title}
                onChange={(e) => setMessageForm({ ...messageForm, title: e.target.value })}
                required
              />
              <label>Message</label>
              <textarea rows="3"
                value={messageForm.content}
                onChange={(e) => setMessageForm({ ...messageForm, content: e.target.value })}
                required />
              <button className="btn btn-primary" type="submit">Send to Attendees</button>
            </form>
            {messages.length > 0 && (
              <div style={{ marginTop: '1rem' }}>
                <h3>Sent messages</h3>
                {messages.map((m) => (
                  <div key={m.id} className="message-item">
                    <strong>{m.title}</strong>
                    <p>{m.content}</p>
                    <div className="muted small">{new Date(m.createdAt).toLocaleString()}</div>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="panel" style={{ marginTop: '1rem' }}>
            <h2>Registrations ({registrations ? registrations.length : '…'})</h2>
            {!registrations ? (
              <p className="muted">Loading…</p>
            ) : registrations.length === 0 ? (
              <p className="muted">No registrations yet.</p>
            ) : (
              <ul className="activity-list">
                {registrations.map((reg) => (
                  <li key={reg.id}>
                    <strong>${Number(reg.totalAmount).toFixed(2)}</strong>
                    <div>
                      {reg.items.map((it) => `${it.quantity}× ${it.ticketTypeName}`).join(', ')}
                    </div>
                    <div className="muted small">
                      {new Date(reg.registrationDate).toLocaleString()} • {reg.status}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}
