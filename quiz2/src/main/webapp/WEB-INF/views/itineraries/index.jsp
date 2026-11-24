<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 05 · Nusantara Trip Itineraries CRUD</div>
    <h1 class="page-title">Indonesian Multi-Day Trip Plans</h1>
    <p class="page-lead">Curated archipelago loops across volcanic highlands, rail corridors, and marine passages.</p>
  </div>
  <a href="#itineraryFormDrawer" onclick="document.getElementById('itineraryFormDrawer').open = true" class="pill-btn">+ Plan New Itinerary</a>
</div>

<form action="${pageContext.request.contextPath}/itineraries" method="get" class="filter-strip">
  <div class="field">
    <label>Region</label>
    <select name="region_id" class="select">
      <option value="0">All Regions</option>
      <c:forEach var="r" items="${regions}">
        <option value="${r.id}" ${selectedRegionId == r.id ? 'selected' : ''}><c:out value="${r.name}"/></option>
      </c:forEach>
    </select>
  </div>
  <div class="field">
    <label>Travel Pace</label>
    <select name="pace" class="select">
      <option value="">All Paces</option>
      <option value="Active Explorer" ${selectedPace == 'Active Explorer' ? 'selected' : ''}>Active Explorer</option>
      <option value="Sailing &amp; Island Hop" ${selectedPace == 'Sailing & Island Hop' ? 'selected' : ''}>Sailing &amp; Island Hop</option>
      <option value="Slow Culture" ${selectedPace == 'Slow Culture' ? 'selected' : ''}>Slow Culture</option>
      <option value="Remote Expedition" ${selectedPace == 'Remote Expedition' ? 'selected' : ''}>Remote Expedition</option>
      <option value="Budget Heritage" ${selectedPace == 'Budget Heritage' ? 'selected' : ''}>Budget Heritage</option>
    </select>
  </div>
  <div class="field" style="max-width:180px">
    <label>Max Budget (IDR)</label>
    <input type="number" name="max_budget" class="input" placeholder="10000000" value="<c:out value='${maxBudget}'/>">
  </div>
  <button type="submit" class="pill-btn">Filter Plans</button>
  <a href="${pageContext.request.contextPath}/itineraries" class="pill-btn pill-btn-outline">Reset</a>
</form>

<details class="crud-drawer" id="itineraryFormDrawer" ${not empty editing ? 'open' : ''}>
  <summary>
    <span>${not empty editing ? 'Edit Trip Itinerary' : 'Create Custom Indonesia Itinerary'}</span>
    <span class="tag">${not empty editing ? 'Update Mode' : 'Click to expand'}</span>
  </summary>
  <form action="${pageContext.request.contextPath}/itineraries/save" method="post" style="margin-top:18px;display:flex;flex-direction:column;gap:14px">
    <c:if test="${not empty editing}">
      <input type="hidden" name="id" value="${editing.id}">
    </c:if>
    <div class="form-grid-3">
      <div class="field">
        <label>Itinerary Title</label>
        <input type="text" name="title" class="input" value="<c:out value='${editing.title}'/>" placeholder="e.g. Lombok Rinjani &amp; Gili Coral Loop" required>
      </div>
      <div class="field">
        <label>Region</label>
        <select name="region_id" class="select">
          <c:forEach var="r" items="${regions}">
            <option value="${r.id}" ${editing.region_id == r.id ? 'selected' : ''}><c:out value="${r.name}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label>Pace</label>
        <select name="pace" class="select">
          <option value="Active Explorer" ${editing.pace == 'Active Explorer' ? 'selected' : ''}>Active Explorer</option>
          <option value="Sailing &amp; Island Hop" ${editing.pace == 'Sailing & Island Hop' ? 'selected' : ''}>Sailing &amp; Island Hop</option>
          <option value="Slow Culture" ${editing.pace == 'Slow Culture' ? 'selected' : ''}>Slow Culture</option>
          <option value="Remote Expedition" ${editing.pace == 'Remote Expedition' ? 'selected' : ''}>Remote Expedition</option>
          <option value="Budget Heritage" ${editing.pace == 'Budget Heritage' ? 'selected' : ''}>Budget Heritage</option>
        </select>
      </div>
    </div>

    <div class="form-grid-3">
      <div class="field">
        <label>Duration (Days)</label>
        <input type="number" name="duration_days" class="input" value="${not empty editing ? editing.duration_days : 4}" min="1" max="30">
      </div>
      <div class="field">
        <label>Estimated Budget (IDR)</label>
        <input type="number" name="estimated_budget_idr" class="input" value="${not empty editing ? editing.estimated_budget_idr : 6500000}" min="500000">
      </div>
      <div class="field">
        <label>Cover Image URL (Unsplash)</label>
        <input type="url" name="cover_image" class="input" value="<c:out value='${not empty editing ? editing.cover_image : "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80"}'/>">
      </div>
    </div>

    <div class="field">
      <label>Route Stops</label>
      <input type="text" name="route_stops" class="input" value="<c:out value='${editing.route_stops}'/>" placeholder="e.g. Mataram -> Senaru -> Sembalun -> Gili Air" required>
    </div>

    <div class="field">
      <label>Day-by-Day Stage Breakdown</label>
      <textarea name="day_by_day_plan" class="textarea" placeholder="Day 1: ...&#10;Day 2: ..."><c:out value="${editing.day_by_day_plan}"/></textarea>
    </div>

    <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px">
      <label style="display:inline-flex;align-items:center;gap:8px;font-size:13px;font-weight:600">
        <input type="checkbox" name="is_public" value="1" ${(empty editing || editing.is_public == 1) ? 'checked' : ''}> Publicly Visible to Travelers
      </label>
      <div style="display:flex;gap:10px">
        <c:if test="${not empty editing}"><a href="${pageContext.request.contextPath}/itineraries" class="pill-btn pill-btn-outline">Cancel</a></c:if>
        <button type="submit" class="pill-btn">${not empty editing ? 'Update Itinerary' : 'Publish Itinerary'}</button>
      </div>
    </div>
  </form>
</details>

<div class="card-grid">
  <c:forEach var="it" items="${itineraries}">
    <article class="editorial-card">
      <div class="card-media">
        <img src="${it.cover_image}" alt="<c:out value='${it.title}'/>" loading="lazy">
      </div>
      <div class="card-body">
        <div class="card-meta">
          <span class="tag"><c:out value="${it.region_name}"/></span>
          <span><c:out value="${it.duration_days}"/> Days · <c:out value="${it.pace}"/></span>
        </div>
        <h2 class="card-title"><c:out value="${it.title}"/></h2>
        <div style="font-size:12.5px;color:var(--muted)">Route: <c:out value="${it.route_stops}"/></div>
        <p class="card-text">Est. Budget: <strong>Rp <fmt:formatNumber value="${it.estimated_budget_idr}" groupingUsed="true"/></strong> · Curated by <c:out value="${it.author_name}"/></p>
        <div class="card-footer">
          <a href="${pageContext.request.contextPath}/itineraries/view?id=${it.id}" class="pill-btn pill-btn-sm">View Days</a>
          <div style="display:flex;gap:6px">
            <a href="${pageContext.request.contextPath}/itineraries?edit_id=${it.id}" class="pill-btn pill-btn-outline pill-btn-sm">Edit</a>
            <form action="${pageContext.request.contextPath}/itineraries/delete" method="post" onsubmit="return confirm('Delete this itinerary?');">
              <input type="hidden" name="id" value="${it.id}">
              <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
            </form>
          </div>
        </div>
      </div>
    </article>
  </c:forEach>
</div>

<%@ include file="../partials/footer.jsp" %>
