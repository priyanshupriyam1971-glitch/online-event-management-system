import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import client from '../../api/client.js';

export default function EventForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({
    title: '', description: '', category: '', venue: '', eventDate: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    client.get(`/organizers/events/${id}`)
      .then(({ data }) => setForm({
        title: data.title,
        description: data.description || '',
        category: data.category || '',
        venue: data.venue || '',
        eventDate: data.eventDate.slice(0, 16)
      }))
      .catch((err) => setError(err.response?.data?.message || 'Could not load event'));
  }, [id, isEdit]);

  const submit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const payload = { ...form, eventDate: new Date(form.eventDate).toISOString() };
      if (isEdit) {
        await client.put(`/organizers/events/${id}`, payload);
      } else {
        await client.post('/organizers/events', payload);
      }
      navigate('/organizer');
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed');
    } finally {
      setLoading(false);
    }
  };

  const set = (field) => (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));

  return (
    <div>
      <div className="page-header">
        <h1>{isEdit ? 'Edit Event' : 'Create Event'}</h1>
      </div>

      <div className="auth-page">
        <div className="auth-card">
          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={submit}>
            <label>Event title</label>
            <input value={form.title} onChange={set('title')} required />

            <label>Description</label>
            <textarea rows="5" value={form.description} onChange={set('description')} />

            <div className="form-row">
              <div>
                <label>Category</label>
                <input value={form.category} onChange={set('category')} placeholder="e.g. Tech, Music, Sports" />
              </div>
              <div>
                <label>Venue</label>
                <input value={form.venue} onChange={set('venue')} placeholder="Location" />
              </div>
            </div>

            <label>Date & time</label>
            <input
              type="datetime-local"
              value={form.eventDate}
              onChange={set('eventDate')}
              required
            />

            <button className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Saving…' : isEdit ? 'Update Event' : 'Submit for Approval'}
            </button>
          </form>

          <p className="muted center small">
            New events are reviewed by an admin before going live.
          </p>
        </div>
      </div>
    </div>
  );
}
