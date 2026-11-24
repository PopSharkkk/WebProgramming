package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class RentalServlet extends HttpServlet {
    private static final String DEFAULT_IMG = "https://images.unsplash.com/photo-1559628233-100c798642d4?auto=format&fit=crop&w=1200&q=80";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String search = WebUtil.param(req, "search", "");
            String type = WebUtil.param(req, "rental_type", "");
            long destId = WebUtil.parseLong(req.getParameter("destination_id"), 0);
            boolean availOnly = "1".equals(req.getParameter("available_only"));

            req.setAttribute("activeNav", "rentals");
            req.setAttribute("pageTitle", "Wild Expedition Rentals Fleet — Indonesia");
            req.setAttribute("rentals", DataStore.listRentals(search, type, destId > 0 ? destId : null, availOnly));
            req.setAttribute("destinations", DataStore.listDestinations(null, null, null, null, "name"));
            req.setAttribute("search", search);
            req.setAttribute("selectedType", type);
            req.setAttribute("selectedDestId", destId);
            req.setAttribute("availOnly", availOnly);

            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0) {
                req.setAttribute("editing", DataStore.findRental(editId));
            }
            WebUtil.render(req, resp, "rentals/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading rentals", e);
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
                String title = WebUtil.param(req, "title", "");
                String rentalType = WebUtil.param(req, "rental_type", "4x4 Volcano Jeep");
                String providerName = WebUtil.param(req, "provider_name", "Wild Fleet Indonesia");
                int price = WebUtil.parseInt(req.getParameter("price_per_day_idr"), 500000);
                int capacity = WebUtil.parseInt(req.getParameter("capacity_guests"), 2);
                String spec = WebUtil.param(req, "transmission_or_spec", "Standard Field Spec");
                String imageUrl = WebUtil.safePhotoOrFallback(req.getParameter("image_url"), DEFAULT_IMG);
                String inclusions = WebUtil.param(req, "inclusions", "Safety gear & orientation");
                int isAvailable = "1".equals(req.getParameter("is_available")) || "on".equals(req.getParameter("is_available")) ? 1 : 0;

                if (title.isBlank()) {
                    WebUtil.redirectWithError(req, resp, "/rentals", "Rental title is required.");
                    return;
                }
                if (id > 0) {
                    DataStore.updateRental(id, destId, title, rentalType, providerName, price, capacity, spec, imageUrl, inclusions, isAvailable);
                    WebUtil.redirectWithSuccess(req, resp, "/rentals", "Updated rental: " + title);
                } else {
                    DataStore.createRental(destId, title, rentalType, providerName, price, capacity, spec, imageUrl, inclusions, isAvailable);
                    WebUtil.redirectWithSuccess(req, resp, "/rentals", "Added rental to fleet: " + title);
                }
            } else if ("/toggle".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.toggleRentalAvailability(id);
                WebUtil.redirectWithSuccess(req, resp, "/rentals", "Updated rental availability.");
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.deleteRental(id);
                WebUtil.redirectWithSuccess(req, resp, "/rentals", "Rental removed from fleet.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error saving rental", e);
        }
    }
}
