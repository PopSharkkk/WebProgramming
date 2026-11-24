<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 08 · Personal Bucket-List CRUD</div>
    <h1 class="page-title">Saved Archipelago Places</h1>
    <p class="page-lead">Organize Indonesian islands you want to explore next, assign seasonal targets, and keep private planning notes.</p>
  </div>
  <a href="${pageContext.request.contextPath}/destinations" class="pill-btn">Browse More Destinations</a>
</div>

<!-- Quick Add Destination to Bucket List -->
<form action="${pageContext.request.contextPath}/saved/toggle" method="post" class="filter-strip">
  <div class="field" style="max-width:360px">
    <label>Quick-Add Destination to Bucket List</label>
    <select name="destination_id" class="select">
      <c:forEach var="d" items="${destinations}">
        <option value="${d.id}"><c:out value="${d.name}"/> (<c:out value="${d.province}"/>)</option>
      </c:forEach>
    </select>
  </div>
  <input type="hidden" name="redirect" value="/saved">
  <button type="submit" class="pill-btn">Add / Toggle Bookmark</button>
</form>

<div class="card-grid-2">
  <c:forEach var="sp" items="${savedPlaces}">
    <article class="editorial-card">
      <div class="card-media">
        <img src="${sp.hero_image}" alt="<c:out value='${sp.destination_name}'/>" loading="lazy">
      </div>
      <div class="card-body">
        <div class="card-meta">
          <span class="tag tag-dark"><c:out value="${sp.priority_level}"/></span>
          <span><c:out value="${sp.region_name}"/> · <c:out value="${sp.province}"/></span>
        </div>
        <h2 class="card-title"><c:out value="${sp.destination_name}"/></h2>

        <form action="${pageContext.request.contextPath}/saved/update" method="post" style="display:flex;flex-direction:column;gap:10px;margin-top:6px">
          <input type="hidden" name="id" value="${sp.id}">
          <div class="form-grid-2">
            <div class="field">
              <label>Target Season</label>
              <input type="text" name="target_season" class="input" value="<c:out value='${sp.target_season}'/>">
            </div>
            <div class="field">
              <label>Priority</label>
              <select name="priority_level" class="select">
                <option value="Must Visit" ${sp.priority_level == 'Must Visit' ? 'selected' : ''}>Must Visit</option>
                <option value="Next Trip" ${sp.priority_level == 'Next Trip' ? 'selected' : ''}>Next Trip</option>
                <option value="Dream List" ${sp.priority_level == 'Dream List' ? 'selected' : ''}>Dream List</option>
              </select>
            </div>
          </div>
          <div class="field">
            <label>Personal Planning Note</label>
            <textarea name="personal_note" class="textarea" style="min-height:64px"><c:out value="${sp.personal_note}"/></textarea>
          </div>
          <div class="card-footer">
            <button type="submit" class="pill-btn pill-btn-sm">Save Note</button>
            <div style="display:flex;gap:8px">
              <a href="${pageContext.request.contextPath}/destinations/view?id=${sp.destination_id}" class="pill-btn pill-btn-outline pill-btn-sm">Open Guide</a>
            </div>
          </div>
        </form>

        <form action="${pageContext.request.contextPath}/saved/delete" method="post" style="margin-top:8px" onsubmit="return confirm('Remove from your bucket list?');">
          <input type="hidden" name="id" value="${sp.id}">
          <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm" style="width:100%">Remove from Saved List</button>
        </form>
      </div>
    </article>
  </c:forEach>
  <c:if test="${empty savedPlaces}">
    <div class="panel" style="grid-column:1/-1;text-align:center;padding:36px">
      <p style="color:var(--muted)">Your bucket list is empty. Save any destination above or from the <a href="${pageContext.request.contextPath}/destinations" style="font-weight:650">Destinations Guide</a>.</p>
    </div>
  </c:if>
</div>

<%@ include file="../partials/footer.jsp" %>
