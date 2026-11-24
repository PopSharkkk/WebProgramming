package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class GuideServlet extends HttpServlet {
    private static final String DEFAULT_IMG = "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?auto=format&fit=crop&w=800&q=80";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String search = WebUtil.param(req, "search", "");
            long destId = WebUtil.parseLong(req.getParameter("destination_id"), 0);

            req.setAttribute("activeNav", "guides");
            req.setAttribute("pageTitle", "Certified Indonesian Local Guides — Wild Indonesia");
            req.setAttribute("guides", DataStore.listGuides(search, destId > 0 ? destId : null));
            req.setAttribute("destinations", DataStore.listDestinations(null, null, null, null, "name"));
            req.setAttribute("search", search);
            req.setAttribute("selectedDestId", destId);

            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0) {
                req.setAttribute("editing", DataStore.findGuide(editId));
            }
            WebUtil.render(req, resp, "guides/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading guides", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        String pathInfo = req.getPathInfo();
        try {
            if ("/save".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                long destId = WebUtil.parseLong(req.getParameter("destination_id"), 1);
                String fullName = WebUtil.param(req, "full_name", "");
                String specialty = WebUtil.param(req, "specialty", "");
                String languages = WebUtil.param(req, "languages", "Indonesian, English");
                int years = WebUtil.parseInt(req.getParameter("years_experience"), 3);
                int rate = WebUtil.parseInt(req.getParameter("daily_rate_idr"), 500000);
                String license = WebUtil.param(req, "license_number", "HPI-ID-" + (System.currentTimeMillis() % 100000));
                String photo = WebUtil.safePhotoOrFallback(req.getParameter("photo_url"), DEFAULT_IMG);
                String bio = WebUtil.param(req, "bio", "");
                int verified = "1".equals(req.getParameter("is_verified")) || "on".equals(req.getParameter("is_verified")) ? 1 : 0;

                if (fullName.isBlank() || specialty.isBlank()) {
                    WebUtil.redirectWithError(req, resp, "/guides", "Guide name and specialty are required.");
                    return;
                }
                if (id > 0) {
                    DataStore.updateGuide(id, destId, fullName, specialty, languages, years, rate, license, photo, bio, verified);
                    WebUtil.redirectWithSuccess(req, resp, "/guides", "Updated guide: " + fullName);
                } else {
                    DataStore.createGuide(destId, fullName, specialty, languages, years, rate, license, photo, bio, verified);
                    WebUtil.redirectWithSuccess(req, resp, "/guides", "Registered new guide: " + fullName);
                }
            } else if ("/toggle".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.toggleGuideVerified(id);
                WebUtil.redirectWithSuccess(req, resp, "/guides", "Updated guide verification status.");
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.deleteGuide(id);
                WebUtil.redirectWithSuccess(req, resp, "/guides", "Local guide removed.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error saving guide", e);
        }
    }
}
