<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 06 · Expedition Rental Bookings CRUD</div>
    <h1 class="page-title">Rental &amp; Local Guide Bookings</h1>
    <p class="page-lead">Reserve Wild phinisi yachts, 4x4 volcano jeeps, eco-villas, and certified local guides with server-calculated IDR totals.</p>
  </div>
  <div style="display:flex;gap:8px;flex-wrap:wrap">
    <a href="${pageContext.request.contextPath}/bookings?all=1" class="pill-btn pill-btn-outline pill-btn-sm">View All Ledger</a>
    <a href="#bookingFormDrawer" onclick="document.getElementById('bookingFormDrawer').open = true" class="pill-btn pill-btn-sm">+ New Reservation</a>
  </div>
</div>

<form action="${pageContext.request.contextPath}/bookings" method="get" class="filter-strip">
  <input type="hidden" name="all" value="${viewAll ? '1' : '0'}">
  <div class="field" style="max-width:220px">
    <label>Filter Status</label>
    <select name="status" class="select">
      <option value="">All Statuses</option>
      <option value="Pending" ${selectedStatus == 'Pending' ? 'selected' : ''}>Pending</option>
      <option value="Confirmed" ${selectedStatus == 'Confirmed' ? 'selected' : ''}>Confirmed</option>
      <option value="Completed" ${selectedStatus == 'Completed' ? 'selected' : ''}>Completed</option>
      <option value="Cancelled" ${selectedStatus == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
    </select>
  </div>
  <button type="submit" class="pill-btn">Apply</button>
  <a href="${pageContext.request.contextPath}/bookings" class="pill-btn pill-btn-outline">Reset</a>
</form>

<details class="crud-drawer" id="bookingFormDrawer" ${(not empty editing || preselectedRentalId > 0) ? 'open' : ''}>
  <summary>
    <span>${not empty editing ? 'Edit Reservation #' : 'Reserve Expedition Rental & Optional Local Guide'}<c:if test="${not empty editing}">${editing.booking_code}</c:if></span>
    <span class="tag">${not empty editing ? 'Update Mode' : 'Click to reserve'}</span>
  </summary>
  <form action="${pageContext.request.contextPath}/bookings/save" method="post" style="margin-top:18px;display:flex;flex-direction:column;gap:14px">
    <c:if test="${not empty editing}">
      <input type="hidden" name="id" value="${editing.id}">
    </c:if>
    <div class="form-grid-2">
      <div class="field">
        <label>Select Expedition Rental</label>
        <select name="rental_id" class="select" required>
          <c:forEach var="rn" items="${rentals}">
            <option value="${rn.id}" ${(editing.rental_id == rn.id || preselectedRentalId == rn.id) ? 'selected' : ''}>
              <c:out value="${rn.title}"/> (<c:out value="${rn.destination_name}"/> — Rp <fmt:formatNumber value="${rn.price_per_day_idr}" groupingUsed="true"/>/day)
            </option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label>Pair with Certified Local Guide (Optional)</label>
        <select name="guide_id" class="select">
          <option value="0">Self-Guided (No Guide Needed)</option>
          <c:forEach var="g" items="${guides}">
            <option value="${g.id}" ${editing.guide_id == g.id ? 'selected' : ''}>
              <c:out value="${g.full_name}"/> — <c:out value="${g.specialty}"/> (+Rp <fmt:formatNumber value="${g.daily_rate_idr}" groupingUsed="true"/>/day)
            </option>
          </c:forEach>
        </select>
      </div>
    </div>

    <div class="form-grid-3">
      <div class="field">
        <label>Start Date</label>
        <input type="date" name="start_date" class="input" value="<c:out value='${not empty editing ? editing.start_date : "2026-12-15"}'/>" required>
      </div>
      <div class="field">
        <label>End Date</label>
        <input type="date" name="end_date" class="input" value="<c:out value='${not empty editing ? editing.end_date : "2026-12-17"}'/>" required>
      </div>
      <div class="field">
        <label>Guests</label>
        <input type="number" name="guests_count" class="input" value="${not empty editing ? editing.guests_count : 2}" min="1" max="15">
      </div>
    </div>

    <c:if test="${not empty editing}">
      <div class="field" style="max-width:220px">
        <label>Booking Status</label>
        <select name="status" class="select">
          <option value="Pending" ${editing.status == 'Pending' ? 'selected' : ''}>Pending</option>
          <option value="Confirmed" ${editing.status == 'Confirmed' ? 'selected' : ''}>Confirmed</option>
          <option value="Completed" ${editing.status == 'Completed' ? 'selected' : ''}>Completed</option>
          <option value="Cancelled" ${editing.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
        </select>
      </div>
    </c:if>

    <div class="field">
      <label>Pickup Point / Dietary &amp; Gear Requests</label>
      <textarea name="special_requests" class="textarea" placeholder="Harbor pickup time, dietary preferences, jacket sizes..."><c:out value="${editing.special_requests}"/></textarea>
    </div>

    <div style="display:flex;gap:10px;justify-content:flex-end">
      <c:if test="${not empty editing}"><a href="${pageContext.request.contextPath}/bookings" class="pill-btn pill-btn-outline">Cancel</a></c:if>
      <button type="submit" class="pill-btn">${not empty editing ? 'Update Reservation' : 'Submit Reservation'}</button>
    </div>
  </form>
</details>

<div class="panel">
  <div class="table-wrap">
    <table class="data-table">
      <thead>
        <tr>
          <th>Code &amp; Traveler</th>
          <th>Rental &amp; Destination</th>
          <th>Dates &amp; Guide</th>
          <th>Total (IDR)</th>
          <th>Status</th>
          <th style="text-align:right">Actions</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="b" items="${bookings}">
          <tr>
            <td>
              <strong><c:out value="${b.booking_code}"/></strong>
              <div style="font-size:12px;color:var(--muted)"><c:out value="${b.guest_name}"/></div>
            </td>
            <td>
              <strong><c:out value="${b.rental_title}"/></strong>
              <div style="font-size:12px;color:var(--muted)"><c:out value="${b.destination_name}"/> · <c:out value="${b.guests_count}"/> guests</div>
            </td>
            <td>
              <div><c:out value="${b.start_date}"/> → <c:out value="${b.end_date}"/></div>
              <div style="font-size:12px;color:var(--muted)">
                Guide: <c:out value="${not empty b.guide_name ? b.guide_name : 'Self-guided'}"/>
              </div>
            </td>
            <td><strong>Rp <fmt:formatNumber value="${b.total_price_idr}" groupingUsed="true"/></strong></td>
            <td>
              <form action="${pageContext.request.contextPath}/bookings/status" method="post" style="display:flex;gap:6px;align-items:center">
                <input type="hidden" name="id" value="${b.id}">
                <select name="status" class="select" style="padding:4px 8px;font-size:12px;width:118px">
                  <option value="Pending" ${b.status == 'Pending' ? 'selected' : ''}>Pending</option>
                  <option value="Confirmed" ${b.status == 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                  <option value="Completed" ${b.status == 'Completed' ? 'selected' : ''}>Completed</option>
                  <option value="Cancelled" ${b.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                </select>
                <button type="submit" class="pill-btn pill-btn-outline pill-btn-sm">Set</button>
              </form>
            </td>
            <td style="text-align:right">
              <div style="display:inline-flex;gap:6px">
                <a href="${pageContext.request.contextPath}/bookings?edit_id=${b.id}" class="pill-btn pill-btn-outline pill-btn-sm">Edit</a>
                <form action="${pageContext.request.contextPath}/bookings/delete" method="post" onsubmit="return confirm('Cancel and delete this booking?');">
                  <input type="hidden" name="id" value="${b.id}">
                  <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty bookings}">
          <tr><td colspan="6" style="text-align:center;color:var(--muted);padding:28px">No bookings match this filter yet.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<%@ include file="../partials/footer.jsp" %>
