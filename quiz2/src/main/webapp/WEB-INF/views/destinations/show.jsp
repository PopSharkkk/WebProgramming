<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div style="margin-bottom:18px">
  <a href="${pageContext.request.contextPath}/destinations" class="pill-btn pill-btn-outline pill-btn-sm">← Back to Destinations</a>
</div>

<div class="panel" style="padding:0;overflow:hidden;margin-bottom:28px">
  <div style="aspect-ratio:21/9;width:100%;background:#d8d3cf;overflow:hidden">
    <img src="${dest.hero_image}" alt="<c:out value='${dest.name}'/>" style="width:100%;height:100%;object-fit:cover;display:block">
  </div>
  <div style="padding:28px">
    <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:18px;flex-wrap:wrap">
      <div>
        <div class="kicker"><c:out value="${dest.region_name}"/> · <c:out value="${dest.province}"/> · Best Season: <c:out value="${dest.best_season}"/></div>
        <h1 class="page-title"><c:out value="${dest.name}"/></h1>
      </div>
      <div style="display:flex;gap:8px;flex-wrap:wrap">
        <form action="${pageContext.request.contextPath}/saved/toggle" method="post">
          <input type="hidden" name="destination_id" value="${dest.id}">
          <input type="hidden" name="redirect" value="/destinations/view?id=${dest.id}">
          <button type="submit" class="pill-btn">Save to Bucket List</button>
        </form>
        <a href="${pageContext.request.contextPath}/destinations?edit_id=${dest.id}" class="pill-btn pill-btn-outline">Edit Entry</a>
      </div>
    </div>

    <div style="display:flex;gap:10px;flex-wrap:wrap;margin:16px 0 20px">
      <span class="tag tag-dark"><c:out value="${dest.category}"/></span>
      <span class="tag">Park / Entry Fee: Rp <fmt:formatNumber value="${dest.entry_fee_idr}" groupingUsed="true"/></span>
      <span class="tag">Recommended Stay: <c:out value="${dest.recommended_days}"/> Days</span>
    </div>

    <div class="card-grid-2" style="margin-top:16px">
      <div>
        <h2 style="font-size:17px;font-weight:700;margin-bottom:8px">Field Overview</h2>
        <p style="font-size:14.5px;color:var(--ink-2);line-height:1.65"><c:out value="${dest.guide_overview}"/></p>
      </div>
      <div style="background:var(--paper);border-radius:14px;padding:18px">
        <h2 style="font-size:16px;font-weight:700;margin-bottom:8px">Practical Travel Tips</h2>
        <p style="font-size:14px;color:var(--ink-2);line-height:1.6"><c:out value="${dest.travel_tips}"/></p>
      </div>
    </div>
  </div>
</div>

<!-- Wild Rentals at this Destination -->
<h2 style="font-size:22px;font-weight:800;margin-bottom:14px">Available Wild Expedition Rentals Here</h2>
<div class="card-grid" style="margin-bottom:34px">
  <c:forEach var="rn" items="${rentals}">
    <article class="editorial-card">
      <div class="card-body">
        <div class="card-meta"><span><c:out value="${rn.rental_type}"/></span><span>Up to <c:out value="${rn.capacity_guests}"/> guests</span></div>
        <h3 class="card-title"><c:out value="${rn.title}"/></h3>
        <p class="card-text"><c:out value="${rn.inclusions}"/></p>
        <div class="card-footer">
          <strong>Rp <fmt:formatNumber value="${rn.price_per_day_idr}" groupingUsed="true"/> / day</strong>
          <a href="${pageContext.request.contextPath}/bookings?rental_id=${rn.id}" class="pill-btn pill-btn-sm">Reserve Rental</a>
        </div>
      </div>
    </article>
  </c:forEach>
  <c:if test="${empty rentals}">
    <div class="panel" style="grid-column:1/-1">
      <p style="color:var(--muted)">No rental equipment registered for this spot yet. <a href="${pageContext.request.contextPath}/rentals" style="font-weight:650">Add one in Rentals →</a></p>
    </div>
  </c:if>
</div>

<!-- Certified Local Guides -->
<h2 style="font-size:22px;font-weight:800;margin-bottom:14px">Verified Local Guides</h2>
<div class="card-grid" style="margin-bottom:34px">
  <c:forEach var="g" items="${guides}">
    <article class="editorial-card">
      <div class="card-body">
        <div class="card-meta"><span><c:out value="${g.license_number}"/></span><span><c:out value="${g.years_experience}"/> yrs exp</span></div>
        <h3 class="card-title"><c:out value="${g.full_name}"/></h3>
        <p class="card-text"><c:out value="${g.specialty}"/> · Languages: <c:out value="${g.languages}"/></p>
        <div class="card-footer">
          <strong>Rp <fmt:formatNumber value="${g.daily_rate_idr}" groupingUsed="true"/> / day</strong>
          <span class="tag tag-emerald">Verified HPI</span>
        </div>
      </div>
    </article>
  </c:forEach>
</div>

<!-- Field Stories -->
<h2 style="font-size:22px;font-weight:800;margin-bottom:14px">Traveler Field Stories</h2>
<div class="card-grid-2">
  <c:forEach var="s" items="${stories}">
    <article class="editorial-card">
      <div class="card-body">
        <div class="card-meta"><span>By <c:out value="${s.author_name}"/></span><span>★ <c:out value="${s.rating}"/> / 5 · <c:out value="${s.visited_month}"/></span></div>
        <h3 class="card-title"><c:out value="${s.title}"/></h3>
        <p class="card-text"><c:out value="${s.story_body}"/></p>
      </div>
    </article>
  </c:forEach>
</div>

<%@ include file="../partials/footer.jsp" %>
