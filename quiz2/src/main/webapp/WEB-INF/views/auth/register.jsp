<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../partials/header.jsp" %>

<div style="max-width:580px;margin:20px auto">
  <div class="panel">
    <div class="kicker">Member Registration</div>
    <h1 class="page-title" style="font-size:30px;margin-bottom:8px">Join Wild Indonesia</h1>
    <p style="font-size:14px;color:var(--ink-2);margin-bottom:22px">
      Register as a traveler to plan Indonesia itineraries and bookings, or as a curator to edit the archipelago guide.
    </p>

    <form action="${pageContext.request.contextPath}/register" method="post" style="display:flex;flex-direction:column;gap:14px">
      <div class="form-grid-2">
        <div class="field">
          <label>Full Name</label>
          <input type="text" name="full_name" class="input" placeholder="e.g. Rinaltra Nabasa" required>
        </div>
        <div class="field">
          <label>Email Address</label>
          <input type="email" name="email" class="input" placeholder="you@example.com" required>
        </div>
      </div>

      <div class="form-grid-3">
        <div class="field">
          <label>Password</label>
          <input type="password" name="password" class="input" placeholder="Min. 4 chars" required>
        </div>
        <div class="field">
          <label>Account Role</label>
          <select name="role" class="select">
            <option value="traveler">Traveler (Explorer)</option>
            <option value="curator">Guide Curator (Admin)</option>
          </select>
        </div>
        <div class="field">
          <label>Nationality</label>
          <input type="text" name="nationality" class="input" value="Indonesia">
        </div>
      </div>

      <div class="field">
        <label>Phone / WhatsApp</label>
        <input type="text" name="phone" class="input" placeholder="+62 812-...">
      </div>

      <div class="field">
        <label>Travel Bio</label>
        <textarea name="bio" class="textarea" placeholder="Favorite Indonesian islands or travel style..."></textarea>
      </div>

      <button type="submit" class="pill-btn" style="width:100%">Create Account</button>
    </form>
  </div>
</div>

<%@ include file="../partials/footer.jsp" %>
