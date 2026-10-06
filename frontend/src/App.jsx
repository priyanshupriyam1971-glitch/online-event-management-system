import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { useAuth } from './context/AuthContext.jsx';

import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Home from './pages/Home.jsx';
import EventDetail from './pages/EventDetail.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import Users from './pages/admin/Users.jsx';
import OrganizerDashboard from './pages/organizer/OrganizerDashboard.jsx';
import EventForm from './pages/organizer/EventForm.jsx';
import EventManage from './pages/organizer/EventManage.jsx';
import AttendeeDashboard from './pages/attendee/AttendeeDashboard.jsx';

export default function App() {
  const { user } = useAuth();

  return (
    <div className="app-shell">
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/events/:id" element={<EventDetail />} />
          <Route path="/login" element={user ? <Navigate to="/" /> : <Login />} />
          <Route path="/register" element={user ? <Navigate to="/" /> : <Register />} />

          <Route path="/admin" element={
            <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
          } />
          <Route path="/admin/users" element={
            <ProtectedRoute roles={['ADMIN']}><Users /></ProtectedRoute>
          } />

          <Route path="/organizer" element={
            <ProtectedRoute roles={['ORGANIZER']}><OrganizerDashboard /></ProtectedRoute>
          } />
          <Route path="/organizer/events/new" element={
            <ProtectedRoute roles={['ORGANIZER']}><EventForm /></ProtectedRoute>
          } />
          <Route path="/organizer/events/:id/edit" element={
            <ProtectedRoute roles={['ORGANIZER']}><EventForm /></ProtectedRoute>
          } />
          <Route path="/organizer/events/:id" element={
            <ProtectedRoute roles={['ORGANIZER']}><EventManage /></ProtectedRoute>
          } />

          <Route path="/attendee" element={
            <ProtectedRoute roles={['ATTENDEE', 'ORGANIZER', 'ADMIN']}><AttendeeDashboard /></ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
      <footer className="footer">
        Online Event Management System — from event creation to attendee participation, everything in one platform.
      </footer>
    </div>
  );
}
