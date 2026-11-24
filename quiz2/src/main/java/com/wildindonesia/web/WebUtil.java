package com.wildindonesia.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

public final class WebUtil {
    private WebUtil() {}

    public static void render(HttpServletRequest req, HttpServletResponse resp, String jspPath)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + jspPath).forward(req, resp);
    }

    public static void redirectWithSuccess(HttpServletRequest req, HttpServletResponse resp, String target, String msg)
            throws IOException {
        req.getSession(true).setAttribute("flashSuccess", msg);
        resp.sendRedirect(req.getContextPath() + target);
    }

    public static void redirectWithError(HttpServletRequest req, HttpServletResponse resp, String target, String msg)
            throws IOException {
        req.getSession(true).setAttribute("flashError", msg);
        resp.sendRedirect(req.getContextPath() + target);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (Map<String, Object>) session.getAttribute("currentUser");
    }

    public static Long currentUserId(HttpServletRequest req) {
        Map<String, Object> user = currentUser(req);
        if (user == null || user.get("id") == null) return null;
        return ((Number) user.get("id")).longValue();
    }

    public static boolean requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (currentUser(req) == null) {
            redirectWithError(req, resp, "/login", "Please sign in first to save changes or manage your bookings.");
            return false;
        }
        return true;
    }

    public static String param(HttpServletRequest req, String name, String fallback) {
        String v = req.getParameter(name);
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }

    public static long parseLong(String s, long fallback) {
        if (s == null || s.isBlank()) return fallback;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return fallback; }
    }

    public static int parseInt(String s, int fallback) {
        if (s == null || s.isBlank()) return fallback;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return fallback; }
    }

    public static double parseDouble(String s, double fallback) {
        if (s == null || s.isBlank()) return fallback;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return fallback; }
    }

    public static String slugify(String text) {
        if (text == null) return "entry-" + System.currentTimeMillis();
        String slug = text.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "entry-" + System.currentTimeMillis() : slug + "-" + (System.currentTimeMillis() % 10000);
    }

    public static String safePhotoOrFallback(String url, String fallback) {
        if (url == null || url.isBlank()) return fallback;
        String u = url.trim();
        if (u.startsWith("https://") || u.startsWith("http://") || u.startsWith("/")) return u;
        return fallback;
    }
}
