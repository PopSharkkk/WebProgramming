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

public class DestinationServlet extends HttpServlet {
    private static final String DEFAULT_IMG = "https://images.unsplash.com/photo-1530172202330-0b30ddcfc7b5?auto=format&fit=crop&w=1200&q=80";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/view".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), -1);
                Map<String, Object> dest = DataStore.findDestination(id);
                if (dest == null) {
                    WebUtil.redirectWithError(req, resp, "/destinations", "Destination not found.");
                    return;
                }
                req.setAttribute("activeNav", "destinations");
                req.setAttribute("pageTitle", dest.get("name") + " — Wild Indonesia");
                req.setAttribute("dest", dest);
                req.setAttribute("rentals", DataStore.listRentals(null, null, id, false));
                req.setAttribute("guides", DataStore.listGuides(null, id));
                req.setAttribute("stories", DataStore.listStories(id));
                WebUtil.render(req, resp, "destinations/show.jsp");
                return;
            }

            String search = WebUtil.param(req, "search", "");
            long regionId = WebUtil.parseLong(req.getParameter("region_id"), 0);
            String category = WebUtil.param(req, "category", "");
            int maxFee = WebUtil.parseInt(req.getParameter("max_fee"), 0);
            String sort = WebUtil.param(req, "sort", "featured");

            req.setAttribute("activeNav", "destinations");
            req.setAttribute("pageTitle", "Indonesia Destinations Guide — Wild Indonesia");
            req.setAttribute("destinations", DataStore.listDestinations(search, regionId > 0 ? regionId : null, category, maxFee > 0 ? maxFee : null, sort));
            req.setAttribute("regions", DataStore.listRegions());
            req.setAttribute("search", search);
            req.setAttribute("selectedRegionId", regionId);
            req.setAttribute("selectedCategory", category);
            req.setAttribute("maxFee", maxFee > 0 ? maxFee : "");
            req.setAttribute("selectedSort", sort);

            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0) {
                req.setAttribute("editing", DataStore.findDestination(editId));
            }
            WebUtil.render(req, resp, "destinations/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading destinations", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        String pathInfo = req.getPathInfo();
        try {
            if ("/save".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                long regionId = WebUtil.parseLong(req.getParameter("region_id"), 1);
                String name = WebUtil.param(req, "name", "");
                String province = WebUtil.param(req, "province", "");
                String category = WebUtil.param(req, "category", "Volcano & Highland");
                int entryFee = WebUtil.parseInt(req.getParameter("entry_fee_idr"), 0);
                int days = WebUtil.parseInt(req.getParameter("recommended_days"), 2);
                String heroImage = WebUtil.safePhotoOrFallback(req.getParameter("hero_image"), DEFAULT_IMG);
                String summary = WebUtil.param(req, "summary", "");
                String overview = WebUtil.param(req, "guide_overview", "");
                String tips = WebUtil.param(req, "travel_tips", "");
                int isFeatured = "1".equals(req.getParameter("is_featured")) || "on".equals(req.getParameter("is_featured")) ? 1 : 0;

                if (name.isBlank() || province.isBlank() || summary.isBlank()) {
                    WebUtil.redirectWithError(req, resp, "/destinations", "Name, province, and summary are required.");
                    return;
                }
                if (id > 0) {
                    DataStore.updateDestination(id, regionId, name, province, category, entryFee, days, heroImage, summary, overview, tips, isFeatured);
                    WebUtil.redirectWithSuccess(req, resp, "/destinations", "Updated destination: " + name);
                } else {
                    DataStore.createDestination(regionId, name, WebUtil.slugify(name), province, category, entryFee, days, -7.5, 112.5, heroImage, summary, overview, tips, isFeatured);
                    WebUtil.redirectWithSuccess(req, resp, "/destinations", "Added new Indonesia destination: " + name);
                }
            } else if ("/toggle-featured".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.toggleDestinationFeatured(id);
                WebUtil.redirectWithSuccess(req, resp, "/destinations", "Toggled featured spotlight status.");
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.deleteDestination(id);
                WebUtil.redirectWithSuccess(req, resp, "/destinations", "Destination removed.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error saving destination", e);
        }
    }
}
