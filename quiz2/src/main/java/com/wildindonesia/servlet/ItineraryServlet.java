package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class ItineraryServlet extends HttpServlet {
    private static final String DEFAULT_IMG = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/view".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), -1);
                Map<String, Object> item = DataStore.findItinerary(id);
                if (item == null) {
                    WebUtil.redirectWithError(req, resp, "/itineraries", "Itinerary not found.");
                    return;
                }
                req.setAttribute("activeNav", "itineraries");
                req.setAttribute("pageTitle", item.get("title") + " — Wild Indonesia");
                req.setAttribute("itinerary", item);
                WebUtil.render(req, resp, "itineraries/show.jsp");
                return;
            }

            long regionId = WebUtil.parseLong(req.getParameter("region_id"), 0);
            String pace = WebUtil.param(req, "pace", "");
            int maxBudget = WebUtil.parseInt(req.getParameter("max_budget"), 0);

            req.setAttribute("activeNav", "itineraries");
            req.setAttribute("pageTitle", "Indonesia Multi-Day Itineraries — Wild Indonesia");
            req.setAttribute("itineraries", DataStore.listItineraries(regionId > 0 ? regionId : null, pace, maxBudget > 0 ? maxBudget : null));
            req.setAttribute("regions", DataStore.listRegions());
            req.setAttribute("selectedRegionId", regionId);
            req.setAttribute("selectedPace", pace);
            req.setAttribute("maxBudget", maxBudget > 0 ? maxBudget : "");

            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0) {
                req.setAttribute("editing", DataStore.findItinerary(editId));
            }
            WebUtil.render(req, resp, "itineraries/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading itineraries", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        String pathInfo = req.getPathInfo();
        try {
            if ("/save".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                long userId = WebUtil.currentUserId(req);
                long regionId = WebUtil.parseLong(req.getParameter("region_id"), 1);
                String title = WebUtil.param(req, "title", "");
                int days = WebUtil.parseInt(req.getParameter("duration_days"), 4);
                int budget = WebUtil.parseInt(req.getParameter("estimated_budget_idr"), 5000000);
                String pace = WebUtil.param(req, "pace", "Active Explorer");
                String stops = WebUtil.param(req, "route_stops", "");
                String plan = WebUtil.param(req, "day_by_day_plan", "");
                String cover = WebUtil.safePhotoOrFallback(req.getParameter("cover_image"), DEFAULT_IMG);
                int isPublic = "1".equals(req.getParameter("is_public")) || "on".equals(req.getParameter("is_public")) ? 1 : 0;

                if (title.isBlank() || stops.isBlank()) {
                    WebUtil.redirectWithError(req, resp, "/itineraries", "Itinerary title and route stops are required.");
                    return;
                }
                if (id > 0) {
                    var existing = DataStore.findItinerary(id);
                    if (existing == null || (!WebUtil.isCurator(req) && ((Number) existing.get("user_id")).longValue() != userId)) {
                        WebUtil.redirectWithError(req, resp, "/itineraries", "You do not have permission to edit this itinerary.");
                        return;
                    }
                    DataStore.updateItinerary(id, regionId, title, days, budget, pace, stops, plan, cover, isPublic);
                    WebUtil.redirectWithSuccess(req, resp, "/itineraries", "Updated itinerary: " + title);
                } else {
                    DataStore.createItinerary(userId, regionId, title, days, budget, pace, stops, plan, cover, isPublic);
                    WebUtil.redirectWithSuccess(req, resp, "/itineraries", "Published itinerary: " + title);
                }
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                var existing = DataStore.findItinerary(id);
                if (existing == null || (!WebUtil.isCurator(req) && ((Number) existing.get("user_id")).longValue() != WebUtil.currentUserId(req))) {
                    WebUtil.redirectWithError(req, resp, "/itineraries", "You do not have permission to delete this itinerary.");
                    return;
                }
                DataStore.deleteItinerary(id);
                WebUtil.redirectWithSuccess(req, resp, "/itineraries", "Itinerary deleted.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error saving itinerary", e);
        }
    }
}
