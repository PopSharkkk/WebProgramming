<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Quality Control &amp; Telemetry · Fakhrian Elanta</div>
    <h1 class="page-title">QA Registered Accounts Inspector</h1>
    <p class="page-lead">Track all registered travelers &amp; curators in real time, inspect database activity metrics, and 1-click switch active sessions.</p>
  </div>
  <span class="tag tag-emerald">Engine: <c:out value="${dbEngine}"/></span>
</div>

<div class="stat-strip">
  <div class="stat-box">
    <div class="stat-value"><c:out value="${counts.users}"/></div>
    <div class="stat-cap">Registered Accounts</div>
  </div>
  <div class="stat-box">
    <div class="stat-value"><c:out value="${counts.destinations}"/></div>
    <div class="stat-cap">Indonesian Destinations</div>
  </div>
  <div class="stat-box">
    <div class="stat-value"><c:out value="${counts.bookings}"/></div>
    <div class="stat-cap">Rental Bookings</div>
  </div>
  <div class="stat-box">
    <div class="stat-value"><c:out value="${counts.travel_stories}"/></div>
    <div class="stat-cap">Field Stories</div>
  </div>
</div>

<form action="${pageContext.request.contextPath}/debug/accounts" method="get" class="filter-strip">
  <div style="display:flex;gap:8px;flex-wrap:wrap">
    <a href="${pageContext.request.contextPath}/debug/accounts" class="pill-btn ${empty roleFilter ? '' : 'pill-btn-outline'} pill-btn-sm">All Roles</a>
    <a href="${pageContext.request.contextPath}/debug/accounts?role=curator" class="pill-btn ${roleFilter == 'curator' ? '' : 'pill-btn-outline'} pill-btn-sm">Curators</a>
    <a href="${pageContext.request.contextPath}/debug/accounts?role=traveler" class="pill-btn ${roleFilter == 'traveler' ? '' : 'pill-btn-outline'} pill-btn-sm">Travelers</a>
  </div>
  <div class="field" style="max-width:280px;margin-left:auto">
    <input type="text" name="search" class="input" placeholder="Search name, email, nationality..." value="<c:out value='${search}'/>">
  </div>
  <button type="submit" class="pill-btn pill-btn-sm">Search</button>
</form>

<div class="panel">
  <div class="table-wrap">
    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Account Identity</th>
          <th>Role</th>
          <th>Nationality &amp; Contact</th>
          <th>Activity Telemetry</th>
          <th style="text-align:right">QA Actions</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="u" items="${users}">
          <tr>
            <td><strong>#<c:out value="${u.id}"/></strong></td>
            <td>
              <strong><c:out value="${u.full_name}"/></strong>
              <c:if test="${sessionScope.currentUser.id == u.id}">
                <span class="tag tag-emerald">Current Session</span>
              </c:if>
              <div style="font-size:12px;color:var(--muted);font-family:monospace"><c:out value="${u.email}"/></div>
            </td>
            <td>
              <span class="tag ${u.role == 'curator' ? 'tag-dark' : ''}">
                <c:out value="${u.role}"/>
              </span>
            </td>
            <td>
              <div><c:out value="${u.nationality}"/></div>
              <div style="font-size:12px;color:var(--muted)"><c:out value="${u.phone}"/></div>
            </td>
            <td style="font-size:12.5px">
              <c:out value="${u.bookings_count}"/> bookings ·
              <c:out value="${u.itineraries_count}"/> itineraries ·
              <c:out value="${u.stories_count}"/> stories ·
              <c:out value="${u.saved_count}"/> saved
            </td>
            <td style="text-align:right">
              <div style="display:inline-flex;gap:6px">
                <form action="${pageContext.request.contextPath}/debug/accounts/switch" method="post">
                  <input type="hidden" name="user_id" value="${u.id}">
                  <button type="submit" class="pill-btn pill-btn-sm">Switch Session</button>
                </form>
                <c:if test="${u.id > 6}">
                  <form action="${pageContext.request.contextPath}/debug/accounts/delete" method="post" onsubmit="return confirm('Delete test account?');">
                    <input type="hidden" name="user_id" value="${u.id}">
                    <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
                  </form>
                </c:if>
              </div>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<%@ include file="../partials/footer.jsp" %>
