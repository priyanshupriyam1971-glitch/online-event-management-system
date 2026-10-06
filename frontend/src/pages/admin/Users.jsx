import { useEffect, useState } from 'react';
import client from '../../api/client.js';

export default function Users() {
  const [users, setUsers] = useState(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null);

  const load = () => {
    client.get('/admin/users')
      .then(({ data }) => setUsers(data))
      .catch((err) => setError(err.response?.data?.message || 'Could not load users'));
  };

  useEffect(load, []);

  const save = async (user) => {
    try {
      await client.put(`/admin/users/${user.id}`, {
        name: user.name,
        role: user.role,
        enabled: user.enabled
      });
      setEditing(null);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Update failed');
    }
  };

  const remove = async (user) => {
    if (!window.confirm(`Delete user ${user.name} (${user.email})?`)) return;
    try {
      await client.delete(`/admin/users/${user.id}`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed');
    }
  };

  if (error) return <div className="empty"><div className="alert alert-error">{error}</div></div>;
  if (!users) return <div className="empty"><p className="muted">Loading…</p></div>;

  return (
    <div>
      <div className="page-header"><h1>User Management</h1></div>

      <section className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                {editing === user.id ? (
                  <>
                    <td>
                      <input
                        value={user.name}
                        onChange={(e) => setUsers((prev) => prev.map((u) =>
                          u.id === user.id ? { ...u, name: e.target.value } : u))}
                      />
                    </td>
                    <td>{user.email}</td>
                    <td>
                      <select
                        value={user.role}
                        onChange={(e) => setUsers((prev) => prev.map((u) =>
                          u.id === user.id ? { ...u, role: e.target.value } : u))}
                      >
                        <option value="ATTENDEE">ATTENDEE</option>
                        <option value="ORGANIZER">ORGANIZER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                    </td>
                    <td>
                      <select
                        value={user.enabled}
                        onChange={(e) => setUsers((prev) => prev.map((u) =>
                          u.id === user.id ? { ...u, enabled: e.target.value === 'true' } : u))}
                      >
                        <option value={true}>Active</option>
                        <option value={false}>Disabled</option>
                      </select>
                    </td>
                    <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                    <td>
                      <button className="btn btn-success" onClick={() => save(user)}>Save</button>
                      <button className="btn btn-ghost" onClick={() => setEditing(null)}>Cancel</button>
                    </td>
                  </>
                ) : (
                  <>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td><span className={`badge badge-${user.role.toLowerCase()}`}>{user.role}</span></td>
                    <td>{user.enabled ? <span className="text-success">Active</span> : <span className="text-danger">Disabled</span>}</td>
                    <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                    <td>
                      <button className="btn btn-ghost" onClick={() => setEditing(user.id)}>Edit</button>
                      <button className="btn btn-danger" onClick={() => remove(user)}>Delete</button>
                    </td>
                  </>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}
