<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><c:out value="${pageTitle != null ? pageTitle : 'Wild Indonesia — Tourist Guide & Expedition Rentals'}"/></title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/editorial.css">
</head>
<body>
  <div class="page-backdrop" aria-hidden="true">
    <svg viewBox="0 0 1440 960" preserveAspectRatio="none">
      <path d="M -120 920 C 260 880, 420 510, 820 420 C 1140 340, 1290 120, 1580 -40" fill="none" stroke="#ffffff" stroke-width="2.4" opacity="0.65"/>
      <path d="M -140 660 C 190 600, 460 440, 860 390 C 1180 340, 1340 160, 1560 -70" fill="none" stroke="#ffffff" stroke-width="3.2" opacity="0.72"/>
      <path d="M 40 1010 C 340 870, 560 720, 900 580 C 1180 460, 1340 310, 1560 120" fill="none" stroke="#ffffff" stroke-width="2.8" opacity="0.68"/>
      <path d="M 110 990 C 410 810, 610 630, 970 490 C 1230 390, 1360 220, 1560 70" fill="none" stroke="#ffffff" stroke-width="3.6" opacity="0.75"/>
      <path d="M -80 180 C 210 150, 430 110, 710 40 C 990 -30, 1190 -40, 1470 -100" fill="none" stroke="#ffffff" stroke-width="2.1" opacity="0.60"/>
    </svg>
  </div>

  <header class="site-nav">
    <div class="site-nav-inner">
      <a href="${pageContext.request.contextPath}/" class="brand-mark">
        WILD <span class="brand-sub">Indonesia Guide</span>
      </a>

      <nav class="nav-links" aria-label="Main Menu">
        <a href="${pageContext.request.contextPath}/" class="${activeNav == 'home' ? 'active' : ''}">Home</a>
        <a href="${pageContext.request.contextPath}/destinations" class="${activeNav == 'destinations' ? 'active' : ''}">Destinations</a>
        <a href="${pageContext.request.contextPath}/regions" class="${activeNav == 'regions' ? 'active' : ''}">Regions</a>
        <a href="${pageContext.request.contextPath}/rentals" class="${activeNav == 'rentals' ? 'active' : ''}">Rentals</a>
        <a href="${pageContext.request.contextPath}/guides" class="${activeNav == 'guides' ? 'active' : ''}">Guides</a>
        <a href="${pageContext.request.contextPath}/itineraries" class="${activeNav == 'itineraries' ? 'active' : ''}">Itineraries</a>
        <a href="${pageContext.request.contextPath}/bookings" class="${activeNav == 'bookings' ? 'active' : ''}">Bookings</a>
        <a href="${pageContext.request.contextPath}/stories" class="${activeNav == 'stories' ? 'active' : ''}">Stories</a>
        <a href="${pageContext.request.contextPath}/saved" class="${activeNav == 'saved' ? 'active' : ''}">Saved</a>
      </nav>

      <div class="nav-actions">
        <a href="${pageContext.request.contextPath}/debug/accounts" class="tag tag-amber nav-qa-pill" style="text-decoration:none">QA Accounts</a>
        <c:choose>
          <c:when test="${not empty sessionScope.currentUser}">
            <a href="${pageContext.request.contextPath}/profile" class="tag tag-dark" style="text-decoration:none">
              <c:out value="${sessionScope.currentUser.full_name}"/>
            </a>
            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline">
              <button type="submit" class="pill-btn pill-btn-outline pill-btn-sm">Sign Out</button>
            </form>
          </c:when>
          <c:otherwise>
            <a href="${pageContext.request.contextPath}/login" class="pill-btn pill-btn-outline pill-btn-sm">Sign In</a>
            <a href="${pageContext.request.contextPath}/register" class="pill-btn pill-btn-sm nav-join-btn">Join Guide</a>
          </c:otherwise>
        </c:choose>
        <button type="button" class="nav-menu-toggle" aria-expanded="false" onclick="var n=document.querySelector('.site-nav .nav-links');var o=n.classList.toggle('is-open');this.setAttribute('aria-expanded',o);this.textContent=o?'Close':'Menu';">Menu</button>
      </div>
    </div>
  </header>

  <main class="shell">
    <c:if test="${not empty sessionScope.flashSuccess}">
      <div class="flash"><c:out value="${sessionScope.flashSuccess}"/></div>
      <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
      <div class="flash flash-error"><c:out value="${sessionScope.flashError}"/></div>
      <c:remove var="flashError" scope="session"/>
    </c:if>
