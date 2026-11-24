<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../partials/header.jsp" %>

<div style="max-width:520px;margin:28px auto">
  <div class="panel">
    <div class="kicker">Account Access</div>
    <h1 class="page-title" style="font-size:30px;margin-bottom:8px">Sign In to Wild Indonesia</h1>
    <p style="font-size:14px;color:var(--ink-2);margin-bottom:20px">
      Save destinations, book expedition vehicles &amp; phinisi boats, publish field stories, or curate guide entries.
    </p>

    <div style="background:var(--paper);border:1px solid var(--rule-soft);border-radius:14px;padding:14px;margin-bottom:20px">
      <div style="font-size:12px;font-weight:700;margin-bottom:8px">Quick Demo Accounts (Password: password)</div>
      <div style="display:flex;gap:8px;flex-wrap:wrap">
        <button type="button" class="pill-btn pill-btn-outline pill-btn-sm" onclick="fillDemo('curator@wildindonesia.id')">Curator (Alya)</button>
        <button type="button" class="pill-btn pill-btn-outline pill-btn-sm" onclick="fillDemo('traveler@wildindonesia.id')">Traveler (Nadia)</button>
        <a href="${pageContext.request.contextPath}/debug/accounts" class="pill-btn pill-btn-sm">QA Account Switcher</a>
      </div>
    </div>

    <form action="${pageContext.request.contextPath}/login" method="post" style="display:flex;flex-direction:column;gap:14px">
      <div class="field">
        <label for="emailInput">Email Address</label>
        <input type="email" id="emailInput" name="email" class="input" value="curator@wildindonesia.id" required>
      </div>
      <div class="field">
        <label for="pwInput">Password</label>
        <input type="password" id="pwInput" name="password" class="input" value="password" required>
      </div>
      <button type="submit" class="pill-btn" style="width:100%;margin-top:6px">Sign In</button>
    </form>

    <p style="margin-top:18px;font-size:13.5px;color:var(--muted);text-align:center">
      New traveler? <a href="${pageContext.request.contextPath}/register" style="font-weight:650;color:var(--ink)">Create an account</a>
    </p>
  </div>
</div>

<script>
function fillDemo(email) {
  document.getElementById('emailInput').value = email;
  document.getElementById('pwInput').value = 'password';
}
</script>

<%@ include file="../partials/footer.jsp" %>
