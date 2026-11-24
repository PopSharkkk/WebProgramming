package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class RegionServlet extends HttpServlet {
    private static final String DEFAULT_IMG = "https://images.unsplash.com/photo-1657788405193-c2039edaad6b?auto=format&fit=crop&w=1200&q=80";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("activeNav", "regions");
            req.setAttribute("pageTitle", "Indonesian Archipelago Regions — Wild Indonesia");
            req.setAttribute("regions", DataStore.listRegions());
            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0) {
                req.setAttribute("editing", DataStore.findRegion(editId));
            }
            WebUtil.render(req, resp, "regions/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading regions", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        String pathInfo = req.getPathInfo();
        try {
            if ("/save".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                String name = WebUtil.param(req, "name", "");
                String code = WebUtil.param(req, "code", "").toUpperCase();
                String islandGroup = WebUtil.param(req, "island_group", "");
                String climateNotes = WebUtil.param(req, "climate_notes", "");
                String bestSeason = WebUtil.param(req, "best_season", "");
                String coverImage = WebUtil.safePhotoOrFallback(req.getParameter("cover_image"), DEFAULT_IMG);
                String summary = WebUtil.param(req, "summary", "");

                if (name.isBlank() || code.isBlank() || summary.isBlank()) {
                    WebUtil.redirectWithError(req, resp, "/regions", "Region name, code, and summary are required.");
                    return;
                }
                if (id > 0) {
                    DataStore.updateRegion(id, name, code, islandGroup, climateNotes, bestSeason, coverImage, summary);
                    WebUtil.redirectWithSuccess(req, resp, "/regions", "Updated region: " + name);
                } else {
                    DataStore.createRegion(name, code, islandGroup, climateNotes, bestSeason, coverImage, summary);
                    WebUtil.redirectWithSuccess(req, resp, "/regions", "Created region: " + name);
                }
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.deleteRegion(id);
                WebUtil.redirectWithSuccess(req, resp, "/regions", "Region deleted.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error managing region", e);
        }
    }
}
