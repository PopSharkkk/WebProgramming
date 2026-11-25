<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 04 · Local Guides Directory CRUD</div>
    <h1 class="page-title">Certified Indonesian Local Guides</h1>
    <p class="page-lead">Licensed volcano mountaineers, Komodo marine rangers, and heritage archaeologists across Indonesia.</p>
  </div>
  <a href="#guideFormDrawer" onclick="document.getElementById('guideFormDrawer').open = true" class="pill-btn">+ Register Local Guide</a>
</div>

<form action="${pageContext.request.contextPath}/guides" method="get" class="filter-strip">
  <div class="field" style="min-width:220px">
    <label>Search Name, Specialty, or Language</label>
    <input type="text" name="search" class="input" placeholder="e.g. Volcano, English, Japanese" value="<c:out value='${search}'/>">
  </div>
  <div class="field">
    <label>Home Destination</label>
    <select name="destination_id" class="select">
      <option value="0">All Destinations</option>
      <c:forEach var="d" items="${destinations}">
        <option value="${d.id}" ${selectedDestId == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
      </c:forEach>
    </select>
  </div>
  <button type="submit" class="pill-btn">Filter Guides</button>
  <a href="${pageContext.request.contextPath}/guides" class="pill-btn pill-btn-outline">Reset</a>
</form>

<details class="crud-drawer" id="guideFormDrawer" ${not empty editing ? 'open' : ''}>
  <summary>
    <span>${not empty editing ? 'Edit Local Guide Profile' : 'Register New Local Guide'}</span>
    <span class="tag">${not empty editing ? 'Update Mode' : 'Click to expand'}</span>
  </summary>
  <form action="${pageContext.request.contextPath}/guides/save" method="post" style="margin-top:18px;display:flex;flex-direction:column;gap:14px">
    <c:if test="${not empty editing}">
      <input type="hidden" name="id" value="${editing.id}">
    </c:if>
    <div class="form-grid-3">
      <div class="field">
        <label>Guide Full Name</label>
        <input type="text" name="full_name" class="input" value="<c:out value='${editing.full_name}'/>" placeholder="e.g. Wayan Arimbawa" required>
      </div>
      <div class="field">
        <label>Specialty</label>
        <input type="text" name="specialty" class="input" value="<c:out value='${editing.specialty}'/>" placeholder="e.g. Volcano Trekking &amp; Botany" required>
      </div>
      <div class="field">
        <label>Assigned Destination</label>
        <select name="destination_id" class="select">
          <c:forEach var="d" items="${destinations}">
            <option value="${d.id}" ${editing.destination_id == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
          </c:forEach>
        </select>
      </div>
    </div>

    <div class="form-grid-3">
      <div class="field">
        <label>Spoken Languages</label>
        <input type="text" name="languages" class="input" value="<c:out value='${not empty editing ? editing.languages : "Indonesian, English"}'/>">
      </div>
      <div class="field">
        <label>Years Experience</label>
        <input type="number" name="years_experience" class="input" value="${not empty editing ? editing.years_experience : 5}" min="1" max="40">
      </div>
      <div class="field">
        <label>Daily Rate (IDR)</label>
        <input type="number" name="daily_rate_idr" class="input" value="${not empty editing ? editing.daily_rate_idr : 550000}" min="100000">
      </div>
    </div>

    <div class="form-grid-2">
      <div class="field">
        <label>HPI License Number</label>
        <input type="text" name="license_number" class="input" value="<c:out value='${editing.license_number}'/>" placeholder="HPI-ID-2026-011">
      </div>
      <div class="field">
        <label>Portrait / Field Image URL</label>
        <input type="url" name="photo_url" class="input" value="<c:out value='${not empty editing ? editing.photo_url : "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80"}'/>">
      </div>
    </div>

    <div class="field">
      <label>Guide Bio</label>
      <textarea name="bio" class="textarea"><c:out value="${editing.bio}"/></textarea>
    </div>

    <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px">
      <label style="display:inline-flex;align-items:center;gap:8px;font-size:13px;font-weight:600">
        <input type="checkbox" name="is_verified" value="1" ${(empty editing || editing.is_verified == 1) ? 'checked' : ''}> Verified HPI License
      </label>
      <div style="display:flex;gap:10px">
        <c:if test="${not empty editing}"><a href="${pageContext.request.contextPath}/guides" class="pill-btn pill-btn-outline">Cancel</a></c:if>
        <button type="submit" class="pill-btn">${not empty editing ? 'Update Guide' : 'Register Guide'}</button>
      </div>
    </div>
  </form>
</details>

<div class="card-grid">
  <c:forEach var="g" items="${guides}">
    <article class="editorial-card">
      <div class="card-media">
        <img src="${g.photo_url}" alt="<c:out value='${g.full_name}'/>" loading="lazy" style="object-position:center 20%">
      </div>
      <div class="card-body">
        <div class="card-meta">
          <span><c:out value="${g.license_number}"/></span>
          <form action="${pageContext.request.contextPath}/guides/toggle" method="post" style="display:inline">
            <input type="hidden" name="id" value="${g.id}">
            <button type="submit" class="tag ${g.is_verified == 1 ? 'tag-emerald' : 'tag-amber'}" style="cursor:pointer">
              ${g.is_verified == 1 ? '✓ Verified HPI' : 'Pending Check'}
            </button>
          </form>
        </div>
        <h2 class="card-title"><c:out value="${g.full_name}"/></h2>
        <div style="font-size:12.5px;color:var(--muted)"><c:out value="${g.destination_name}"/> · <c:out value="${g.years_experience}"/> yrs experience</div>
        <p class="card-text"><strong><c:out value="${g.specialty}"/></strong><br><c:out value="${g.bio}"/><br><span style="font-size:12px;color:var(--muted)">Languages: <c:out value="${g.languages}"/></span></p>
        <div class="card-footer">
          <strong>Rp <fmt:formatNumber value="${g.daily_rate_idr}" groupingUsed="true"/> / day</strong>
          <div style="display:flex;gap:6px">
            <a href="${pageContext.request.contextPath}/guides?edit_id=${g.id}" class="pill-btn pill-btn-outline pill-btn-sm">Edit</a>
            <form action="${pageContext.request.contextPath}/guides/delete" method="post" onsubmit="return confirm('Remove this guide?');">
              <input type="hidden" name="id" value="${g.id}">
              <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
            </form>
          </div>
        </div>
      </div>
    </article>
  </c:forEach>
</div>

<%@ include file="../partials/footer.jsp" %>
