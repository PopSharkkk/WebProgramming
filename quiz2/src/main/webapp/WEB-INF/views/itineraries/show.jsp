<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div style="margin-bottom:18px">
  <a href="${pageContext.request.contextPath}/itineraries" class="pill-btn pill-btn-outline pill-btn-sm">← Back to Itineraries</a>
</div>

<div class="panel" style="padding:0;overflow:hidden">
  <div style="aspect-ratio:21/9;width:100%;background:#d8d3cf;overflow:hidden">
    <img src="${itinerary.cover_image}" alt="<c:out value='${itinerary.title}'/>" style="width:100%;height:100%;object-fit:cover;display:block">
  </div>
  <div style="padding:28px">
    <div class="kicker"><c:out value="${itinerary.region_name}"/> · <c:out value="${itinerary.pace}"/></div>
    <h1 class="page-title" style="margin-bottom:12px"><c:out value="${itinerary.title}"/></h1>
    <div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:20px">
      <span class="tag tag-dark"><c:out value="${itinerary.duration_days}"/> Days</span>
      <span class="tag">Estimated Budget: Rp <fmt:formatNumber value="${itinerary.estimated_budget_idr}" groupingUsed="true"/></span>
      <span class="tag">By <c:out value="${itinerary.author_name}"/></span>
    </div>

    <div style="background:var(--paper);border-radius:14px;padding:16px;margin-bottom:22px;font-size:14px">
      <strong>Route Sequence:</strong> <c:out value="${itinerary.route_stops}"/>
    </div>

    <h2 style="font-size:18px;font-weight:700;margin-bottom:10px">Day-by-Day Stage Plan</h2>
    <pre style="font-family:inherit;white-space:pre-wrap;font-size:14.5px;line-height:1.7;color:var(--ink-2)"><c:out value="${itinerary.day_by_day_plan}"/></pre>

    <div style="margin-top:24px;display:flex;gap:10px;flex-wrap:wrap">
      <a href="${pageContext.request.contextPath}/bookings" class="pill-btn">Book Rental for This Route</a>
      <a href="${pageContext.request.contextPath}/itineraries?edit_id=${itinerary.id}" class="pill-btn pill-btn-outline">Edit Plan</a>
    </div>
  </div>
</div>

<%@ include file="../partials/footer.jsp" %>
