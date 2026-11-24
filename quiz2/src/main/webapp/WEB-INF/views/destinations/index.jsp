<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 01 · Archipelago Directory CRUD</div>
    <h1 class="page-title">Indonesia Destinations Guide</h1>
    <p class="page-lead">Explore volcanic calderas, UNESCO temple plains, and eastern coral atolls across the Indonesian archipelago.</p>
  </div>
  <a href="#destFormDrawer" onclick="document.getElementById('destFormDrawer').open = true" class="pill-btn">+ Add Destination</a>
</div>

<!-- Multi-Criteria Search & Filter Bar -->
<form action="${pageContext.request.contextPath}/destinations" method="get" class="filter-strip">
  <div class="field" style="min-width:210px">
    <label>Keyword</label>
    <input type="text" name="search" class="input" placeholder="e.g. Bromo, Raja Ampat, Bali" value="<c:out value='${search}'/>">
  </div>
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
    <label>Category</label>
    <select name="category" class="select">
      <option value="">All Categories</option>
      <option value="Volcano &amp; Highland" ${selectedCategory == 'Volcano & Highland' ? 'selected' : ''}>Volcano &amp; Highland</option>
      <option value="Heritage &amp; Temple" ${selectedCategory == 'Heritage & Temple' ? 'selected' : ''}>Heritage &amp; Temple</option>
      <option value="Marine &amp; Reef" ${selectedCategory == 'Marine & Reef' ? 'selected' : ''}>Marine &amp; Reef</option>
      <option value="Marine &amp; Cliff" ${selectedCategory == 'Marine & Cliff' ? 'selected' : ''}>Marine &amp; Cliff</option>
      <option value="Culture &amp; Rice Terrace" ${selectedCategory == 'Culture & Rice Terrace' ? 'selected' : ''}>Culture &amp; Rice Terrace</option>
      <option value="Archipelago &amp; Safari" ${selectedCategory == 'Archipelago & Safari' ? 'selected' : ''}>Archipelago &amp; Safari</option>
    </select>
  </div>
  <div class="field" style="max-width:150px">
    <label>Max Fee (IDR)</label>
    <input type="number" name="max_fee" class="input" placeholder="500000" value="<c:out value='${maxFee}'/>">
  </div>
  <div class="field" style="max-width:160px">
    <label>Sort By</label>
    <select name="sort" class="select">
      <option value="featured" ${selectedSort == 'featured' ? 'selected' : ''}>Featured First</option>
      <option value="fee_asc" ${selectedSort == 'fee_asc' ? 'selected' : ''}>Lowest Entry Fee</option>
      <option value="fee_desc" ${selectedSort == 'fee_desc' ? 'selected' : ''}>Highest Entry Fee</option>
      <option value="name" ${selectedSort == 'name' ? 'selected' : ''}>Alphabetical</option>
    </select>
  </div>
  <button type="submit" class="pill-btn">Apply Filter</button>
  <a href="${pageContext.request.contextPath}/destinations" class="pill-btn pill-btn-outline">Reset</a>
</form>

<!-- Create / Edit Destination Form Drawer -->
<details class="crud-drawer" id="destFormDrawer" ${not empty editing ? 'open' : ''}>
  <summary>
    <span>${not empty editing ? 'Edit Destination Record' : 'Create New Indonesian Destination'}</span>
    <span class="tag">${not empty editing ? 'Update Mode' : 'Click to expand'}</span>
  </summary>
  <form action="${pageContext.request.contextPath}/destinations/save" method="post" style="margin-top:18px;display:flex;flex-direction:column;gap:14px">
    <c:if test="${not empty editing}">
      <input type="hidden" name="id" value="${editing.id}">
    </c:if>
    <div class="form-grid-3">
      <div class="field">
        <label>Destination Name</label>
        <input type="text" name="name" class="input" value="<c:out value='${editing.name}'/>" placeholder="e.g. Lake Toba &amp; Samosir" required>
      </div>
      <div class="field">
        <label>Province</label>
        <input type="text" name="province" class="input" value="<c:out value='${editing.province}'/>" placeholder="e.g. North Sumatra" required>
      </div>
      <div class="field">
        <label>Archipelago Region</label>
        <select name="region_id" class="select">
          <c:forEach var="r" items="${regions}">
            <option value="${r.id}" ${editing.region_id == r.id ? 'selected' : ''}><c:out value="${r.name}"/></option>
          </c:forEach>
        </select>
      </div>
    </div>

    <div class="form-grid-3">
      <div class="field">
        <label>Category</label>
        <select name="category" class="select">
          <option value="Volcano &amp; Highland" ${editing.category == 'Volcano & Highland' ? 'selected' : ''}>Volcano &amp; Highland</option>
          <option value="Heritage &amp; Temple" ${editing.category == 'Heritage & Temple' ? 'selected' : ''}>Heritage &amp; Temple</option>
          <option value="Marine &amp; Reef" ${editing.category == 'Marine & Reef' ? 'selected' : ''}>Marine &amp; Reef</option>
          <option value="Marine &amp; Cliff" ${editing.category == 'Marine & Cliff' ? 'selected' : ''}>Marine &amp; Cliff</option>
          <option value="Culture &amp; Rice Terrace" ${editing.category == 'Culture & Rice Terrace' ? 'selected' : ''}>Culture &amp; Rice Terrace</option>
          <option value="Archipelago &amp; Safari" ${editing.category == 'Archipelago & Safari' ? 'selected' : ''}>Archipelago &amp; Safari</option>
        </select>
      </div>
      <div class="field">
        <label>Entry Fee (IDR)</label>
        <input type="number" name="entry_fee_idr" class="input" value="${not empty editing ? editing.entry_fee_idr : 150000}" min="0">
      </div>
      <div class="field">
        <label>Recommended Days</label>
        <input type="number" name="recommended_days" class="input" value="${not empty editing ? editing.recommended_days : 3}" min="1" max="14">
      </div>
    </div>

    <div class="field">
      <label>Unsplash Photography URL</label>
      <input type="url" name="hero_image" class="input" value="<c:out value='${not empty editing ? editing.hero_image : "https://images.unsplash.com/photo-1539367628448-4bc5c9d171c8?auto=format&fit=crop&w=1200&q=80"}'/>">
    </div>

    <div class="field">
      <label>Short Editorial Summary</label>
      <input type="text" name="summary" class="input" value="<c:out value='${editing.summary}'/>" placeholder="Concise one-sentence landscape highlight..." required>
    </div>

    <div class="form-grid-2">
      <div class="field">
        <label>Field Guide Overview</label>
        <textarea name="guide_overview" class="textarea" placeholder="Practical access notes and terrain details..."><c:out value="${editing.guide_overview}"/></textarea>
      </div>
      <div class="field">
        <label>Travel Tips</label>
        <textarea name="travel_tips" class="textarea" placeholder="Best hour to arrive, gear, local permits..."><c:out value="${editing.travel_tips}"/></textarea>
      </div>
    </div>

    <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px">
      <label style="display:inline-flex;align-items:center;gap:8px;font-size:13px;font-weight:600">
        <input type="checkbox" name="is_featured" value="1" ${editing.is_featured == 1 ? 'checked' : ''}> Spotlight on Home Page
      </label>
      <div style="display:flex;gap:10px">
        <c:if test="${not empty editing}">
          <a href="${pageContext.request.contextPath}/destinations" class="pill-btn pill-btn-outline">Cancel Edit</a>
        </c:if>
        <button type="submit" class="pill-btn">${not empty editing ? 'Update Destination' : 'Publish Destination'}</button>
      </div>
    </div>
  </form>
</details>

<!-- Destinations Grid -->
<div class="card-grid">
  <c:forEach var="d" items="${destinations}">
    <article class="editorial-card">
      <div class="card-media">
        <img src="${d.hero_image}" alt="<c:out value='${d.name}'/>" loading="lazy">
      </div>
      <div class="card-body">
        <div class="card-meta">
          <span><c:out value="${d.region_name}"/> · <c:out value="${d.province}"/></span>
          <c:if test="${d.is_featured == 1}"><span class="tag tag-dark">Featured</span></c:if>
        </div>
        <h2 class="card-title"><c:out value="${d.name}"/></h2>
        <p class="card-text"><c:out value="${d.summary}"/></p>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <span class="tag"><c:out value="${d.category}"/></span>
          <span class="tag">Rp <fmt:formatNumber value="${d.entry_fee_idr}" groupingUsed="true"/></span>
          <span class="tag"><c:out value="${d.recommended_days}"/> Days</span>
        </div>
        <div class="card-footer">
          <a href="${pageContext.request.contextPath}/destinations/view?id=${d.id}" class="pill-btn pill-btn-sm">Read Guide</a>
          <div style="display:flex;gap:6px;flex-wrap:wrap">
            <form action="${pageContext.request.contextPath}/saved/toggle" method="post" style="display:inline">
              <input type="hidden" name="destination_id" value="${d.id}">
              <input type="hidden" name="redirect" value="/destinations">
              <button type="submit" class="pill-btn pill-btn-outline pill-btn-sm">Save</button>
            </form>
            <a href="${pageContext.request.contextPath}/destinations?edit_id=${d.id}" class="pill-btn pill-btn-outline pill-btn-sm">Edit</a>
            <form action="${pageContext.request.contextPath}/destinations/delete" method="post" style="display:inline" onsubmit="return confirm('Delete this destination?');">
              <input type="hidden" name="id" value="${d.id}">
              <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
            </form>
          </div>
        </div>
      </div>
    </article>
  </c:forEach>
</div>

<%@ include file="../partials/footer.jsp" %>
