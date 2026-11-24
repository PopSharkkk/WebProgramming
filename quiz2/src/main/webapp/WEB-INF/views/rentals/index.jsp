<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Feature 03 · Wild Expedition Rentals CRUD</div>
    <h1 class="page-title">Wild Indonesia Fleet &amp; Stay Rentals</h1>
    <p class="page-lead">Book timber phinisi liveaboards in Komodo, 4x4 FJ40 jeeps across Mount Bromo, bamboo eco-villas in Ubud, or coastal tourers.</p>
  </div>
  <a href="#rentalFormDrawer" onclick="document.getElementById('rentalFormDrawer').open = true" class="pill-btn">+ Add Rental Unit</a>
</div>

<form action="${pageContext.request.contextPath}/rentals" method="get" class="filter-strip">
  <div class="field" style="min-width:200px">
    <label>Search Fleet</label>
    <input type="text" name="search" class="input" placeholder="e.g. Phinisi, Land Cruiser, Villa" value="<c:out value='${search}'/>">
  </div>
  <div class="field">
    <label>Rental Type</label>
    <select name="rental_type" class="select">
      <option value="">All Fleet Types</option>
      <option value="Phinisi Yacht" ${selectedType == 'Phinisi Yacht' ? 'selected' : ''}>Phinisi Yacht / Boat</option>
      <option value="4x4 Volcano Jeep" ${selectedType == '4x4 Volcano Jeep' ? 'selected' : ''}>4x4 Volcano Jeep</option>
      <option value="Eco Villa" ${selectedType == 'Eco Villa' ? 'selected' : ''}>Eco Villa / Homestay</option>
      <option value="Island Motorbike" ${selectedType == 'Island Motorbike' ? 'selected' : ''}>Island Motorbike / E-Bike</option>
      <option value="Expedition Gear" ${selectedType == 'Expedition Gear' ? 'selected' : ''}>Expedition Gear</option>
    </select>
  </div>
  <div class="field">
    <label>Destination Hub</label>
    <select name="destination_id" class="select">
      <option value="0">All Destinations</option>
      <c:forEach var="d" items="${destinations}">
        <option value="${d.id}" ${selectedDestId == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
      </c:forEach>
    </select>
  </div>
  <label style="display:inline-flex;align-items:center;gap:6px;font-size:13px;padding-bottom:8px">
    <input type="checkbox" name="available_only" value="1" ${availOnly ? 'checked' : ''}> Available only
  </label>
  <button type="submit" class="pill-btn">Filter Fleet</button>
  <a href="${pageContext.request.contextPath}/rentals" class="pill-btn pill-btn-outline">Reset</a>
</form>

<details class="crud-drawer" id="rentalFormDrawer" ${not empty editing ? 'open' : ''}>
  <summary>
    <span>${not empty editing ? 'Edit Rental Unit' : 'Register New Rental Vehicle or Eco-Stay'}</span>
    <span class="tag">${not empty editing ? 'Update Mode' : 'Click to expand'}</span>
  </summary>
  <form action="${pageContext.request.contextPath}/rentals/save" method="post" style="margin-top:18px;display:flex;flex-direction:column;gap:14px">
    <c:if test="${not empty editing}">
      <input type="hidden" name="id" value="${editing.id}">
    </c:if>
    <div class="form-grid-3">
      <div class="field">
        <label>Rental Title</label>
        <input type="text" name="title" class="input" value="<c:out value='${editing.title}'/>" placeholder="e.g. Defender 110 Highland Camper" required>
      </div>
      <div class="field">
        <label>Type</label>
        <select name="rental_type" class="select">
          <option value="4x4 Volcano Jeep" ${editing.rental_type == '4x4 Volcano Jeep' ? 'selected' : ''}>4x4 Volcano Jeep</option>
          <option value="Phinisi Yacht" ${editing.rental_type == 'Phinisi Yacht' ? 'selected' : ''}>Phinisi Yacht</option>
          <option value="Eco Villa" ${editing.rental_type == 'Eco Villa' ? 'selected' : ''}>Eco Villa</option>
          <option value="Island Motorbike" ${editing.rental_type == 'Island Motorbike' ? 'selected' : ''}>Island Motorbike</option>
          <option value="Expedition Gear" ${editing.rental_type == 'Expedition Gear' ? 'selected' : ''}>Expedition Gear</option>
        </select>
      </div>
      <div class="field">
        <label>Base Destination</label>
        <select name="destination_id" class="select">
          <c:forEach var="d" items="${destinations}">
            <option value="${d.id}" ${editing.destination_id == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
          </c:forEach>
        </select>
      </div>
    </div>

    <div class="form-grid-3">
      <div class="field">
        <label>Operator / Provider</label>
        <input type="text" name="provider_name" class="input" value="<c:out value='${not empty editing ? editing.provider_name : "Wild Fleet Nusantara"}'/>" required>
      </div>
      <div class="field">
        <label>Daily Rate (IDR)</label>
        <input type="number" name="price_per_day_idr" class="input" value="${not empty editing ? editing.price_per_day_idr : 750000}" min="50000">
      </div>
      <div class="field">
        <label>Capacity (Guests)</label>
        <input type="number" name="capacity_guests" class="input" value="${not empty editing ? editing.capacity_guests : 4}" min="1" max="24">
      </div>
    </div>

    <div class="form-grid-2">
      <div class="field">
        <label>Specs / Transmission</label>
        <input type="text" name="transmission_or_spec" class="input" value="<c:out value='${editing.transmission_or_spec}'/>" placeholder="4WD Manual · GPS · Solar Kit">
      </div>
      <div class="field">
        <label>Photo URL (Unsplash)</label>
        <input type="url" name="image_url" class="input" value="<c:out value='${not empty editing ? editing.image_url : "https://images.unsplash.com/photo-1559628233-100c798642d4?auto=format&fit=crop&w=1200&q=80"}'/>">
      </div>
    </div>

    <div class="field">
      <label>Inclusions</label>
      <textarea name="inclusions" class="textarea"><c:out value="${editing.inclusions}"/></textarea>
    </div>

    <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px">
      <label style="display:inline-flex;align-items:center;gap:8px;font-size:13px;font-weight:600">
        <input type="checkbox" name="is_available" value="1" ${(empty editing || editing.is_available == 1) ? 'checked' : ''}> Available for Instant Booking
      </label>
      <div style="display:flex;gap:10px">
        <c:if test="${not empty editing}"><a href="${pageContext.request.contextPath}/rentals" class="pill-btn pill-btn-outline">Cancel</a></c:if>
        <button type="submit" class="pill-btn">${not empty editing ? 'Update Rental' : 'Add to Fleet'}</button>
      </div>
    </div>
  </form>
</details>

<div class="card-grid">
  <c:forEach var="rn" items="${rentals}">
    <article class="editorial-card">
      <div class="card-media">
        <img src="${rn.image_url}" alt="<c:out value='${rn.title}'/>" loading="lazy">
      </div>
      <div class="card-body">
        <div class="card-meta">
          <span class="tag"><c:out value="${rn.rental_type}"/></span>
          <form action="${pageContext.request.contextPath}/rentals/toggle" method="post" style="display:inline">
            <input type="hidden" name="id" value="${rn.id}">
            <button type="submit" class="tag ${rn.is_available == 1 ? 'tag-emerald' : 'tag-danger'}" style="cursor:pointer" title="Toggle availability">
              ${rn.is_available == 1 ? '● Available' : '○ Booked Out'}
            </button>
          </form>
        </div>
        <h2 class="card-title"><c:out value="${rn.title}"/></h2>
        <div style="font-size:12.5px;color:var(--muted)"><c:out value="${rn.destination_name}"/> · By <c:out value="${rn.provider_name}"/></div>
        <p class="card-text"><strong>Specs:</strong> <c:out value="${rn.transmission_or_spec}"/><br><c:out value="${rn.inclusions}"/></p>
        <div style="font-size:15px;font-weight:800;margin-top:4px">
          Rp <fmt:formatNumber value="${rn.price_per_day_idr}" groupingUsed="true"/> <span style="font-weight:450;font-size:12px;color:var(--muted)">/ day · up to <c:out value="${rn.capacity_guests}"/> guests</span>
        </div>
        <div class="card-footer">
          <a href="${pageContext.request.contextPath}/bookings?rental_id=${rn.id}" class="pill-btn pill-btn-sm">Book Now</a>
          <div style="display:flex;gap:6px">
            <a href="${pageContext.request.contextPath}/rentals?edit_id=${rn.id}" class="pill-btn pill-btn-outline pill-btn-sm">Edit</a>
            <form action="${pageContext.request.contextPath}/rentals/delete" method="post" onsubmit="return confirm('Delete this rental?');">
              <input type="hidden" name="id" value="${rn.id}">
              <button type="submit" class="pill-btn pill-btn-danger pill-btn-sm">Delete</button>
            </form>
          </div>
        </div>
      </div>
    </article>
  </c:forEach>
</div>

<%@ include file="../partials/footer.jsp" %>
