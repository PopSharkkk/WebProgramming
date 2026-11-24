package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class BookingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> user = WebUtil.currentUser(req);
            String statusFilter = WebUtil.param(req, "status", "");
            boolean viewAll = "1".equals(req.getParameter("all")) || (user != null && "curator".equals(user.get("role")));
            Long userIdFilter = (user != null && !viewAll) ? WebUtil.currentUserId(req) : null;

            req.setAttribute("activeNav", "bookings");
            req.setAttribute("pageTitle", "Expedition Rental Bookings — Wild Indonesia");
            req.setAttribute("bookings", DataStore.listBookings(userIdFilter, statusFilter));
            req.setAttribute("rentals", DataStore.listRentals(null, null, null, false));
            req.setAttribute("guides", DataStore.listGuides(null, null));
            req.setAttribute("selectedStatus", statusFilter);
            req.setAttribute("viewAll", viewAll);
            req.setAttribute("preselectedRentalId", WebUtil.parseLong(req.getParameter("rental_id"), 0));

            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0) {
                req.setAttribute("editing", DataStore.findBooking(editId));
            }
            WebUtil.render(req, resp, "bookings/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading bookings", e);
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
                long rentalId = WebUtil.parseLong(req.getParameter("rental_id"), 1);
                long guideRaw = WebUtil.parseLong(req.getParameter("guide_id"), 0);
                Long guideId = guideRaw > 0 ? guideRaw : null;
                String startDate = WebUtil.param(req, "start_date", LocalDate.now().plusDays(7).toString());
                String endDate = WebUtil.param(req, "end_date", LocalDate.now().plusDays(9).toString());
                int guests = Math.max(1, WebUtil.parseInt(req.getParameter("guests_count"), 2));
                String notes = WebUtil.param(req, "special_requests", "");
                String status = WebUtil.param(req, "status", "Pending");

                long days = computeDays(startDate, endDate);
                Map<String, Object> rental = DataStore.findRental(rentalId);
                long dailyRental = rental != null ? ((Number) rental.get("price_per_day_idr")).longValue() : 500000L;
                long dailyGuide = 0L;
                if (guideId != null) {
                    Map<String, Object> guide = DataStore.findGuide(guideId);
                    if (guide != null) dailyGuide = ((Number) guide.get("daily_rate_idr")).longValue();
                }
                long total = days * (dailyRental + dailyGuide);

                if (id > 0) {
                    DataStore.updateBookingDetails(id, startDate, endDate, guests, guideId, total, notes, status);
                    WebUtil.redirectWithSuccess(req, resp, "/bookings", "Updated booking #" + id + " (" + days + " day(s)).");
                } else {
                    String code = "WLD-2026-" + (1000 + (System.currentTimeMillis() % 8999));
                    DataStore.createBooking(userId, rentalId, guideId, code, startDate, endDate, guests, total, notes);
                    WebUtil.redirectWithSuccess(req, resp, "/bookings", "Reservation confirmed as Pending (" + code + ").");
                }
            } else if ("/status".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                String status = WebUtil.param(req, "status", "Confirmed");
                DataStore.updateBookingStatus(id, status);
                WebUtil.redirectWithSuccess(req, resp, "/bookings?all=1", "Booking status changed to " + status + ".");
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                DataStore.deleteBooking(id);
                WebUtil.redirectWithSuccess(req, resp, "/bookings", "Booking record cancelled and removed.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error managing booking", e);
        }
    }

    private static long computeDays(String startStr, String endStr) {
        try {
            LocalDate s = LocalDate.parse(startStr);
            LocalDate e = LocalDate.parse(endStr);
            long diff = ChronoUnit.DAYS.between(s, e);
            return Math.max(1L, diff);
        } catch (Exception ex) {
            return 2L;
        }
    }
}
