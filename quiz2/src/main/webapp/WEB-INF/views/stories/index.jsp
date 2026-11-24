<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 07 · Field Stories &amp; Reviews CRUD</div>
    <h1 class="page-title">Archipelago Field Stories</h1>
    <p class="page-lead">Real Indonesian trail logs, seasonal timing insights, and honest notes published by community explorers.</p>
  </div>
  <a href="#storyFormDrawer" onclick="document.getElementById('storyFormDrawer').open = true" class="pill-btn">+ Publish Field Story</a>
</div>

<form action="${pageContext.request.contextPath}/stories" method="get" class="filter-strip">
  <div class="field" style="max-width:300px">
    <label>Filter by Destination</label>
    <select name="destination_id" class="select">
      <option value="0">All Destinations</option>
      <c:forEach var="d" items="${destinations}">
        <option value="${d.id}" ${selectedDestId == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
      </c:forEach>
    </select>
  </div>
  <button type="submit" class="pill-btn">Filter Stories</button>
  <a href="${pageContext.request.contextPath}/stories" class="pill-btn pill-btn-outline">Reset</a>
</form>

<details class="crud-drawer" id="storyFormDrawer" ${not empty editing ? 'open' : ''}>
  <summary>
    <span>${not empty editing ? 'Edit Field Story' : 'Write a New Travel Story'}</span>
    <span class="tag">${not empty editing ? 'Update Mode' : 'Click to write'}</span>
  </summary>
  <form action="${pageContext.request.contextPath}/stories/save" method="post" style="margin-top:18px;display:flex;flex-direction:column;gap:14px">
    <c:if test="${not empty editing}">
      <input type="hidden" name="id" value="${editing.id}">
    </c:if>
    <div class="form-grid-3">
      <div class="field">
        <label>Story Headline</label>
        <input type="text" name="title" class="input" value="<c:out value='${editing.title}'/>" placeholder="e.g. Sunrise Mist on Padar Ridge" required>
      </div>
      <div class="field">
        <label>Destination</label>
        <select name="destination_id" class="select">
          <c:forEach var="d" items="${destinations}">
            <option value="${d.id}" ${editing.destination_id == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label>Rating (1-5)</label>
        <select name="rating" class="select">
          <option value="5" ${editing.rating == 5 ? 'selected' : ''}>5 ★ Unforgettable</option>
          <option value="4" ${editing.rating == 4 ? 'selected' : ''}>4 ★ Great</option>
          <option value="3" ${editing.rating == 3 ? 'selected' : ''}>3 ★ Solid</option>
        </select>
      </div>
    </div>

    <div class="form-grid-2">
      <div class="field">
        <label>Visited Month</label>
        <input type="text" name="visited_month" class="input" value="<c:out value='${not empty editing ? editing.visited_month : "November 2026"}'/>">
      </div>
      <div class="field">
        <label>Photo URL (Unsplash)</label>
        <input type="url" name="photo_url" class="input" value="<c:out value='${not empty editing ? editing.photo_url : "https://images.unsplash.com/photo-1576363399501-0dd08fc750e1?auto=format&fit=crop&w=1200&q=80"}'/>">
      </div>
    </div>

    <div class="field">
      <label>Field Narrative</label>
      <textarea name="story_body" class="textarea" required><c:out value="${editing.story_body}"/></textarea>
    </div>

    <div class="field">
      <label>One Key Travel Tip</label>
      <input type="text" name="travel_tip" class="input" value="<c:out value='${editing.travel_tip}'/>" placeholder="Best advice for fellow explorers...">
    </div>

    <div style="display:flex;gap:10px;justify-content:flex-end">
      <c:if test="${not empty editing}"><a href="${pageContext.request.contextPath}/stories" class="pill-btn pill-btn-outline">Cancel</a></c:if>
      <button type="submit" class="pill-btn">${not empty editing ? 'Update Story' : 'Publish Story'}</button>
    </div>
  </form>
</details>

<div class="card-grid-2">
  <c:forEach var="s" items="${stories}">
    <article class="editorial-card">
      <div class="card-media">
        <img src="${s.photo_url}" alt="<c:out value='${s.title}'/>" loading="lazy">
      </div>
      <div class="card-body">
        <div class="card-meta">
          <span class="tag"><c:out value="${s.destination_name}"/></span>
          <span>★ <c:out value="${s.rating}"/> / 5 · <c:out value="${s.visited_month}"/></span>
        </div>
        <h2 class="card-title"><c:out value="${s.title}"/></h2>
        <div style="font-size:12px;color:var(--muted)">By <c:out value="${s.author_name}"/> · <c:out value="${s.province}"/></div>
        <p class="card-text"><c:out value="${s.story_body}"/></p>
        <c:if test="${not empty s.travel_tip}">
          <div style="background:var(--paper);padding:10px 12px;border-radius:10px;font-size:12.5px">
            <strong>Field Tip:</strong> <c:out value="${s.travel_tip}"/>
          </div>
        </c:if>
        <div class="card-footer">
          <a href="${pageContext.request.contextPath}/destinations/view?id=${s.destination_id}" style="font-size:12.5px;font-weight:650">View Destination →</a>
          <div style="display:flex;gap:6px">
            <a href="${pageContext.request.contextPath}/stories?edit_id=${s.id}" class="pill-btn pill-btn-outline pill-btn-sm">Edit</a>
            <form action="${pageContext.request.contextPath}/stories/delete" method="post" onsubmit="return confirm('Delete this story?');">
              <input type="hidden" name="id" value="${s.id}">
              <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
            </form>
          </div>
        </div>
      </div>
    </article>
  </c:forEach>
</div>

<%@ include file="../partials/footer.jsp" %>
