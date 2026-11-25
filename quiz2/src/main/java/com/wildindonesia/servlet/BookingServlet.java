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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BookingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> user = WebUtil.currentUser(req);
            String statusFilter = WebUtil.param(req, "status", "");
            boolean isCurator = user != null && "curator".equals(user.get("role"));
            boolean viewAll = isCurator && ("1".equals(req.getParameter("all")) || isCurator);
            List<Map<String, Object>> bookings = (user == null)
                    ? List.of()
                    : DataStore.listBookings(viewAll ? null : WebUtil.currentUserId(req), statusFilter);

            req.setAttribute("activeNav", "bookings");
            req.setAttribute("pageTitle", "Expedition Rental Bookings — Wild Indonesia");
            req.setAttribute("bookings", bookings);
            req.setAttribute("rentals", DataStore.listRentals(null, null, null, false));
            req.setAttribute("guides", DataStore.listGuides(null, null));
            req.setAttribute("selectedStatus", statusFilter);
            req.setAttribute("viewAll", viewAll);
            req.setAttribute("preselectedRentalId", WebUtil.parseLong(req.getParameter("rental_id"), 0));

            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0 && user != null) {
                Map<String, Object> existing = DataStore.findBooking(editId);
                if (existing != null && (isCurator || ((Number) existing.get("user_id")).longValue() == WebUtil.currentUserId(req))) {
                    req.setAttribute("editing", existing);
                }
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
        long userId = WebUtil.currentUserId(req);
        boolean isCurator = WebUtil.isCurator(req);
        try {
            if ("/save".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                long rentalId = WebUtil.parseLong(req.getParameter("rental_id"), 1);
                long guideRaw = WebUtil.parseLong(req.getParameter("guide_id"), 0);
                Long guideId = guideRaw > 0 ? guideRaw : null;
                String startDate = WebUtil.param(req, "start_date", LocalDate.now().plusDays(7).toString());
                String endDate = WebUtil.param(req, "end_date", LocalDate.now().plusDays(9).toString());
                int guests = Math.max(1, WebUtil.parseInt(req.getParameter("guests_count"), 2));
                String notes = WebUtil.param(req, "special_requests", "");
                String status = WebUtil.param(req, "status", "Pending");

                long days = computeDays(startDate, endDate);
                if (days <= 0) {
                    WebUtil.redirectWithError(req, resp, "/bookings", "Please enter valid dates where End Date is after Start Date.");
                    return;
                }
                Map<String, Object> rental = DataStore.findRental(rentalId);
                if (rental == null) {
                    WebUtil.redirectWithError(req, resp, "/bookings", "Selected rental was not found.");
                    return;
                }
                long dailyRental = ((Number) rental.get("price_per_day_idr")).longValue();
                long dailyGuide = 0L;
                if (guideId != null) {
                    Map<String, Object> guide = DataStore.findGuide(guideId);
                    if (guide != null) dailyGuide = ((Number) guide.get("daily_rate_idr")).longValue();
                    else guideId = null;
                }
                long total = days * (dailyRental + dailyGuide);

                if (id > 0) {
                    Map<String, Object> existing = DataStore.findBooking(id);
                    if (existing == null || (!isCurator && ((Number) existing.get("user_id")).longValue() != userId)) {
                        WebUtil.redirectWithError(req, resp, "/bookings", "You do not have permission to edit this booking.");
                        return;
                    }
                    DataStore.updateBookingDetails(id, rentalId, startDate, endDate, guests, guideId, total, notes, status);
                    WebUtil.redirectWithSuccess(req, resp, "/bookings", "Updated booking #" + id + " (" + days + " day(s)).");
                } else {
                    String code = "WLD-2026-" + ThreadLocalRandom.current().nextInt(1000, 9999) + "-" + ThreadLocalRandom.current().nextInt(10, 99);
                    DataStore.createBooking(userId, rentalId, guideId, code, startDate, endDate, guests, total, notes);
                    WebUtil.redirectWithSuccess(req, resp, "/bookings", "Reservation confirmed as Pending (" + code + ").");
                }
            } else if ("/status".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                Map<String, Object> existing = DataStore.findBooking(id);
                if (existing == null || (!isCurator && ((Number) existing.get("user_id")).longValue() != userId)) {
                    WebUtil.redirectWithError(req, resp, "/bookings", "You do not have permission to update this booking.");
                    return;
                }
                String status = WebUtil.param(req, "status", "Confirmed");
                DataStore.updateBookingStatus(id, status);
                WebUtil.redirectWithSuccess(req, resp, "/bookings", "Booking status changed to " + status + ".");
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                Map<String, Object> existing = DataStore.findBooking(id);
                if (existing == null || (!isCurator && ((Number) existing.get("user_id")).longValue() != userId)) {
                    WebUtil.redirectWithError(req, resp, "/bookings", "You do not have permission to delete this booking.");
                    return;
                }
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
            return diff > 0 ? diff : -1L;
        } catch (Exception ex) {
            return -1L;
        }
    }
}
