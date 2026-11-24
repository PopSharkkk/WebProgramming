package com.wildindonesia.dao;

import com.wildindonesia.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Unified JDBC Data Access Layer for all 9 3NF tables in Wild Indonesia.
 * Uses PreparedStatement exclusively to eliminate SQL injection vectors and
 * returns lightweight LinkedHashMap rows directly consumable by JSP/JSTL EL.
 */
public final class DataStore {
    private DataStore() {}

    public static String formatIdr(Number amount) {
        if (amount == null) return "Rp 0";
        long val = amount.longValue();
        if (val == 0) return "Free Entry";
        NumberFormat nf = NumberFormat.getInstance(Locale.of("id", "ID"));
        return "Rp " + nf.format(val);
    }

    private static List<Map<String, Object>> queryList(String sql, Object... params) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return extractRows(rs);
            }
        }
    }

    private static Map<String, Object> queryOne(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> list = queryList(sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    private static long executeInsert(String sql, Object... params) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }

    private static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static List<Map<String, Object>> extractRows(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    // ------------------------------------------------------------------------
    // PLATFORM TELEMETRY
    // ------------------------------------------------------------------------
    public static Map<String, Integer> getPlatformCounts() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String[] tables = {"destinations", "regions", "rentals", "local_guides", "itineraries", "bookings", "travel_stories", "saved_places", "users"};
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement()) {
            for (String t : tables) {
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + t)) {
                    counts.put(t, rs.next() ? rs.getInt(1) : 0);
                }
            }
        }
        return counts;
    }

    // ------------------------------------------------------------------------
    // USERS & QA TELEMETRY
    // ------------------------------------------------------------------------
    public static Map<String, Object> findUserByEmail(String email) throws SQLException {
        return queryOne("SELECT * FROM users WHERE email = ?", email);
    }

    public static Map<String, Object> findUserById(long id) throws SQLException {
        return queryOne("SELECT * FROM users WHERE id = ?", id);
    }

    public static long createUser(String fullName, String email, String passwordHash, String role, String nationality, String phone, String bio) throws SQLException {
        return executeInsert(
            "INSERT INTO users (full_name, email, password_hash, role, nationality, phone, bio) VALUES (?,?,?,?,?,?,?)",
            fullName, email, passwordHash, role, nationality, phone, bio);
    }

    public static void updateUserProfile(long id, String fullName, String nationality, String phone, String bio) throws SQLException {
        executeUpdate("UPDATE users SET full_name=?, nationality=?, phone=?, bio=? WHERE id=?",
            fullName, nationality, phone, bio, id);
    }

    public static List<Map<String, Object>> listUsersWithStats(String roleFilter, String search) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT u.*, " +
            "(SELECT COUNT(*) FROM bookings b WHERE b.user_id = u.id) AS bookings_count, " +
            "(SELECT COUNT(*) FROM itineraries it WHERE it.user_id = u.id) AS itineraries_count, " +
            "(SELECT COUNT(*) FROM travel_stories s WHERE s.user_id = u.id) AS stories_count, " +
            "(SELECT COUNT(*) FROM saved_places sp WHERE sp.user_id = u.id) AS saved_count " +
            "FROM users u WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (roleFilter != null && !roleFilter.isBlank()) {
            sql.append("AND u.role = ? ");
            params.add(roleFilter);
        }
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(COALESCE(u.nationality,'')) LIKE ?) ");
            String like = "%" + search.toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append("ORDER BY u.id DESC");
        return queryList(sql.toString(), params.toArray());
    }

    public static void deleteUser(long id) throws SQLException {
        executeUpdate("DELETE FROM users WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 1: DESTINATIONS CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listDestinations(String search, Long regionId, String category, Integer maxFee, String sort) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT d.*, r.name AS region_name, r.code AS region_code, " +
            "(SELECT COUNT(*) FROM rentals rn WHERE rn.destination_id = d.id) AS rental_count, " +
            "(SELECT COUNT(*) FROM local_guides lg WHERE lg.destination_id = d.id) AS guide_count " +
            "FROM destinations d JOIN regions r ON d.region_id = r.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(d.name) LIKE ? OR LOWER(d.province) LIKE ? OR LOWER(d.summary) LIKE ? OR LOWER(r.name) LIKE ?) ");
            String like = "%" + search.toLowerCase() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (regionId != null && regionId > 0) {
            sql.append("AND d.region_id = ? ");
            params.add(regionId);
        }
        if (category != null && !category.isBlank()) {
            sql.append("AND d.category = ? ");
            params.add(category);
        }
        if (maxFee != null && maxFee > 0) {
            sql.append("AND d.entry_fee_idr <= ? ");
            params.add(maxFee);
        }
        if ("fee_asc".equals(sort)) sql.append("ORDER BY d.entry_fee_idr ASC, d.id DESC");
        else if ("fee_desc".equals(sort)) sql.append("ORDER BY d.entry_fee_idr DESC, d.id DESC");
        else if ("name".equals(sort)) sql.append("ORDER BY d.name ASC");
        else sql.append("ORDER BY d.is_featured DESC, d.id DESC");
        return queryList(sql.toString(), params.toArray());
    }

    public static List<Map<String, Object>> featuredDestinations(int limit) throws SQLException {
        return queryList(
            "SELECT d.*, r.name AS region_name FROM destinations d JOIN regions r ON d.region_id = r.id " +
            "ORDER BY d.is_featured DESC, d.id ASC LIMIT ?", limit);
    }

    public static Map<String, Object> findDestination(long id) throws SQLException {
        return queryOne("SELECT d.*, r.name AS region_name, r.best_season FROM destinations d JOIN regions r ON d.region_id = r.id WHERE d.id = ?", id);
    }

    public static long createDestination(long regionId, String name, String slug, String province, String category,
                                         int entryFeeIdr, int recommendedDays, double lat, double lng,
                                         String heroImage, String summary, String guideOverview, String travelTips, int isFeatured) throws SQLException {
        return executeInsert(
            "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            regionId, name, slug, province, category, entryFeeIdr, recommendedDays, lat, lng, heroImage, summary, guideOverview, travelTips, isFeatured);
    }

    public static void updateDestination(long id, long regionId, String name, String province, String category,
                                         int entryFeeIdr, int recommendedDays, String heroImage,
                                         String summary, String guideOverview, String travelTips, int isFeatured) throws SQLException {
        executeUpdate(
            "UPDATE destinations SET region_id=?, name=?, province=?, category=?, entry_fee_idr=?, recommended_days=?, hero_image=?, summary=?, guide_overview=?, travel_tips=?, is_featured=? WHERE id=?",
            regionId, name, province, category, entryFeeIdr, recommendedDays, heroImage, summary, guideOverview, travelTips, isFeatured, id);
    }

    public static void toggleDestinationFeatured(long id) throws SQLException {
        executeUpdate("UPDATE destinations SET is_featured = CASE WHEN is_featured = 1 THEN 0 ELSE 1 END WHERE id = ?", id);
    }

    public static void deleteDestination(long id) throws SQLException {
        executeUpdate("DELETE FROM destinations WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 2: REGIONS CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listRegions() throws SQLException {
        return queryList(
            "SELECT r.*, (SELECT COUNT(*) FROM destinations d WHERE d.region_id = r.id) AS dest_count, " +
            "(SELECT COUNT(*) FROM itineraries i WHERE i.region_id = r.id) AS itinerary_count " +
            "FROM regions r ORDER BY r.id ASC");
    }

    public static Map<String, Object> findRegion(long id) throws SQLException {
        return queryOne("SELECT * FROM regions WHERE id = ?", id);
    }

    public static long createRegion(String name, String code, String islandGroup, String climateNotes, String bestSeason, String coverImage, String summary) throws SQLException {
        return executeInsert(
            "INSERT INTO regions (name, code, island_group, climate_notes, best_season, cover_image, summary) VALUES (?,?,?,?,?,?,?)",
            name, code, islandGroup, climateNotes, bestSeason, coverImage, summary);
    }

    public static void updateRegion(long id, String name, String code, String islandGroup, String climateNotes, String bestSeason, String coverImage, String summary) throws SQLException {
        executeUpdate(
            "UPDATE regions SET name=?, code=?, island_group=?, climate_notes=?, best_season=?, cover_image=?, summary=? WHERE id=?",
            name, code, islandGroup, climateNotes, bestSeason, coverImage, summary, id);
    }

    public static void deleteRegion(long id) throws SQLException {
        executeUpdate("DELETE FROM regions WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 3: WILD RENTALS CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listRentals(String search, String typeFilter, Long destId, boolean availableOnly) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT rn.*, d.name AS destination_name, d.province " +
            "FROM rentals rn JOIN destinations d ON rn.destination_id = d.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(rn.title) LIKE ? OR LOWER(rn.provider_name) LIKE ? OR LOWER(d.name) LIKE ?) ");
            String like = "%" + search.toLowerCase() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (typeFilter != null && !typeFilter.isBlank()) {
            sql.append("AND rn.rental_type = ? ");
            params.add(typeFilter);
        }
        if (destId != null && destId > 0) {
            sql.append("AND rn.destination_id = ? ");
            params.add(destId);
        }
        if (availableOnly) {
            sql.append("AND rn.is_available = 1 ");
        }
        sql.append("ORDER BY rn.is_available DESC, rn.id DESC");
        return queryList(sql.toString(), params.toArray());
    }

    public static Map<String, Object> findRental(long id) throws SQLException {
        return queryOne("SELECT rn.*, d.name AS destination_name FROM rentals rn JOIN destinations d ON rn.destination_id = d.id WHERE rn.id = ?", id);
    }

    public static long createRental(long destId, String title, String rentalType, String providerName,
                                    int pricePerDay, int capacity, String spec, String imageUrl, String inclusions, int isAvailable) throws SQLException {
        return executeInsert(
            "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
            destId, title, rentalType, providerName, pricePerDay, capacity, spec, imageUrl, inclusions, isAvailable);
    }

    public static void updateRental(long id, long destId, String title, String rentalType, String providerName,
                                    int pricePerDay, int capacity, String spec, String imageUrl, String inclusions, int isAvailable) throws SQLException {
        executeUpdate(
            "UPDATE rentals SET destination_id=?, title=?, rental_type=?, provider_name=?, price_per_day_idr=?, capacity_guests=?, transmission_or_spec=?, image_url=?, inclusions=?, is_available=? WHERE id=?",
            destId, title, rentalType, providerName, pricePerDay, capacity, spec, imageUrl, inclusions, isAvailable, id);
    }

    public static void toggleRentalAvailability(long id) throws SQLException {
        executeUpdate("UPDATE rentals SET is_available = CASE WHEN is_available = 1 THEN 0 ELSE 1 END WHERE id = ?", id);
    }

    public static void deleteRental(long id) throws SQLException {
        executeUpdate("DELETE FROM rentals WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 4: LOCAL GUIDES CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listGuides(String search, Long destId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT lg.*, d.name AS destination_name, d.province " +
            "FROM local_guides lg JOIN destinations d ON lg.destination_id = d.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(lg.full_name) LIKE ? OR LOWER(lg.specialty) LIKE ? OR LOWER(lg.languages) LIKE ? OR LOWER(d.name) LIKE ?) ");
            String like = "%" + search.toLowerCase() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (destId != null && destId > 0) {
            sql.append("AND lg.destination_id = ? ");
            params.add(destId);
        }
        sql.append("ORDER BY lg.is_verified DESC, lg.years_experience DESC, lg.id DESC");
        return queryList(sql.toString(), params.toArray());
    }

    public static Map<String, Object> findGuide(long id) throws SQLException {
        return queryOne("SELECT * FROM local_guides WHERE id = ?", id);
    }

    public static long createGuide(long destId, String fullName, String specialty, String languages, int years,
                                   int dailyRate, String licenseNo, String photoUrl, String bio, int isVerified) throws SQLException {
        return executeInsert(
            "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
            destId, fullName, specialty, languages, years, dailyRate, licenseNo, photoUrl, bio, isVerified);
    }

    public static void updateGuide(long id, long destId, String fullName, String specialty, String languages, int years,
                                   int dailyRate, String licenseNo, String photoUrl, String bio, int isVerified) throws SQLException {
        executeUpdate(
            "UPDATE local_guides SET destination_id=?, full_name=?, specialty=?, languages=?, years_experience=?, daily_rate_idr=?, license_number=?, photo_url=?, bio=?, is_verified=? WHERE id=?",
            destId, fullName, specialty, languages, years, dailyRate, licenseNo, photoUrl, bio, isVerified, id);
    }

    public static void toggleGuideVerified(long id) throws SQLException {
        executeUpdate("UPDATE local_guides SET is_verified = CASE WHEN is_verified = 1 THEN 0 ELSE 1 END WHERE id = ?", id);
    }

    public static void deleteGuide(long id) throws SQLException {
        executeUpdate("DELETE FROM local_guides WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 5: ITINERARIES CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listItineraries(Long regionId, String pace, Integer maxBudget) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT i.*, r.name AS region_name, u.full_name AS author_name " +
            "FROM itineraries i JOIN regions r ON i.region_id = r.id JOIN users u ON i.user_id = u.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (regionId != null && regionId > 0) {
            sql.append("AND i.region_id = ? ");
            params.add(regionId);
        }
        if (pace != null && !pace.isBlank()) {
            sql.append("AND i.pace = ? ");
            params.add(pace);
        }
        if (maxBudget != null && maxBudget > 0) {
            sql.append("AND i.estimated_budget_idr <= ? ");
            params.add(maxBudget);
        }
        sql.append("ORDER BY i.id DESC");
        return queryList(sql.toString(), params.toArray());
    }

    public static Map<String, Object> findItinerary(long id) throws SQLException {
        return queryOne(
            "SELECT i.*, r.name AS region_name, u.full_name AS author_name " +
            "FROM itineraries i JOIN regions r ON i.region_id = r.id JOIN users u ON i.user_id = u.id WHERE i.id = ?", id);
    }

    public static long createItinerary(long userId, long regionId, String title, int days, int budgetIdr,
                                       String pace, String routeStops, String dayPlan, String coverImage, int isPublic) throws SQLException {
        return executeInsert(
            "INSERT INTO itineraries (user_id, region_id, title, duration_days, estimated_budget_idr, pace, route_stops, day_by_day_plan, cover_image, is_public) VALUES (?,?,?,?,?,?,?,?,?,?)",
            userId, regionId, title, days, budgetIdr, pace, routeStops, dayPlan, coverImage, isPublic);
    }

    public static void updateItinerary(long id, long regionId, String title, int days, int budgetIdr,
                                       String pace, String routeStops, String dayPlan, String coverImage, int isPublic) throws SQLException {
        executeUpdate(
            "UPDATE itineraries SET region_id=?, title=?, duration_days=?, estimated_budget_idr=?, pace=?, route_stops=?, day_by_day_plan=?, cover_image=?, is_public=? WHERE id=?",
            regionId, title, days, budgetIdr, pace, routeStops, dayPlan, coverImage, isPublic, id);
    }

    public static void deleteItinerary(long id) throws SQLException {
        executeUpdate("DELETE FROM itineraries WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 6: BOOKINGS CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listBookings(Long userIdFilter, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT b.*, u.full_name AS guest_name, u.email AS guest_email, " +
            "rn.title AS rental_title, rn.rental_type, d.name AS destination_name, " +
            "lg.full_name AS guide_name " +
            "FROM bookings b " +
            "JOIN users u ON b.user_id = u.id " +
            "JOIN rentals rn ON b.rental_id = rn.id " +
            "JOIN destinations d ON rn.destination_id = d.id " +
            "LEFT JOIN local_guides lg ON b.guide_id = lg.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (userIdFilter != null && userIdFilter > 0) {
            sql.append("AND b.user_id = ? ");
            params.add(userIdFilter);
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append("AND b.status = ? ");
            params.add(statusFilter);
        }
        sql.append("ORDER BY b.id DESC");
        return queryList(sql.toString(), params.toArray());
    }

    public static Map<String, Object> findBooking(long id) throws SQLException {
        return queryOne("SELECT * FROM bookings WHERE id = ?", id);
    }

    public static long createBooking(long userId, long rentalId, Long guideId, String bookingCode,
                                     String startDate, String endDate, int guests, long totalPriceIdr, String specialRequests) throws SQLException {
        return executeInsert(
            "INSERT INTO bookings (user_id, rental_id, guide_id, booking_code, start_date, end_date, guests_count, total_price_idr, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,'Pending')",
            userId, rentalId, guideId, bookingCode, startDate, endDate, guests, totalPriceIdr, specialRequests);
    }

    public static void updateBookingDetails(long id, String startDate, String endDate, int guests, Long guideId, long totalPriceIdr, String specialRequests, String status) throws SQLException {
        executeUpdate(
            "UPDATE bookings SET start_date=?, end_date=?, guests_count=?, guide_id=?, total_price_idr=?, special_requests=?, status=? WHERE id=?",
            startDate, endDate, guests, guideId, totalPriceIdr, specialRequests, status, id);
    }

    public static void updateBookingStatus(long id, String status) throws SQLException {
        executeUpdate("UPDATE bookings SET status = ? WHERE id = ?", status, id);
    }

    public static void deleteBooking(long id) throws SQLException {
        executeUpdate("DELETE FROM bookings WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 7: TRAVEL STORIES CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listStories(Long destId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT s.*, u.full_name AS author_name, d.name AS destination_name, d.province " +
            "FROM travel_stories s JOIN users u ON s.user_id = u.id JOIN destinations d ON s.destination_id = d.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (destId != null && destId > 0) {
            sql.append("AND s.destination_id = ? ");
            params.add(destId);
        }
        sql.append("ORDER BY s.id DESC");
        return queryList(sql.toString(), params.toArray());
    }

    public static Map<String, Object> findStory(long id) throws SQLException {
        return queryOne("SELECT * FROM travel_stories WHERE id = ?", id);
    }

    public static long createStory(long userId, long destId, String title, int rating, String visitedMonth, String body, String photoUrl, String tip) throws SQLException {
        return executeInsert(
            "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
            userId, destId, title, rating, visitedMonth, body, photoUrl, tip);
    }

    public static void updateStory(long id, long destId, String title, int rating, String visitedMonth, String body, String photoUrl, String tip) throws SQLException {
        executeUpdate(
            "UPDATE travel_stories SET destination_id=?, title=?, rating=?, visited_month=?, story_body=?, photo_url=?, travel_tip=? WHERE id=?",
            destId, title, rating, visitedMonth, body, photoUrl, tip, id);
    }

    public static void deleteStory(long id) throws SQLException {
        executeUpdate("DELETE FROM travel_stories WHERE id = ?", id);
    }

    // ------------------------------------------------------------------------
    // FEATURE 8: SAVED PLACES (BUCKET-LIST) CRUD
    // ------------------------------------------------------------------------
    public static List<Map<String, Object>> listSavedPlaces(long userId) throws SQLException {
        return queryList(
            "SELECT sp.*, d.name AS destination_name, d.province, d.category, d.hero_image, d.entry_fee_idr, r.name AS region_name " +
            "FROM saved_places sp " +
            "JOIN destinations d ON sp.destination_id = d.id " +
            "JOIN regions r ON d.region_id = r.id " +
            "WHERE sp.user_id = ? ORDER BY sp.id DESC", userId);
    }

    public static boolean toggleSavedPlace(long userId, long destId) throws SQLException {
        Map<String, Object> existing = queryOne("SELECT id FROM saved_places WHERE user_id = ? AND destination_id = ?", userId, destId);
        if (existing != null) {
            executeUpdate("DELETE FROM saved_places WHERE id = ?", existing.get("id"));
            return false;
        }
        executeInsert(
            "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
            userId, destId, "Dry Season 2027", "Must Visit", "Saved from Indonesia destination guide.");
        return true;
    }

    public static void updateSavedPlace(long id, long userId, String targetSeason, String priorityLevel, String personalNote) throws SQLException {
        executeUpdate(
            "UPDATE saved_places SET target_season=?, priority_level=?, personal_note=? WHERE id=? AND user_id=?",
            targetSeason, priorityLevel, personalNote, id, userId);
    }

    public static void deleteSavedPlace(long id, long userId) throws SQLException {
        executeUpdate("DELETE FROM saved_places WHERE id = ? AND user_id = ?", id, userId);
    }
}
