<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../partials/header.jsp" %>

<div class="page-head">
  <div>
    <div class="kicker">Database Engineering · 3NF Schema (<c:out value="${dbDialect}"/>)</div>
    <h1 class="page-title">Conceptual &amp; Physical Data Models (CDM / PDM)</h1>
    <p class="page-lead">Normalized 9-table relational architecture powering Wild Indonesia's tourist guide, expedition rentals, and booking engine.</p>
  </div>
  <button type="button" class="pill-btn" onclick="window.print()">Print Diagrams</button>
</div>

<!-- 1. CDM SVG -->
<div class="panel" style="margin-bottom:28px">
  <h2 style="font-size:20px;font-weight:800;margin-bottom:6px">1. Conceptual Data Model (CDM)</h2>
  <p style="font-size:13.5px;color:var(--ink-2);margin-bottom:18px">High-level business entities, core attributes, and 1:N / M:N cardinalities across Indonesia's regions, destinations, expedition fleet, and travelers.</p>
  <svg viewBox="0 0 1120 640" style="width:100%;height:auto;background:#fafaf9;border:1px solid var(--rule-soft);border-radius:14px">
    <!-- Relationships -->
    <line x1="250" y1="120" x2="380" y2="120" stroke="#1d1d1d" stroke-width="1.6"/>
    <text x="315" y="110" font-size="11" font-weight="700" text-anchor="middle">Contains (1:N)</text>

    <line x1="620" y1="120" x2="770" y2="120" stroke="#1d1d1d" stroke-width="1.6"/>
    <text x="695" y="110" font-size="11" font-weight="700" text-anchor="middle">Offers (1:N)</text>

    <line x1="500" y1="220" x2="500" y2="330" stroke="#1d1d1d" stroke-width="1.6"/>
    <text x="555" y="280" font-size="11" font-weight="700">Assigned (1:N)</text>

    <line x1="890" y1="220" x2="890" y2="330" stroke="#1d1d1d" stroke-width="1.6"/>
    <text x="955" y="280" font-size="11" font-weight="700">Reserved (1:N)</text>

    <line x1="250" y1="430" x2="770" y2="430" stroke="#1d1d1d" stroke-width="1.6" stroke-dasharray="5,4"/>
    <text x="695" y="420" font-size="11" font-weight="700" text-anchor="middle">Books (1:N)</text>

    <line x1="145" y1="220" x2="145" y2="330" stroke="#6e6e6a" stroke-width="1.5" stroke-dasharray="4,4"/>
    <text x="155" y="280" font-size="11" font-weight="700">Plans Routes (1:N)</text>

    <!-- REGIONS -->
    <g transform="translate(30,40)">
      <rect width="220" height="165" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.8"/>
      <rect width="220" height="30" rx="10" fill="#1d1d1d"/>
      <text x="110" y="20" fill="#ffffff" font-size="12" font-weight="700" text-anchor="middle">REGIONS</text>
      <text x="14" y="52" font-size="11.5">• id (Identifier), code</text>
      <text x="14" y="72" font-size="11.5">• name, island_group</text>
      <text x="14" y="92" font-size="11.5">• best_season</text>
      <text x="14" y="112" font-size="11.5">• climate_notes</text>
      <text x="14" y="132" font-size="11.5">• cover_image, summary</text>
    </g>

    <!-- DESTINATIONS -->
    <g transform="translate(380,40)">
      <rect width="240" height="180" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.8"/>
      <rect width="240" height="30" rx="10" fill="#1d1d1d"/>
      <text x="120" y="20" fill="#ffffff" font-size="12" font-weight="700" text-anchor="middle">DESTINATIONS</text>
      <text x="14" y="52" font-size="11.5">• id (Identifier), slug</text>
      <text x="14" y="72" font-size="11.5">• name, province, category</text>
      <text x="14" y="92" font-size="11.5">• entry_fee_idr, days</text>
      <text x="14" y="112" font-size="11.5">• latitude, longitude</text>
      <text x="14" y="132" font-size="11.5">• guide_overview, travel_tips</text>
      <text x="14" y="152" font-size="11.5">• hero_image, is_featured</text>
    </g>

    <!-- RENTALS -->
    <g transform="translate(770,40)">
      <rect width="250" height="180" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.8"/>
      <rect width="250" height="30" rx="10" fill="#1d1d1d"/>
      <text x="125" y="20" fill="#ffffff" font-size="12" font-weight="700" text-anchor="middle">RENTALS</text>
      <text x="14" y="52" font-size="11.5">• id (Identifier), title</text>
      <text x="14" y="72" font-size="11.5">• rental_type, provider_name</text>
      <text x="14" y="92" font-size="11.5">• price_per_day_idr</text>
      <text x="14" y="112" font-size="11.5">• capacity_guests, spec</text>
      <text x="14" y="132" font-size="11.5">• inclusions, is_available</text>
    </g>

    <!-- USERS -->
    <g transform="translate(30,330)">
      <rect width="220" height="180" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.8"/>
      <rect width="220" height="30" rx="10" fill="#0f766e"/>
      <text x="110" y="20" fill="#ffffff" font-size="12" font-weight="700" text-anchor="middle">USERS (Traveler / Curator)</text>
      <text x="14" y="52" font-size="11.5">• id (Identifier), role</text>
      <text x="14" y="72" font-size="11.5">• full_name, email</text>
      <text x="14" y="92" font-size="11.5">• password_hash</text>
      <text x="14" y="112" font-size="11.5">• nationality, phone, bio</text>
      <text x="14" y="136" font-size="11" fill="#6e6e6a">Owns: Itineraries, Stories, Saved</text>
    </g>

    <!-- LOCAL_GUIDES -->
    <g transform="translate(380,330)">
      <rect width="240" height="180" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.8"/>
      <rect width="240" height="30" rx="10" fill="#1d1d1d"/>
      <text x="120" y="20" fill="#ffffff" font-size="12" font-weight="700" text-anchor="middle">LOCAL_GUIDES</text>
      <text x="14" y="52" font-size="11.5">• id (Identifier), full_name</text>
      <text x="14" y="72" font-size="11.5">• specialty, languages</text>
      <text x="14" y="92" font-size="11.5">• daily_rate_idr, years_exp</text>
      <text x="14" y="112" font-size="11.5">• license_number, is_verified</text>
    </g>

    <!-- BOOKINGS -->
    <g transform="translate(770,330)">
      <rect width="250" height="180" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.8"/>
      <rect width="250" height="30" rx="10" fill="#b45309"/>
      <text x="125" y="20" fill="#ffffff" font-size="12" font-weight="700" text-anchor="middle">BOOKINGS</text>
      <text x="14" y="52" font-size="11.5">• id (Identifier), booking_code</text>
      <text x="14" y="72" font-size="11.5">• start_date, end_date</text>
      <text x="14" y="92" font-size="11.5">• guests_count, total_price_idr</text>
      <text x="14" y="112" font-size="11.5">• special_requests, status</text>
    </g>

    <!-- Footer Entities Strip -->
    <g transform="translate(30,545)">
      <rect width="310" height="72" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.4"/>
      <text x="16" y="26" font-size="12" font-weight="700">ITINERARIES (user_id, region_id)</text>
      <text x="16" y="48" font-size="11">duration_days, budget_idr, pace, route_stops, plan</text>
    </g>
    <g transform="translate(380,545)">
      <rect width="310" height="72" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.4"/>
      <text x="16" y="26" font-size="12" font-weight="700">TRAVEL_STORIES (user_id, destination_id)</text>
      <text x="16" y="48" font-size="11">title, rating (1-5), visited_month, story_body, tip</text>
    </g>
    <g transform="translate(730,545)">
      <rect width="290" height="72" rx="10" fill="#ffffff" stroke="#1d1d1d" stroke-width="1.4"/>
      <text x="16" y="26" font-size="12" font-weight="700">SAVED_PLACES (user_id + destination_id UQ)</text>
      <text x="16" y="48" font-size="11">target_season, priority_level, personal_note</text>
    </g>
  </svg>
</div>

<!-- 2. PDM Relational Summary Table -->
<div class="panel">
  <h2 style="font-size:20px;font-weight:800;margin-bottom:12px">2. Physical Data Model (PDM) — Keys &amp; Foreign Key Cascade Rules</h2>
  <div class="table-wrap">
    <table class="data-table">
      <thead>
        <tr>
          <th>Table Name</th>
          <th>Primary Key</th>
          <th>Foreign Keys &amp; Cascade Constraints</th>
          <th>CRUD Feature Mapping</th>
        </tr>
      </thead>
      <tbody>
        <tr><td><strong>users</strong></td><td>id (INT / BIGINT AI)</td><td>UNIQUE(email)</td><td>Authentication, Profile &amp; QA Switcher</td></tr>
        <tr><td><strong>regions</strong></td><td>id (INT / BIGINT AI)</td><td>UNIQUE(code)</td><td>Feature 2 — Archipelago Regions CRUD</td></tr>
        <tr><td><strong>destinations</strong></td><td>id (INT / BIGINT AI)</td><td>region_id → regions(id) ON DELETE CASCADE</td><td>Feature 1 — Destinations Guide CRUD</td></tr>
        <tr><td><strong>rentals</strong></td><td>id (INT / BIGINT AI)</td><td>destination_id → destinations(id) ON DELETE CASCADE</td><td>Feature 3 — Wild Expedition Rentals CRUD</td></tr>
        <tr><td><strong>local_guides</strong></td><td>id (INT / BIGINT AI)</td><td>destination_id → destinations(id) ON DELETE CASCADE</td><td>Feature 4 — Certified Local Guides CRUD</td></tr>
        <tr><td><strong>itineraries</strong></td><td>id (INT / BIGINT AI)</td><td>user_id → users(id) CASCADE; region_id → regions(id) CASCADE</td><td>Feature 5 — Nusantara Trip Itineraries CRUD</td></tr>
        <tr><td><strong>bookings</strong></td><td>id (INT / BIGINT AI)</td><td>user_id → users(id) CASCADE; rental_id → rentals(id) CASCADE; guide_id → local_guides(id) SET NULL</td><td>Feature 6 — Rental &amp; Guide Bookings CRUD</td></tr>
        <tr><td><strong>travel_stories</strong></td><td>id (INT / BIGINT AI)</td><td>user_id → users(id) CASCADE; destination_id → destinations(id) CASCADE</td><td>Feature 7 — Traveler Field Stories CRUD</td></tr>
        <tr><td><strong>saved_places</strong></td><td>id (INT / BIGINT AI)</td><td>user_id → users(id) CASCADE; destination_id → destinations(id) CASCADE; UNIQUE(user_id, destination_id)</td><td>Feature 8 — Personal Bucket-List Watchlist CRUD</td></tr>
      </tbody>
    </table>
  </div>
</div>

<%@ include file="../partials/footer.jsp" %>
