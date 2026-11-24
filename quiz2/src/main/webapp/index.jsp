<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>WILD — Indonesia Tourist Guide &amp; Travel Rentals</title>
  <style>
    @font-face {
      font-family: 'Inter';
      src: url('${pageContext.request.contextPath}/assets/fonts/InterVariable.woff2') format('woff2');
      font-weight: 100 900;
      font-style: normal;
      font-display: block;
    }

    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    :root {
      --fs: min(19.6vh, 13.2vw);
      --ink: #1d1d1d;
      --ink-2: #3a3a3a;
      --paper: #f2f2f1;
      --rule: #a9a9a6;
      --rule-soft: rgba(29, 29, 29, 0.12);
      --btn: #1b1b1b;
      --placeholder: #d8d3cf;
      --hairline: #b9b9b9;
    }

    html, body {
      width: 100%;
      background: #ffffff;
      color: var(--ink);
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
      -webkit-font-smoothing: antialiased;
      overflow-x: hidden;
    }

    body.rec-mode {
      height: 100%;
      overflow: hidden;
    }

    body.rec-mode .guide-atlas {
      display: none;
    }

    /* ONE-SCREEN HERO STAGE */
    .stage {
      position: relative;
      width: 100%;
      height: 100vh;
      height: 100dvh;
      overflow: hidden;
      background: #ffffff;
      font-size: var(--fs);
    }

    .paper-layer {
      position: absolute;
      inset: 0;
      background: var(--paper);
      opacity: 0;
      pointer-events: none;
      z-index: 1;
    }

    .paper-lines {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      display: block;
    }

    .hero-nav {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      z-index: 10;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: calc(var(--fs) * 0.14) calc(var(--fs) * 0.27) 0;
      font-size: max(12px, calc(var(--fs) * 0.072));
      font-weight: 450;
      line-height: 1.2;
      color: var(--ink);
      transform: translateY(-140%);
    }

    .nav-group {
      display: flex;
      align-items: center;
      gap: 1.55em;
    }

    .hero-nav a {
      color: inherit;
      text-decoration: none;
      white-space: nowrap;
      transition: opacity 0.2s ease;
    }

    .hero-nav a:hover { opacity: 0.58; }

    .hero-center {
      position: absolute;
      top: 43%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 5;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.026em;
      user-select: none;
    }

    .word-part {
      font-size: 1em;
      font-weight: 800;
      letter-spacing: -0.045em;
      line-height: 0.8;
      color: var(--ink);
      white-space: nowrap;
      transform: translateY(0.02em);
    }

    .pill {
      position: relative;
      height: 1.81em;
      width: 0.012em;
      border-radius: 9999px;
      overflow: hidden;
      background: var(--placeholder);
      flex-shrink: 0;
      clip-path: inset(50% 0);
      will-change: width, clip-path;
    }

    .pill-video {
      position: absolute;
      left: 50%;
      top: 0;
      transform: translateX(-50%);
      height: 100%;
      min-width: 4.2em;
      width: 100%;
      object-fit: cover;
      object-position: center center;
      opacity: 1;
      pointer-events: none;
    }

    .pill-slide {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      object-position: center center;
      opacity: 0;
      transition: opacity 0.9s ease;
      animation: kenBurns 14s ease-in-out infinite alternate;
      pointer-events: none;
    }

    .pill-slide.is-active {
      opacity: 1;
    }

    @keyframes kenBurns {
      0% { transform: scale(1) translateY(0%); }
      100% { transform: scale(1.06) translateY(-1.2%); }
    }

    .pill-caption {
      position: absolute;
      left: 50%;
      bottom: 0.14em;
      transform: translateX(-50%);
      z-index: 3;
      color: #ffffff;
      font-size: max(10px, calc(var(--fs) * 0.052));
      font-weight: 550;
      letter-spacing: 0.04em;
      text-shadow: 0 1px 10px rgba(0, 0, 0, 0.65);
      background: rgba(29, 29, 29, 0.42);
      backdrop-filter: blur(6px);
      padding: 0.35em 0.9em;
      border-radius: 9999px;
      opacity: 0;
      white-space: nowrap;
      pointer-events: none;
    }

    .pill-hairline {
      position: absolute;
      inset: 0;
      background: var(--hairline);
      opacity: 1;
      pointer-events: none;
      z-index: 2;
    }

    .pill-icon {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      z-index: 3;
      color: #ffffff;
      opacity: 0;
      pointer-events: auto;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 0.19em;
      height: 0.19em;
      background: transparent;
      border: none;
      padding: 0;
      cursor: pointer;
    }

    .pill-icon.breeze { left: 0.15em; pointer-events: none; }
    .pill-icon.speaker { right: 0.15em; }

    .pill-icon svg {
      width: 100%;
      height: 100%;
      display: block;
    }

    .pill-icon.speaker.waves-off .wave { opacity: 0; }

    .hero-footer {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 10;
      padding: 0 0.27em 0.18em;
      display: flex;
      align-items: flex-end;
      gap: 0.22em;
      transform: translateY(160%);
    }

    .hero-copy {
      width: calc(var(--fs) * 1.42);
      flex-shrink: 0;
      font-size: max(11.5px, calc(var(--fs) * 0.066));
      line-height: 1.45;
      color: var(--ink-2);
      font-weight: 420;
    }

    .hero-copy .dropcap {
      float: left;
      font-size: 2.35em;
      font-weight: 650;
      line-height: 0.84;
      margin-right: 0.09em;
      margin-top: 0.04em;
      color: var(--ink);
    }

    .footer-rule-wrap {
      flex: 1;
      display: flex;
      align-items: center;
      padding-bottom: calc(var(--fs) * 0.095);
    }

    .footer-rule {
      width: 100%;
      height: 1px;
      background: var(--rule);
      transform-origin: center center;
      transform: scaleX(0);
    }

    .btn-wild {
      flex-shrink: 0;
      background: var(--btn);
      color: #ffffff;
      border: none;
      border-radius: 9999px;
      font-family: inherit;
      font-size: max(12px, 0.072em);
      font-weight: 500;
      padding: 0.92em 1.75em;
      cursor: pointer;
      white-space: nowrap;
      transition: transform 0.15s ease, opacity 0.2s ease;
    }

    .btn-wild:hover { opacity: 0.88; }
    .btn-wild:active { transform: scale(0.97); }

    @media (max-width: 720px) {
      :root { --fs: min(34vw, 16.5vh); }

      .hero-nav { padding: 16px 20px 0; font-size: 12.5px; }
      .hero-nav .nav-group.right-group { display: none; }
      .hero-nav .nav-group.left-group { width: 100%; justify-content: space-between; gap: 10px; }
      .hero-nav .nav-extra { display: none; }

      .hero-center {
        top: 47%;
        flex-direction: column;
        width: calc(100% - 40px);
        gap: 0.03em;
      }

      .pill {
        width: 100%;
        height: 1.5px;
        clip-path: inset(0 50%);
      }

      .pill-video {
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 100%;
        min-width: 100%;
        height: calc((100vw - 40px) / 1.85);
      }

      .pill-icon { width: 22px; height: 22px; }
      .pill-icon.breeze { left: 16px; }
      .pill-icon.speaker { right: 16px; }

      .hero-footer {
        padding: 0 20px 20px;
        flex-direction: column;
        align-items: stretch;
        gap: 14px;
      }

      .hero-copy { width: 100%; font-size: 13px; line-height: 1.5; }
      .footer-rule-wrap { flex: 1; min-width: 0; display: flex; align-items: center; gap: 14px; padding-bottom: 0; }
      .mobile-cta-row { display: flex; align-items: center; gap: 14px; width: 100%; }
      .btn-wild { height: 38px; padding: 0 18px; font-size: 12.5px; }
    }

    @media (min-width: 721px) {
      .mobile-cta-row { display: contents; }
    }

    /* ARCHIVAL INDONESIA GUIDE BELOW THE HERO FOLD */
    .guide-atlas {
      background: var(--paper);
      border-top: 1px solid var(--rule);
      padding: 64px 28px 80px;
    }

    .atlas-inner {
      max-width: 1320px;
      margin: 0 auto;
    }

    .atlas-head {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 20px;
      padding-bottom: 24px;
      border-bottom: 1px solid var(--rule);
      margin-bottom: 32px;
      flex-wrap: wrap;
    }

    .atlas-kicker {
      font-size: 11.5px;
      font-weight: 650;
      letter-spacing: 0.1em;
      text-transform: uppercase;
      color: #6e6e6a;
      margin-bottom: 6px;
    }

    .atlas-title {
      font-size: clamp(26px, 3vw, 38px);
      font-weight: 800;
      letter-spacing: -0.03em;
      line-height: 1.08;
    }

    .atlas-actions {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }

    .pill-link {
      display: inline-flex;
      align-items: center;
      border-radius: 9999px;
      padding: 10px 20px;
      font-size: 13px;
      font-weight: 550;
      text-decoration: none;
      background: var(--btn);
      color: #ffffff;
      border: 1px solid var(--btn);
    }

    .pill-link.ghost {
      background: #ffffff;
      color: var(--ink);
      border-color: var(--rule);
    }

    .atlas-stats {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 16px;
      margin-bottom: 36px;
    }

    .stat-tile {
      background: #ffffff;
      border: 1px solid var(--rule-soft);
      border-radius: 16px;
      padding: 20px;
      text-decoration: none;
      color: inherit;
    }

    .stat-num {
      font-size: 30px;
      font-weight: 800;
      letter-spacing: -0.03em;
    }

    .stat-cap {
      font-size: 12.5px;
      color: #6e6e6a;
      margin-top: 4px;
    }

    .dest-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 22px;
      margin-bottom: 48px;
    }

    .dest-card {
      background: #ffffff;
      border: 1px solid var(--rule-soft);
      border-radius: 18px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .dest-img {
      aspect-ratio: 16 / 10;
      width: 100%;
      background: #d8d3cf;
      overflow: hidden;
    }

    .dest-img img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .dest-body {
      padding: 18px 20px 20px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      flex: 1;
    }

    .dest-meta {
      font-size: 12px;
      color: #6e6e6a;
      display: flex;
      justify-content: space-between;
    }

    .dest-name {
      font-size: 18px;
      font-weight: 700;
      letter-spacing: -0.02em;
    }

    .dest-sum {
      font-size: 13.5px;
      color: var(--ink-2);
      line-height: 1.5;
    }

    .dest-foot {
      margin-top: auto;
      padding-top: 12px;
      border-top: 1px solid var(--rule-soft);
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12.5px;
    }

    .module-grid {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 14px;
      padding-top: 28px;
      border-top: 1px solid var(--rule);
    }

    .module-tile {
      background: #ffffff;
      border: 1px solid var(--rule-soft);
      border-radius: 14px;
      padding: 16px 18px;
      text-decoration: none;
      color: inherit;
    }

    .module-tile h3 { font-size: 14.5px; font-weight: 700; margin-bottom: 4px; }
    .module-tile p { font-size: 12.5px; color: #6e6e6a; line-height: 1.45; }

    @media (max-width: 960px) {
      .dest-grid { grid-template-columns: repeat(2, 1fr); }
      .atlas-stats, .module-grid { grid-template-columns: repeat(2, 1fr); }
    }

    @media (max-width: 640px) {
      .guide-atlas { padding: 40px 18px 56px; }
      .dest-grid, .atlas-stats, .module-grid { grid-template-columns: 1fr; }
    }
  </style>
</head>
<body class="${param.rec == '1' ? 'rec-mode' : ''}">
  <!-- FULL-SCREEN WILD PILL HERO -->
  <section class="stage" id="heroStage">
    <div class="paper-layer" id="paperLayer" aria-hidden="true">
      <svg class="paper-lines" viewBox="0 0 1440 960" preserveAspectRatio="none">
        <path d="M -120 920 C 260 880, 420 510, 820 420 C 1140 340, 1290 120, 1580 -40" fill="none" stroke="#ffffff" stroke-width="2.6" opacity="0.72"/>
        <path d="M -90 790 C 210 730, 390 610, 740 480 C 1070 350, 1260 210, 1540 20" fill="none" stroke="#ffffff" stroke-width="1.4" opacity="0.58"/>
        <path d="M -140 660 C 190 600, 460 440, 860 390 C 1180 340, 1340 160, 1560 -70" fill="none" stroke="#ffffff" stroke-width="3.7" opacity="0.82"/>
        <path d="M -60 540 C 310 490, 520 350, 910 270 C 1220 200, 1350 90, 1550 -10" fill="none" stroke="#ffffff" stroke-width="1.8" opacity="0.64"/>
        <path d="M -110 410 C 250 370, 590 300, 960 190 C 1240 100, 1390 20, 1540 -80" fill="none" stroke="#ffffff" stroke-width="2.9" opacity="0.76"/>
        <path d="M 40 1010 C 340 870, 560 720, 900 580 C 1180 460, 1340 310, 1560 120" fill="none" stroke="#ffffff" stroke-width="3.4" opacity="0.80"/>
        <path d="M 190 1020 C 480 900, 680 740, 1020 620 C 1260 530, 1380 380, 1560 220" fill="none" stroke="#ffffff" stroke-width="1.6" opacity="0.61"/>
        <path d="M 330 1010 C 620 890, 820 790, 1110 660 C 1320 560, 1410 440, 1550 310" fill="none" stroke="#ffffff" stroke-width="2.2" opacity="0.69"/>
        <path d="M -130 280 C 180 240, 480 180, 790 95 C 1080 20, 1280 -20, 1520 -90" fill="none" stroke="#ffffff" stroke-width="1.3" opacity="0.56"/>
        <path d="M -70 970 C 240 820, 490 660, 810 530 C 1100 410, 1270 250, 1520 50" fill="none" stroke="#ffffff" stroke-width="1.9" opacity="0.67"/>
        <path d="M 110 990 C 410 810, 610 630, 970 490 C 1230 390, 1360 220, 1560 70" fill="none" stroke="#ffffff" stroke-width="3.9" opacity="0.85"/>
        <path d="M -150 850 C 140 770, 340 530, 680 430 C 980 340, 1190 190, 1480 -20" fill="none" stroke="#ffffff" stroke-width="2.4" opacity="0.74"/>
        <path d="M 520 1010 C 780 910, 980 820, 1220 710 C 1380 630, 1460 520, 1570 420" fill="none" stroke="#ffffff" stroke-width="1.2" opacity="0.55"/>
        <path d="M -80 180 C 210 150, 430 110, 710 40 C 990 -30, 1190 -40, 1470 -100" fill="none" stroke="#ffffff" stroke-width="2.8" opacity="0.78"/>
        <path d="M 250 1020 C 560 880, 760 690, 1080 560 C 1310 460, 1420 330, 1570 190" fill="none" stroke="#ffffff" stroke-width="1.7" opacity="0.62"/>
      </svg>
    </div>

    <nav class="hero-nav" id="heroNav" aria-label="Primary Navigation">
      <div class="nav-group left-group">
        <a href="${pageContext.request.contextPath}/">Home</a>
        <a href="${pageContext.request.contextPath}/destinations">Destinations</a>
        <a href="${pageContext.request.contextPath}/regions" class="nav-extra">Regions</a>
        <a href="${pageContext.request.contextPath}/rentals">Rentals</a>
        <a href="${pageContext.request.contextPath}/itineraries">Itineraries</a>
        <a href="${pageContext.request.contextPath}/guides" class="nav-extra">Guides</a>
      </div>
      <div class="nav-group right-group">
        <a href="${pageContext.request.contextPath}/stories">Field Stories</a>
        <a href="${pageContext.request.contextPath}/bookings">Bookings</a>
        <a href="${pageContext.request.contextPath}/debug/accounts">QA Accounts</a>
        <c:choose>
          <c:when test="${not empty sessionScope.currentUser}">
            <a href="${pageContext.request.contextPath}/saved">Saved</a>
            <a href="${pageContext.request.contextPath}/profile"><c:out value="${sessionScope.currentUser.full_name}"/></a>
          </c:when>
          <c:otherwise>
            <a href="${pageContext.request.contextPath}/login">Sign In</a>
          </c:otherwise>
        </c:choose>
      </div>
    </nav>

    <div class="hero-center" id="heroCenter" aria-label="Wild Indonesia Hero">
      <span class="word-part">WI</span>

      <div class="pill" id="heroPill">
        <div class="pill-video" id="heroGallery">
          <img class="pill-slide is-active" data-place="Padar Island · Komodo, East Nusa Tenggara" src="https://images.unsplash.com/photo-1657788405193-c2039edaad6b?auto=format&fit=crop&w=1600&q=85" alt="Padar Island, Indonesia">
          <img class="pill-slide" data-place="Wayag Karst Lagoons · Raja Ampat, Papua" src="https://images.unsplash.com/photo-1703769605296-b5ab3cece3d9?auto=format&fit=crop&w=1600&q=85" alt="Raja Ampat, Indonesia">
          <img class="pill-slide" data-place="Mount Bromo Caldera · East Java" src="https://images.unsplash.com/photo-1530172202330-0b30ddcfc7b5?auto=format&fit=crop&w=1600&q=85" alt="Mount Bromo, Indonesia">
          <img class="pill-slide" data-place="Kelingking Cliffs · Nusa Penida, Bali" src="https://images.unsplash.com/photo-1576363399501-0dd08fc750e1?auto=format&fit=crop&w=1600&q=85" alt="Nusa Penida, Indonesia">
          <img class="pill-slide" data-place="Borobudur Dawn Stupas · Central Java" src="https://images.unsplash.com/photo-1566559532224-6d65e9fc0f37?auto=format&fit=crop&w=1600&q=85" alt="Borobudur, Indonesia">
        </div>
        <div class="pill-hairline" id="pillHairline"></div>
        <div class="pill-caption" id="pillCaption">Padar Island · Komodo, East Nusa Tenggara</div>

        <span class="pill-icon breeze" id="iconBreeze" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round">
            <path d="M3 8h10a2.5 2.5 0 1 0-2.4-3.2"/>
            <path d="M3 12h15a2.5 2.5 0 1 1-2.4 3.2"/>
            <path d="M3 16h8a2 2 0 1 1-2 2.4"/>
          </svg>
        </span>

        <button type="button" class="pill-icon speaker" id="iconSpeaker" aria-label="Next Indonesian destination photo" title="Next Indonesian landmark">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="5" width="18" height="14" rx="3"/>
            <circle cx="8.5" cy="10.5" r="1.5"/>
            <path d="M21 15l-4.5-4.5L7 19"/>
          </svg>
        </button>
      </div>

      <span class="word-part">LD</span>
    </div>

    <footer class="hero-footer" id="heroFooter">
      <p class="hero-copy">
        <span class="dropcap">E</span>mbark on a wild ride across Indonesia with our archipelago travel rental &amp; tourist guide. Discover volcanic horizons and hidden lagoons on your unforgettable journey.
      </p>
      <div class="mobile-cta-row">
        <div class="footer-rule-wrap">
          <div class="footer-rule" id="footerRule"></div>
        </div>
        <button type="button" class="btn-wild" id="btnWild">Let's Get Wild</button>
      </div>
    </footer>
  </section>

  <!-- INDONESIA TOURIST GUIDE & RENTAL ATLAS (BELOW THE HERO FOLD) -->
  <section class="guide-atlas" id="explore-indonesia">
    <div class="atlas-inner">
      <div class="atlas-head">
        <div>
          <div class="atlas-kicker">Wild Indonesia · Archival Field Edition</div>
          <h2 class="atlas-title">Indonesian Islands, Expedition Rentals &amp; Local Guides</h2>
        </div>
        <div class="atlas-actions">
          <a href="${pageContext.request.contextPath}/destinations" class="pill-link">Browse All Destinations</a>
          <a href="${pageContext.request.contextPath}/rentals" class="pill-link ghost">Rent Expedition Fleet</a>
          <a href="${pageContext.request.contextPath}/debug/accounts" class="pill-link ghost">QA Account Switcher</a>
        </div>
      </div>

      <div class="atlas-stats">
        <a href="${pageContext.request.contextPath}/destinations" class="stat-tile">
          <div class="stat-num"><c:out value="${counts.destinations}"/></div>
          <div class="stat-cap">Curated Indonesian Destinations</div>
        </a>
        <a href="${pageContext.request.contextPath}/regions" class="stat-tile">
          <div class="stat-num"><c:out value="${counts.regions}"/></div>
          <div class="stat-cap">Archipelago Climate Regions</div>
        </a>
        <a href="${pageContext.request.contextPath}/rentals" class="stat-tile">
          <div class="stat-num"><c:out value="${counts.rentals}"/></div>
          <div class="stat-cap">Phinisi, 4x4 &amp; Eco-Villa Rentals</div>
        </a>
        <a href="${pageContext.request.contextPath}/guides" class="stat-tile">
          <div class="stat-num"><c:out value="${counts.local_guides}"/></div>
          <div class="stat-cap">Certified Indonesian Local Guides</div>
        </a>
      </div>

      <div class="dest-grid">
        <c:forEach var="d" items="${featuredDestinations}">
          <article class="dest-card">
            <div class="dest-img">
              <img src="${d.hero_image}" alt="<c:out value='${d.name}'/>" loading="lazy">
            </div>
            <div class="dest-body">
              <div class="dest-meta">
                <span><c:out value="${d.region_name}"/></span>
                <span><c:out value="${d.province}"/></span>
              </div>
              <h3 class="dest-name"><c:out value="${d.name}"/></h3>
              <p class="dest-sum"><c:out value="${d.summary}"/></p>
              <div class="dest-foot">
                <strong><c:out value="${d.category}"/> · <c:out value="${d.recommended_days}"/> Days</strong>
                <a href="${pageContext.request.contextPath}/destinations/view?id=${d.id}" style="font-weight:600">Open Field Guide →</a>
              </div>
            </div>
          </article>
        </c:forEach>
      </div>

      <div class="module-grid">
        <a href="${pageContext.request.contextPath}/destinations" class="module-tile">
          <h3>01 · Destinations CRUD</h3>
          <p>Search volcanic calderas, reefs, and temples across Indonesia; add, update, or curate entries.</p>
        </a>
        <a href="${pageContext.request.contextPath}/regions" class="module-tile">
          <h3>02 · Island Regions CRUD</h3>
          <p>Compare Java, Bali, Komodo, and Papua seasonal monsoons and manage regional atlases.</p>
        </a>
        <a href="${pageContext.request.contextPath}/rentals" class="module-tile">
          <h3>03 · Wild Rentals Fleet</h3>
          <p>Browse 24m wooden phinisi boats, FJ40 jeeps, scooters, and bamboo villas with availability toggles.</p>
        </a>
        <a href="${pageContext.request.contextPath}/guides" class="module-tile">
          <h3>04 · Local Guides Directory</h3>
          <p>Connect with licensed Indonesian rangers, marine skippers, and cultural archaeologists.</p>
        </a>
        <a href="${pageContext.request.contextPath}/itineraries" class="module-tile">
          <h3>05 · Trip Itineraries CRUD</h3>
          <p>Plan multi-day island routes with day-by-day stages and estimated IDR budgets.</p>
        </a>
        <a href="${pageContext.request.contextPath}/bookings" class="module-tile">
          <h3>06 · Bookings &amp; Checkout</h3>
          <p>Reserve Wild rentals paired with a local guide and track confirmation statuses.</p>
        </a>
        <a href="${pageContext.request.contextPath}/stories" class="module-tile">
          <h3>07 · Field Stories &amp; Tips</h3>
          <p>Read and publish authentic Indonesian trail reports, photo logs, and local advice.</p>
        </a>
        <a href="${pageContext.request.contextPath}/saved" class="module-tile">
          <h3>08 · Bucket-List Watchlist</h3>
          <p>Bookmark destinations, set priority tiers, and organize target travel seasons.</p>
        </a>
      </div>
    </div>
  </section>

  <script>
    (function () {
      const DURATION = 2100;
      const paper = document.getElementById('paperLayer');
      const nav = document.getElementById('heroNav');
      const center = document.getElementById('heroCenter');
      const pill = document.getElementById('heroPill');
      const hairline = document.getElementById('pillHairline');
      const footer = document.getElementById('heroFooter');
      const rule = document.getElementById('footerRule');
      const iconBreeze = document.getElementById('iconBreeze');
      const iconSpeaker = document.getElementById('iconSpeaker');
      const pillCaption = document.getElementById('pillCaption');
      const slides = Array.from(document.querySelectorAll('.pill-slide'));
      const btnWild = document.getElementById('btnWild');

      const params = new URLSearchParams(window.location.search);
      const isRec = params.get('rec') === '1';
      const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

      let activeAnimations = [];
      let isReplaying = false;
      let slideIdx = 0;

      function showSlide(idx) {
        slideIdx = (idx + slides.length) % slides.length;
        slides.forEach((el, i) => el.classList.toggle('is-active', i === slideIdx));
        if (pillCaption && slides[slideIdx]) {
          pillCaption.textContent = slides[slideIdx].dataset.place || 'Indonesia';
        }
      }

      window.__recVideo = function (t) {
        return (t % 4.25) + (1 / 120);
      };

      iconSpeaker.addEventListener('click', () => {
        showSlide(slideIdx + 1);
      });

      if (!reducedMotion && !isRec) {
        setInterval(() => showSlide(slideIdx + 1), 4500);
      }

      function isPhone() { return window.innerWidth <= 720; }

      function clearAnimations() {
        activeAnimations.forEach(a => a.cancel());
        activeAnimations = [];
      }

      function buildTimeline(playImmediately) {
        clearAnimations();
        const phone = isPhone();

        let pillFrames;
        let gapFrames;

        if (phone) {
          const w = center.clientWidth || (document.documentElement.clientWidth - 40);
          const finalH = w / 1.85;
          pill.style.width = '100%';
          pillFrames = [
            { height: '1.5px', clipPath: 'inset(0 50%)', offset: 0 },
            { height: '1.5px', clipPath: 'inset(0 0%)', offset: 0.14, easing: 'cubic-bezier(0.5,0,0.3,1)' },
            { height: (finalH * 0.10).toFixed(2) + 'px', clipPath: 'inset(0 0%)', offset: 0.30, easing: 'cubic-bezier(0.45,0,0.3,1)' },
            { height: (finalH * 0.55).toFixed(2) + 'px', clipPath: 'inset(0 0%)', offset: 0.58, easing: 'cubic-bezier(0.4,0,0.2,1)' },
            { height: finalH.toFixed(2) + 'px', clipPath: 'inset(0 0%)', offset: 1 }
          ];
          gapFrames = [
            { gap: '0.03em', offset: 0 },
            { gap: '0.03em', offset: 0.14 },
            { gap: '0.06em', offset: 0.58 },
            { gap: '0.1em', offset: 1 }
          ];
        } else {
          pill.style.height = '1.81em';
          pillFrames = [
            { width: '0.012em', clipPath: 'inset(50% 0)', offset: 0 },
            { width: '0.012em', clipPath: 'inset(0% 0)', offset: 0.14, easing: 'cubic-bezier(0.5,0,0.3,1)' },
            { width: '0.16em', clipPath: 'inset(0% 0)', offset: 0.30, easing: 'cubic-bezier(0.45,0,0.3,1)' },
            { width: '1.81em', clipPath: 'inset(0% 0)', offset: 0.58, easing: 'cubic-bezier(0.4,0,0.2,1)' },
            { width: '3.62em', clipPath: 'inset(0% 0)', offset: 1 }
          ];
          gapFrames = [
            { gap: '0.026em', offset: 0 },
            { gap: '0.026em', offset: 0.14 },
            { gap: '0.05em', offset: 0.58 },
            { gap: '0.085em', offset: 1 }
          ];
        }

        const opts = { duration: DURATION, fill: 'both' };

        const anims = [
          pill.animate(pillFrames, opts),
          center.animate(gapFrames, opts),
          hairline.animate([
            { opacity: 1, offset: 0 },
            { opacity: 1, offset: 0.14 },
            { opacity: 0, offset: 0.30 },
            { opacity: 0, offset: 1 }
          ], opts),
          paper.animate([
            { opacity: 0, offset: 0 },
            { opacity: 0, offset: 0.45 },
            { opacity: 1, offset: 0.78 },
            { opacity: 1, offset: 1 }
          ], opts),
          nav.animate([
            { transform: 'translateY(-140%)', offset: 0 },
            { transform: 'translateY(-140%)', offset: 0.50, easing: 'cubic-bezier(0.2,0.8,0.2,1)' },
            { transform: 'translateY(0%)', offset: 0.86 },
            { transform: 'translateY(0%)', offset: 1 }
          ], opts),
          footer.animate([
            { transform: 'translateY(160%)', offset: 0 },
            { transform: 'translateY(160%)', offset: 0.52, easing: 'cubic-bezier(0.2,0.8,0.2,1)' },
            { transform: 'translateY(0%)', offset: 0.90 },
            { transform: 'translateY(0%)', offset: 1 }
          ], opts),
          rule.animate([
            { transform: 'scaleX(0)', offset: 0 },
            { transform: 'scaleX(0)', offset: 0.40 },
            { transform: 'scaleX(1)', offset: 0.95 },
            { transform: 'scaleX(1)', offset: 1 }
          ], opts),
          iconBreeze.animate([
            { opacity: 0, offset: 0 },
            { opacity: 0, offset: 0.80 },
            { opacity: 0.85, offset: 1 }
          ], opts),
          iconSpeaker.animate([
            { opacity: 0, offset: 0 },
            { opacity: 0, offset: 0.80 },
            { opacity: 0.85, offset: 1 }
          ], opts),
          pillCaption.animate([
            { opacity: 0, offset: 0 },
            { opacity: 0, offset: 0.80 },
            { opacity: 1, offset: 1 }
          ], opts)
        ];

        if (!playImmediately) anims.forEach(a => a.finish());
        activeAnimations = anims;
        return anims;
      }

      async function playCollapseAndReopen() {
        if (isReplaying || !activeAnimations.length) return;
        isReplaying = true;
        activeAnimations.forEach(a => a.reverse());
        await Promise.all(activeAnimations.map(a => a.finished.catch(() => {})));
        await new Promise(r => setTimeout(r, 180));
        activeAnimations.forEach(a => a.play());
        await Promise.all(activeAnimations.map(a => a.finished.catch(() => {})));
        isReplaying = false;
      }

      btnWild.addEventListener('click', () => {
        if (reducedMotion) return;
        playCollapseAndReopen();
      });

      window.addEventListener('resize', () => { buildTimeline(false); });

      document.fonts.ready.then(() => {
        if (reducedMotion) {
          buildTimeline(false);
          return;
        }
        buildTimeline(true);

        if (isRec) {
          setTimeout(() => { activeAnimations.forEach(a => a.reverse()); }, 4900);
        }
      });
    })();
  </script>
</body>
</html>
