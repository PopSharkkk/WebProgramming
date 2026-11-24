package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class SavedPlaceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        try {
            long userId = WebUtil.currentUserId(req);
            req.setAttribute("activeNav", "saved");
            req.setAttribute("pageTitle", "Personal Archipelago Bucket-List — Wild Indonesia");
            req.setAttribute("savedPlaces", DataStore.listSavedPlaces(userId));
            req.setAttribute("destinations", DataStore.listDestinations(null, null, null, null, "name"));
            WebUtil.render(req, resp, "saved/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading bucket list", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        String pathInfo = req.getPathInfo();
        long userId = WebUtil.currentUserId(req);
        try {
            if ("/toggle".equals(pathInfo)) {
                long destId = WebUtil.parseLong(req.getParameter("destination_id"), 0);
                boolean added = DataStore.toggleSavedPlace(userId, destId);
                String redirectBack = WebUtil.param(req, "redirect", "/saved");
                WebUtil.redirectWithSuccess(req, resp, redirectBack,
                        added ? "Added destination to your Indonesia bucket list." : "Removed destination from your bucket list.");
            } else if ("/update".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                String season = WebUtil.param(req, "target_season", "Dry Season 2027");
                String priority = WebUtil.param(req, "priority_level", "Must Visit");
                String note = WebUtil.param(req, "personal_note", "");
                DataStore.updateSavedPlace(id, userId, season, priority, note);
                WebUtil.redirectWithSuccess(req, resp, "/saved", "Updated bucket-list travel note.");
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.deleteSavedPlace(id, userId);
                WebUtil.redirectWithSuccess(req, resp, "/saved", "Destination removed from saved bucket list.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error updating bucket list", e);
        }
    }
}
