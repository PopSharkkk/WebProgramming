<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Member Settings</div>
    <h1 class="page-title">Traveler Profile</h1>
    <p class="page-lead">Update your contact identity used on Indonesia trip itineraries, field stories, and expedition bookings.</p>
  </div>
  <span class="tag tag-dark"><c:out value="${sessionScope.currentUser.role}"/></span>
</div>

<div class="panel" style="max-width:680px">
  <form action="${pageContext.request.contextPath}/profile" method="post" style="display:flex;flex-direction:column;gap:14px">
    <div class="form-grid-2">
      <div class="field">
        <label>Full Name</label>
        <input type="text" name="full_name" class="input" value="<c:out value='${sessionScope.currentUser.full_name}'/>" required>
      </div>
      <div class="field">
        <label>Email (Read-Only)</label>
        <input type="email" class="input" value="<c:out value='${sessionScope.currentUser.email}'/>" disabled>
      </div>
    </div>

    <div class="form-grid-2">
      <div class="field">
        <label>Nationality</label>
        <input type="text" name="nationality" class="input" value="<c:out value='${sessionScope.currentUser.nationality}'/>">
      </div>
      <div class="field">
        <label>Phone / WhatsApp</label>
        <input type="text" name="phone" class="input" value="<c:out value='${sessionScope.currentUser.phone}'/>">
      </div>
    </div>

    <div class="field">
      <label>Explorer Bio</label>
      <textarea name="bio" class="textarea"><c:out value="${sessionScope.currentUser.bio}"/></textarea>
    </div>

    <div style="display:flex;gap:10px">
      <button type="submit" class="pill-btn">Save Profile Changes</button>
      <a href="${pageContext.request.contextPath}/bookings" class="pill-btn pill-btn-outline">My Bookings</a>
      <a href="${pageContext.request.contextPath}/saved" class="pill-btn pill-btn-outline">Bucket List</a>
    </div>
  </form>
</div>

<%@ include file="../partials/footer.jsp" %>
