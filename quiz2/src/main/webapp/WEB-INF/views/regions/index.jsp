<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 02 · Archipelago Regions CRUD</div>
    <h1 class="page-title">Indonesian Island Regions &amp; Seasons</h1>
    <p class="page-lead">Compare monsoon windows, island groups, and regional terrain summaries before routing your Indonesian trip.</p>
  </div>
  <a href="#regionFormDrawer" onclick="document.getElementById('regionFormDrawer').open = true" class="pill-btn">+ Add Island Region</a>
</div>

<details class="crud-drawer" id="regionFormDrawer" ${not empty editing ? 'open' : ''}>
  <summary>
    <span>${not empty editing ? 'Edit Island Region' : 'Create New Archipelago Region'}</span>
    <span class="tag">${not empty editing ? 'Update Mode' : 'Click to expand'}</span>
  </summary>
  <form action="${pageContext.request.contextPath}/regions/save" method="post" style="margin-top:18px;display:flex;flex-direction:column;gap:14px">
    <c:if test="${not empty editing}">
      <input type="hidden" name="id" value="${editing.id}">
    </c:if>
    <div class="form-grid-3">
      <div class="field">
        <label>Region Name</label>
        <input type="text" name="name" class="input" value="<c:out value='${editing.name}'/>" placeholder="e.g. Sulawesi Highlands &amp; Togean" required>
      </div>
      <div class="field">
        <label>Region Code</label>
        <input type="text" name="code" class="input" value="<c:out value='${editing.code}'/>" placeholder="e.g. ID-SULAWESI" required>
      </div>
      <div class="field">
        <label>Island Group</label>
        <input type="text" name="island_group" class="input" value="<c:out value='${editing.island_group}'/>" placeholder="e.g. Central Archipelago" required>
      </div>
    </div>

    <div class="form-grid-2">
      <div class="field">
        <label>Best Travel Season</label>
        <input type="text" name="best_season" class="input" value="<c:out value='${editing.best_season}'/>" placeholder="e.g. May - October (Dry)">
      </div>
      <div class="field">
        <label>Cover Image URL (Unsplash)</label>
        <input type="url" name="cover_image" class="input" value="<c:out value='${not empty editing ? editing.cover_image : "https://images.unsplash.com/photo-1518548419970-58e3b4079ab2?auto=format&fit=crop&w=1200&q=80"}'/>">
      </div>
    </div>

    <div class="form-grid-2">
      <div class="field">
        <label>Climate &amp; Sea Notes</label>
        <textarea name="climate_notes" class="textarea"><c:out value="${editing.climate_notes}"/></textarea>
      </div>
      <div class="field">
        <label>Editorial Summary</label>
        <textarea name="summary" class="textarea" required><c:out value="${editing.summary}"/></textarea>
      </div>
    </div>

    <div style="display:flex;gap:10px;justify-content:flex-end">
      <c:if test="${not empty editing}">
        <a href="${pageContext.request.contextPath}/regions" class="pill-btn pill-btn-outline">Cancel</a>
      </c:if>
      <button type="submit" class="pill-btn">${not empty editing ? 'Update Region' : 'Save Region'}</button>
    </div>
  </form>
</details>

<div class="card-grid-2">
  <c:forEach var="r" items="${regions}">
    <article class="editorial-card">
      <div class="card-media">
        <img src="${r.cover_image}" alt="<c:out value='${r.name}'/>" loading="lazy">
      </div>
      <div class="card-body">
        <div class="card-meta">
          <span class="tag tag-dark"><c:out value="${r.code}"/></span>
          <span><c:out value="${r.island_group}"/></span>
        </div>
        <h2 class="card-title"><c:out value="${r.name}"/></h2>
        <p class="card-text"><c:out value="${r.summary}"/></p>
        <div style="background:var(--paper);padding:12px 14px;border-radius:12px;font-size:12.5px;color:var(--ink-2)">
          <strong>Best Window:</strong> <c:out value="${r.best_season}"/><br>
          <strong>Climate Notes:</strong> <c:out value="${r.climate_notes}"/>
        </div>
        <div class="card-footer">
          <a href="${pageContext.request.contextPath}/destinations?region_id=${r.id}" style="font-size:13px;font-weight:650">
            <c:out value="${r.dest_count}"/> Destinations · <c:out value="${r.itinerary_count}"/> Itineraries →
          </a>
          <div style="display:flex;gap:6px">
            <a href="${pageContext.request.contextPath}/regions?edit_id=${r.id}" class="pill-btn pill-btn-outline pill-btn-sm">Edit</a>
            <form action="${pageContext.request.contextPath}/regions/delete" method="post" onsubmit="return confirm('Delete region and cascade destinations?');">
              <input type="hidden" name="id" value="${r.id}">
              <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
            </form>
          </div>
        </div>
      </div>
    </article>
  </c:forEach>
</div>

<%@ include file="../partials/footer.jsp" %>
