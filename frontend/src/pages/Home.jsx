import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client.js';

export default function Home() {
  const [events, setEvents] = useState([]);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const params = {};
      if (search) params.search = search;
      if (category) params.category = category;
      const { data } = await client.get('/events', { params });
      setEvents(data);
    } catch (err) {
      setError('Could not load events');
    }
  };

  useEffect(() => { load(); }, [search, category]);

  const categories = [...new Set(events.map((e) => e.category).filter(Boolean))];

  return (
    <div>
      <section className="hero">
        <h1>Discover and manage events — all in one place</h1>
        <p className="muted">
          Browse upcoming events, register instantly, and manage everything from a single dashboard.
        </p>
        <div className="search-bar">
          <input
            type="text"
            placeholder="Search events by title, venue or description…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="">All categories</option>
            {categories.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
        </div>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      {events.length === 0 ? (
        <div className="empty">
          <h2>No events found</h2>
          <p className="muted">
            Events appear here once an organizer creates one and an admin approves it.
          </p>
        </div>
      ) : (
        <div className="event-grid">
          {events.map((event) => (
            <Link to={`/events/${event.id}`} key={event.id} className="event-card">
              <div className="event-meta">
                <span className="badge badge-category">{event.category || 'General'}</span>
                <span>{new Date(event.eventDate).toLocaleDateString(undefined, {
                  weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
                })}</span>
              </div>
              <h3>{event.title}</h3>
              <p className="muted">{event.venue || 'Venue to be announced'}</p>
              <p className="event-desc">
                {event.description ? event.description.slice(0, 120) + (event.description.length > 120 ? '…' : '') : ''}
              </p>
              <span className="event-organizer">by {event.organizerName}</span>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
