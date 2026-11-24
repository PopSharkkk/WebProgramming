package com.wildindonesia;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.db.DatabaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WildIndonesiaCrudTest {
    private static AppServer server;
    private static String baseUrl;
    private static HttpClient client;
    private static File tempDb;

    @BeforeAll
    static void startServer() throws Exception {
        tempDb = Files.createTempFile("wild-indonesia-test-", ".db").toFile();
        tempDb.deleteOnExit();
        DatabaseManager.configure("jdbc:sqlite:" + tempDb.getAbsolutePath(), "", "");

        server = new AppServer();
        server.start(0);
        baseUrl = "http://localhost:" + server.getBoundPort();

        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (server != null) server.stop();
        if (tempDb != null && tempDb.exists()) tempDb.delete();
    }

    private static HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> postForm(String path, Map<String, String> params) throws Exception {
        String body = params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @Order(1)
    void heroLandingRendersWildPillAndIndonesiaGuide() throws Exception {
        HttpResponse<String> res = get("/");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("heroPill"));
        assertTrue(res.body().contains("images.unsplash.com/photo-1657788405193-c2039edaad6b"));
        assertTrue(res.body().contains("Padar Island"));
        assertTrue(res.body().contains("Let's Get Wild"));
        assertTrue(res.body().contains("Mount Bromo"));
    }

    @Test
    @Order(2)
    void loginAndQaAccountSwitchWork() throws Exception {
        HttpResponse<String> loginRes = postForm("/login", Map.of(
                "email", "curator@wildindonesia.id",
                "password", "password"
        ));
        assertEquals(200, loginRes.statusCode());
        assertTrue(loginRes.body().contains("Alya Prameswari"));

        HttpResponse<String> qaPage = get("/debug/accounts");
        assertEquals(200, qaPage.statusCode());
        assertTrue(qaPage.body().contains("QA Registered Accounts Inspector"));
    }

    @Test
    @Order(3)
    void crudFeature1DestinationsRoundTrip() throws Exception {
        postForm("/destinations/save", Map.of(
                "region_id", "1",
                "name", "Dieng Plateau Crater",
                "province", "Central Java",
                "category", "Volcano & Highland",
                "entry_fee_idr", "75000",
                "recommended_days", "2",
                "summary", "High-altitude volcanic lakes and Hindu shrines.",
                "guide_overview", "2,000m highland plateau.",
                "travel_tips", "Bring warm fleece.",
                "is_featured", "0"
        ));
        List<Map<String, Object>> list = DataStore.listDestinations("Dieng Plateau", null, null, null, "featured");
        assertEquals(1, list.size());
        long id = ((Number) list.get(0).get("id")).longValue();

        postForm("/destinations/save", Map.of(
                "id", String.valueOf(id),
                "region_id", "1",
                "name", "Dieng Plateau Updated",
                "province", "Central Java",
                "category", "Volcano & Highland",
                "entry_fee_idr", "85000",
                "recommended_days", "3",
                "summary", "Updated crater guide.",
                "guide_overview", "Updated overview.",
                "travel_tips", "Arrive dawn."
        ));
        assertEquals("Dieng Plateau Updated", DataStore.findDestination(id).get("name"));

        postForm("/destinations/delete", Map.of("id", String.valueOf(id)));
        assertNull(DataStore.findDestination(id));
    }

    @Test
    @Order(4)
    void crudFeature2RegionsRoundTrip() throws Exception {
        postForm("/regions/save", Map.of(
                "name", "Sulawesi Ridge",
                "code", "ID-SUL",
                "island_group", "Wallacea",
                "best_season", "June - September",
                "climate_notes", "Warm tropical valleys.",
                "summary", "Highland Toraja & Togean reefs."
        ));
        long id = ((Number) DataStore.listRegions().stream()
                .filter(r -> "ID-SUL".equals(r.get("code"))).findFirst().orElseThrow().get("id")).longValue();

        postForm("/regions/save", Map.of(
                "id", String.valueOf(id),
                "name", "Sulawesi & Togean",
                "code", "ID-SUL",
                "island_group", "Wallacea",
                "best_season", "May - October",
                "climate_notes", "Updated climate.",
                "summary", "Updated summary."
        ));
        assertEquals("Sulawesi & Togean", DataStore.findRegion(id).get("name"));

        postForm("/regions/delete", Map.of("id", String.valueOf(id)));
        assertNull(DataStore.findRegion(id));
    }

    @Test
    @Order(5)
    void crudFeature3RentalsRoundTrip() throws Exception {
        postForm("/rentals/save", Map.of(
                "destination_id", "1",
                "title", "Test Wrangler 4x4",
                "rental_type", "4x4 Volcano Jeep",
                "provider_name", "Wild Fleet Test",
                "price_per_day_idr", "1100000",
                "capacity_guests", "4",
                "transmission_or_spec", "4WD",
                "inclusions", "Fuel + Driver",
                "is_available", "1"
        ));
        List<Map<String, Object>> found = DataStore.listRentals("Test Wrangler", null, null, false);
        assertEquals(1, found.size());
        long id = ((Number) found.get(0).get("id")).longValue();

        postForm("/rentals/toggle", Map.of("id", String.valueOf(id)));
        assertEquals(0, ((Number) DataStore.findRental(id).get("is_available")).intValue());

        postForm("/rentals/delete", Map.of("id", String.valueOf(id)));
        assertNull(DataStore.findRental(id));
    }

    @Test
    @Order(6)
    void crudFeature4GuidesRoundTrip() throws Exception {
        postForm("/guides/save", Map.of(
                "destination_id", "1",
                "full_name", "Pak Surya Guide",
                "specialty", "Caldera Trekking",
                "languages", "Indonesian, English",
                "years_experience", "7",
                "daily_rate_idr", "600000",
                "license_number", "HPI-TEST-999",
                "bio", "Experienced East Java guide.",
                "is_verified", "1"
        ));
        List<Map<String, Object>> found = DataStore.listGuides("Pak Surya", null);
        assertEquals(1, found.size());
        long id = ((Number) found.get(0).get("id")).longValue();

        postForm("/guides/toggle", Map.of("id", String.valueOf(id)));
        assertEquals(0, ((Number) DataStore.findGuide(id).get("is_verified")).intValue());

        postForm("/guides/delete", Map.of("id", String.valueOf(id)));
        assertNull(DataStore.findGuide(id));
    }

    @Test
    @Order(7)
    void crudFeature5ItinerariesRoundTrip() throws Exception {
        postForm("/itineraries/save", Map.of(
                "region_id", "1",
                "title", "Test Merapi Sunrise Loop",
                "duration_days", "3",
                "estimated_budget_idr", "3500000",
                "pace", "Active Explorer",
                "route_stops", "Yogya -> Kaliurang -> Borobudur",
                "day_by_day_plan", "Day 1: Arrive Yogya.",
                "is_public", "1"
        ));
        long id = ((Number) DataStore.listItineraries(null, null, null).stream()
                .filter(i -> "Test Merapi Sunrise Loop".equals(i.get("title"))).findFirst().orElseThrow().get("id")).longValue();

        postForm("/itineraries/save", Map.of(
                "id", String.valueOf(id),
                "region_id", "1",
                "title", "Test Merapi Loop Updated",
                "duration_days", "4",
                "estimated_budget_idr", "4000000",
                "pace", "Slow Culture",
                "route_stops", "Yogya -> Borobudur",
                "day_by_day_plan", "Updated days.",
                "is_public", "1"
        ));
        assertEquals("Test Merapi Loop Updated", DataStore.findItinerary(id).get("title"));

        postForm("/itineraries/delete", Map.of("id", String.valueOf(id)));
        assertNull(DataStore.findItinerary(id));
    }

    @Test
    @Order(8)
    void crudFeature6BookingsRoundTrip() throws Exception {
        postForm("/bookings/save", Map.of(
                "rental_id", "1",
                "guide_id", "1",
                "start_date", "2027-03-01",
                "end_date", "2027-03-04",
                "guests_count", "4",
                "special_requests", "JUnit test charter"
        ));
        long id = ((Number) DataStore.listBookings(null, null).stream()
                .filter(b -> "JUnit test charter".equals(b.get("special_requests"))).findFirst().orElseThrow().get("id")).longValue();

        postForm("/bookings/status", Map.of("id", String.valueOf(id), "status", "Confirmed"));
        assertEquals("Confirmed", DataStore.findBooking(id).get("status"));

        postForm("/bookings/delete", Map.of("id", String.valueOf(id)));
        assertNull(DataStore.findBooking(id));
    }

    @Test
    @Order(9)
    void crudFeature7StoriesRoundTrip() throws Exception {
        postForm("/stories/save", Map.of(
                "destination_id", "1",
                "title", "JUnit Volcanic Dawn Dispatch",
                "rating", "5",
                "visited_month", "November 2026",
                "story_body", "Crisp stars above the caldera rim.",
                "travel_tip", "Pack gloves."
        ));
        long id = ((Number) DataStore.listStories(1L).stream()
                .filter(s -> "JUnit Volcanic Dawn Dispatch".equals(s.get("title"))).findFirst().orElseThrow().get("id")).longValue();

        postForm("/stories/save", Map.of(
                "id", String.valueOf(id),
                "destination_id", "1",
                "title", "JUnit Story Updated",
                "rating", "4",
                "visited_month", "December 2026",
                "story_body", "Updated narrative.",
                "travel_tip", "Arrive 04:00."
        ));
        assertEquals("JUnit Story Updated", DataStore.findStory(id).get("title"));

        postForm("/stories/delete", Map.of("id", String.valueOf(id)));
        assertNull(DataStore.findStory(id));
    }

    @Test
    @Order(10)
    void crudFeature8SavedPlacesRoundTrip() throws Exception {
        postForm("/saved/toggle", Map.of("destination_id", "2", "redirect", "/saved"));
        List<Map<String, Object>> saved = DataStore.listSavedPlaces(1L);
        Map<String, Object> match = saved.stream().filter(s -> ((Number) s.get("destination_id")).longValue() == 2L).findFirst().orElseThrow();
        long id = ((Number) match.get("id")).longValue();

        postForm("/saved/update", Map.of(
                "id", String.valueOf(id),
                "target_season", "August 2027",
                "priority_level", "Next Trip",
                "personal_note", "Updated bucket note"
        ));
        assertTrue(DataStore.listSavedPlaces(1L).stream().anyMatch(s -> "Updated bucket note".equals(s.get("personal_note"))));

        postForm("/saved/delete", Map.of("id", String.valueOf(id)));
        assertTrue(DataStore.listSavedPlaces(1L).stream().noneMatch(s -> ((Number) s.get("id")).longValue() == id));
    }
}
