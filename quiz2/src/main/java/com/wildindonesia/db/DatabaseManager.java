package com.wildindonesia.db;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

/**
 * Central JDBC DatabaseManager supporting both SQLite (default embedded file)
 * and MySQL (configured via DB_URL / DB_USER / DB_PASS environment variables).
 */
public final class DatabaseManager {
    private static volatile String dbUrl = resolveEnv("DB_URL", "jdbc:sqlite:data/wild_indonesia.db");
    private static volatile String dbUser = resolveEnv("DB_USER", "");
    private static volatile String dbPass = resolveEnv("DB_PASS", "");

    private DatabaseManager() {}

    public static void configure(String url, String user, String pass) {
        dbUrl = url;
        dbUser = user == null ? "" : user;
        dbPass = pass == null ? "" : pass;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static boolean isMySql() {
        return dbUrl != null && dbUrl.toLowerCase().startsWith("jdbc:mysql:");
    }

    public static Connection getConnection() throws SQLException {
        if (!isMySql() && dbUrl.startsWith("jdbc:sqlite:")) {
            String pathPart = dbUrl.substring("jdbc:sqlite:".length());
            if (!":memory:".equals(pathPart) && !pathPart.startsWith("file:")) {
                File f = new File(pathPart);
                File parent = f.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
            }
        }
        Connection conn = (dbUser.isEmpty() && dbPass.isEmpty())
                ? DriverManager.getConnection(dbUrl)
                : DriverManager.getConnection(dbUrl, dbUser, dbPass);
        if (!isMySql()) {
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
                st.execute("PRAGMA busy_timeout = 5000");
            }
        }
        return conn;
    }

    public static void initializeAndSeed() throws SQLException {
        try (Connection conn = getConnection()) {
            createSchema(conn);
            if (countRows(conn, "destinations") == 0) {
                seedAll(conn);
            }
        }
    }

    public static String hashPassword(String plain) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(plain.toCharArray(), salt, 31000, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    public static boolean verifyPassword(String plain, String stored) {
        if (plain == null || stored == null || !stored.contains(":")) return false;
        try {
            String[] parts = stored.split(":", 2);
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            PBEKeySpec spec = new PBEKeySpec(plain.toCharArray(), salt, 31000, 256);
            byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static String resolveEnv(String key, String fallback) {
        String val = System.getProperty(key);
        if (val != null && !val.isBlank()) return val.trim();
        val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val.trim() : fallback;
    }

    private static int countRows(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void createSchema(Connection conn) throws SQLException {
        boolean mysql = isMySql();
        String pk = mysql ? "BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";
        String fkType = mysql ? "BIGINT UNSIGNED" : "INTEGER";
        String tsDefault = mysql ? "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" : "TEXT NOT NULL DEFAULT (datetime('now'))";
        String boolDefault = mysql ? "TINYINT(1)" : "INTEGER";
        String engine = mysql ? " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4" : "";

        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id %s,
                    full_name VARCHAR(120) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL DEFAULT 'traveler',
                    nationality VARCHAR(80),
                    phone VARCHAR(40),
                    bio TEXT,
                    created_at %s
                )%s
                """.formatted(pk, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS regions (
                    id %s,
                    name VARCHAR(120) NOT NULL,
                    code VARCHAR(20) NOT NULL UNIQUE,
                    island_group VARCHAR(100) NOT NULL,
                    climate_notes TEXT NOT NULL,
                    best_season VARCHAR(100) NOT NULL,
                    cover_image VARCHAR(500) NOT NULL,
                    summary TEXT NOT NULL,
                    created_at %s
                )%s
                """.formatted(pk, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS destinations (
                    id %s,
                    region_id %s NOT NULL,
                    name VARCHAR(150) NOT NULL,
                    slug VARCHAR(160) NOT NULL UNIQUE,
                    province VARCHAR(100) NOT NULL,
                    category VARCHAR(60) NOT NULL,
                    entry_fee_idr INT NOT NULL DEFAULT 0,
                    recommended_days INT NOT NULL DEFAULT 2,
                    latitude DOUBLE,
                    longitude DOUBLE,
                    hero_image VARCHAR(500) NOT NULL,
                    summary TEXT NOT NULL,
                    guide_overview TEXT NOT NULL,
                    travel_tips TEXT NOT NULL,
                    is_featured %s NOT NULL DEFAULT 0,
                    created_at %s,
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )%s
                """.formatted(pk, fkType, boolDefault, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS rentals (
                    id %s,
                    destination_id %s NOT NULL,
                    title VARCHAR(160) NOT NULL,
                    rental_type VARCHAR(60) NOT NULL,
                    provider_name VARCHAR(120) NOT NULL,
                    price_per_day_idr INT NOT NULL,
                    capacity_guests INT NOT NULL DEFAULT 2,
                    transmission_or_spec VARCHAR(120) NOT NULL,
                    image_url VARCHAR(500) NOT NULL,
                    inclusions TEXT NOT NULL,
                    is_available %s NOT NULL DEFAULT 1,
                    created_at %s,
                    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
                )%s
                """.formatted(pk, fkType, boolDefault, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS local_guides (
                    id %s,
                    destination_id %s NOT NULL,
                    full_name VARCHAR(120) NOT NULL,
                    specialty VARCHAR(100) NOT NULL,
                    languages VARCHAR(160) NOT NULL,
                    years_experience INT NOT NULL DEFAULT 3,
                    daily_rate_idr INT NOT NULL,
                    license_number VARCHAR(60) NOT NULL UNIQUE,
                    photo_url VARCHAR(500) NOT NULL,
                    bio TEXT NOT NULL,
                    is_verified %s NOT NULL DEFAULT 1,
                    created_at %s,
                    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
                )%s
                """.formatted(pk, fkType, boolDefault, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS itineraries (
                    id %s,
                    user_id %s NOT NULL,
                    region_id %s NOT NULL,
                    title VARCHAR(160) NOT NULL,
                    duration_days INT NOT NULL DEFAULT 4,
                    estimated_budget_idr INT NOT NULL,
                    pace VARCHAR(40) NOT NULL,
                    route_stops VARCHAR(300) NOT NULL,
                    day_by_day_plan TEXT NOT NULL,
                    cover_image VARCHAR(500) NOT NULL,
                    is_public %s NOT NULL DEFAULT 1,
                    created_at %s,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )%s
                """.formatted(pk, fkType, fkType, boolDefault, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS bookings (
                    id %s,
                    user_id %s NOT NULL,
                    rental_id %s NOT NULL,
                    guide_id %s NULL,
                    booking_code VARCHAR(40) NOT NULL UNIQUE,
                    start_date VARCHAR(20) NOT NULL,
                    end_date VARCHAR(20) NOT NULL,
                    guests_count INT NOT NULL DEFAULT 2,
                    total_price_idr BIGINT NOT NULL,
                    special_requests TEXT,
                    status VARCHAR(30) NOT NULL DEFAULT 'Pending',
                    created_at %s,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
                    FOREIGN KEY (guide_id) REFERENCES local_guides(id) ON DELETE SET NULL
                )%s
                """.formatted(pk, fkType, fkType, fkType, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS travel_stories (
                    id %s,
                    user_id %s NOT NULL,
                    destination_id %s NOT NULL,
                    title VARCHAR(160) NOT NULL,
                    rating INT NOT NULL,
                    visited_month VARCHAR(40) NOT NULL,
                    story_body TEXT NOT NULL,
                    photo_url VARCHAR(500) NOT NULL,
                    travel_tip VARCHAR(300),
                    created_at %s,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
                )%s
                """.formatted(pk, fkType, fkType, tsDefault, engine));

            st.execute("""
                CREATE TABLE IF NOT EXISTS saved_places (
                    id %s,
                    user_id %s NOT NULL,
                    destination_id %s NOT NULL,
                    target_season VARCHAR(80) NOT NULL,
                    priority_level VARCHAR(40) NOT NULL DEFAULT 'Must Visit',
                    personal_note TEXT,
                    created_at %s,
                    UNIQUE (user_id, destination_id),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
                )%s
                """.formatted(pk, fkType, fkType, tsDefault, engine));
        }
    }

    private static void seedAll(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String pw = hashPassword("password");

            // 1. Seed Users (Curators + Travelers including team members)
            long curatorId = insertAndGetId(conn,
                "INSERT INTO users (full_name, email, password_hash, role, nationality, phone, bio) VALUES (?,?,?,?,?,?,?)",
                "Alya Prameswari (Editorial Curator)", "curator@wildindonesia.id", pw, "curator", "Indonesia", "+62 811-3040-882",
                "Lead Nusantara field editor documenting volcanic calderas, eastern reef corridors, and sustainable homestay rentals.");
            long editor2Id = insertAndGetId(conn,
                "INSERT INTO users (full_name, email, password_hash, role, nationality, phone, bio) VALUES (?,?,?,?,?,?,?)",
                "Rinaltra Nabasa S", "rinaltra@wildindonesia.id", pw, "curator", "Indonesia", "+62 812-5025-1024",
                "Visual director and archipelago route designer based in Surabaya.");
            long travelerId = insertAndGetId(conn,
                "INSERT INTO users (full_name, email, password_hash, role, nationality, phone, bio) VALUES (?,?,?,?,?,?,?)",
                "Nadia Kusuma (Explorer)", "traveler@wildindonesia.id", pw, "traveler", "Indonesia", "+62 813-9022-4110",
                "Slow-travel photographer chasing sunrise craters across Java and Flores.");
            long fakhrianId = insertAndGetId(conn,
                "INSERT INTO users (full_name, email, password_hash, role, nationality, phone, bio) VALUES (?,?,?,?,?,?,?)",
                "Fakhrian Elanta", "fakhrian@wildindonesia.id", pw, "traveler", "Indonesia", "+62 815-5025-1019",
                "Quality control engineer and overland expedition tester across East Java & Bali.");
            long haidarId = insertAndGetId(conn,
                "INSERT INTO users (full_name, email, password_hash, role, nationality, phone, bio) VALUES (?,?,?,?,?,?,?)",
                "Haidar Abiyyu at Taqy", "haidar@wildindonesia.id", pw, "traveler", "Indonesia", "+62 816-5025-1121",
                "Marine logistics planner and Komodo liveaboard route researcher.");
            insertAndGetId(conn,
                "INSERT INTO users (full_name, email, password_hash, role, nationality, phone, bio) VALUES (?,?,?,?,?,?,?)",
                "Soren Lindqvist", "soren@nordictrail.org", pw, "traveler", "Sweden", "+46 70-482-1900",
                "Freediver and trail runner exploring Sulawesi and Raja Ampat outer atolls.");

            // 2. Seed 5 Indonesian Island Regions (Verified HTTP 200 Unsplash URLs)
            long javaId = insertAndGetId(conn,
                "INSERT INTO regions (name, code, island_group, climate_notes, best_season, cover_image, summary) VALUES (?,?,?,?,?,?,?)",
                "Java & Volcanic Spine", "ID-JAVA", "Greater Sunda Islands",
                "Cool highland mornings at 8-14 C around Bromo and Ijen; warm tropical plains along Yogyakarta.",
                "May - September (Dry Monsoon)",
                "https://images.unsplash.com/photo-1530172202330-0b30ddcfc7b5?auto=format&fit=crop&w=1200&q=80",
                "Ancient Javanese temple plains, sulfur-blue volcanic craters, and dramatic sea-of-sand calderas linked by scenic rail and mountain passes.");
            long baliId = insertAndGetId(conn,
                "INSERT INTO regions (name, code, island_group, climate_notes, best_season, cover_image, summary) VALUES (?,?,?,?,?,?,?)",
                "Bali & Nusa Islands", "ID-BALI", "Lesser Sunda (West)",
                "Breezy limestone cliffs on Nusa Penida; lush highland mist in Ubud and Bedugul lakes.",
                "April - October",
                "https://images.unsplash.com/photo-1576363399501-0dd08fc750e1?auto=format&fit=crop&w=1200&q=80",
                "UNESCO subak rice terraces, sea temples perched on basalt headlands, and turquoise manta ray coves across Penida and Lembongan.");
            long floresId = insertAndGetId(conn,
                "INSERT INTO regions (name, code, island_group, climate_notes, best_season, cover_image, summary) VALUES (?,?,?,?,?,?,?)",
                "Komodo & Flores Archipelago", "ID-FLORES", "East Nusa Tenggara",
                "Arid golden savanna hills from June to September; emerald green ridges after January rains.",
                "April - November",
                "https://images.unsplash.com/photo-1657788405193-c2039edaad6b?auto=format&fit=crop&w=1200&q=80",
                "Tri-colored bays of Padar Island, UNESCO Komodo dragon sanctuaries, pink coral beaches, and traditional wooden phinisi sailing routes.");
            long papuaId = insertAndGetId(conn,
                "INSERT INTO regions (name, code, island_group, climate_notes, best_season, cover_image, summary) VALUES (?,?,?,?,?,?,?)",
                "Raja Ampat & West Papua", "ID-PAPUA", "Maluku & Papua",
                "Calm glassy seas between October and April with underwater visibility exceeding 25 meters.",
                "October - April",
                "https://images.unsplash.com/photo-1703769605296-b5ab3cece3d9?auto=format&fit=crop&w=1200&q=80",
                "The global epicenter of marine biodiversity: mushroom karst islets in Wayag, hidden turquoise lagoons in Misool, and quiet Papuan eco-homestays.");
            long sumatraId = insertAndGetId(conn,
                "INSERT INTO regions (name, code, island_group, climate_notes, best_season, cover_image, summary) VALUES (?,?,?,?,?,?,?)",
                "Sumatra & Eastern Ridges", "ID-SUMATRA", "Western Archipelago",
                "Equatorial rainforest humidity with crisp volcanic lake breezes across highland plateaus.",
                "May - September",
                "https://images.unsplash.com/photo-1559628233-100c798642d4?auto=format&fit=crop&w=1200&q=80",
                "Wild rainforest canopies, volcanic caldera lakes, traditional wooden outriggers, and untouched surf coves along Indonesia's western frontier.");

            // 3. Seed 12 Authentic Indonesian Destinations
            long bromoId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                javaId, "Mount Bromo & Tengger Caldera", "mount-bromo-tengger", "East Java", "Volcano & Highland",
                220000, 2, -7.9425, 112.9530,
                "https://images.unsplash.com/photo-1530172202330-0b30ddcfc7b5?auto=format&fit=crop&w=1200&q=80",
                "Dawn breaks over three smoking volcanic cones rising from an ancient ten-kilometer caldera floor.",
                "Situated inside Bromo Tengger Semeru National Park, Mount Bromo (2,329 m) offers one of Southeast Asia's most accessible active volcanic panoramas. Travelers ascend Penanjakan or King Kong Hill before 04:30 WIB, then cross the Whispering Sands by 4x4 Land Cruiser to climb the 250 steps up the crater rim.",
                "Pack a windproof down jacket (temperatures drop to 6 C at dawn), bring a dust mask for the Sea of Sand, and book a Cemoro Lawang stay the night prior.",
                1);

            long ijenId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                javaId, "Kawah Ijen Crater Lake", "kawah-ijen-crater", "East Java", "Volcano & Highland",
                150000, 2, -8.0583, 114.2422,
                "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?auto=format&fit=crop&w=1200&q=80",
                "The world's largest acidic turquoise crater lake crowned by surreal midnight sulfur vents.",
                "Located between Banyuwangi and Bondowoso, Kawah Ijen features a 3 km trek from Paltuding Basecamp climbing 500 vertical meters to the caldera rim. At first light, the milky-turquoise lake reveals dramatic vertical walls sculpted from hardened volcanic ash.",
                "Obtain an official health clearance certificate in Banyuwangi, wear proper hiking boots with ankle grip, and carry an industrial P100 respirator if descending near sulfur vents.",
                1);

            long borobudurId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                javaId, "Borobudur Temple Sanctuary", "borobudur-sanctuary", "Central Java", "Heritage & Temple",
                455000, 2, -7.6079, 110.2038,
                "https://images.unsplash.com/photo-1566559532224-6d65e9fc0f37?auto=format&fit=crop&w=1200&q=80",
                "Ninth-century Mahayana Buddhist step-pyramid overlooking misty Menoreh hills and Mount Merapi.",
                "Constructed from two million volcanic andesite blocks during the Sailendra dynasty, Borobudur rises across nine stacked terraces crowned by 72 perforated bell-shaped stupas. Morning mist shrouds the surrounding palm valley while reliefs narrate ancient maritime trade.",
                "Reserve upper-structure access slots at least two weeks in advance; combine dawn at Setumbu Hill with a guided bicycle loop through nearby pottery villages.",
                1);

            long prambananId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                javaId, "Prambanan Hindu Spires", "prambanan-spires", "Yogyakarta", "Heritage & Temple",
                390000, 1, -7.7520, 110.4915,
                "https://images.unsplash.com/photo-1576233475048-55f08b53a8bd?auto=format&fit=crop&w=1200&q=80",
                "Towering 47-meter carved Trimurti sandstone spires glowing amber at dusk.",
                "Indonesia's grandest 9th-century Hindu temple compound dedicates its central spires to Shiva, Vishnu, and Brahma. Rent a bicycle inside the archaeological park to connect Ratu Boko hilltop palace before golden hour.",
                "Arrive around 15:30 WIB to escape midday heat and catch late sunlight across the western relief galleries.",
                0);

            long kelingkingId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                baliId, "Kelingking & Nusa Penida Cliffs", "kelingking-nusa-penida", "Bali", "Marine & Cliff",
                25000, 3, -8.7510, 115.4733,
                "https://images.unsplash.com/photo-1576363399501-0dd08fc750e1?auto=format&fit=crop&w=1200&q=80",
                "Towering limestone headlands dropping straight into electric-blue Indian Ocean swells.",
                "Just 40 minutes by fast boat from Sanur, Nusa Penida rewards explorers who stay overnight rather than rushing a day trip. Alongside Kelingking's dramatic headland, Diamond Beach, Crystal Bay, and Manta Point offer world-class snorkeling with reef mantas.",
                "Hire a local driver rather than riding steep gravel roads unless highly experienced on scooters; depart Sanur on the 07:00 WIB crossing.",
                1);

            long diamondId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                baliId, "Diamond Cove & Atuh Ridge", "diamond-cove-atuh", "Bali", "Marine & Cliff",
                35000, 2, -8.7745, 115.6203,
                "https://images.unsplash.com/photo-1577717903315-1691ae25ab3f?auto=format&fit=crop&w=1200&q=80",
                "White carbonate sand tucked beneath carved limestone stairs on Penida's eastern coast.",
                "Looking east toward Lombok's Mount Rinjani on clear mornings, Diamond Cove features towering sea stacks and crystal shallows framed by hand-carved cliffside steps.",
                "Visit at 06:30 WITA for calm light and uncrowded stairs; swimming is discouraged when strong rip currents surge over the reef.",
                0);

            long ubudId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                baliId, "Tegallalang & Jatiluwih Terraces", "tegallalang-jatiluwih", "Bali", "Culture & Rice Terrace",
                50000, 3, -8.4317, 115.2792,
                "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80",
                "Centuries-old subak communal irrigation sculpting emerald valleys across central Bali.",
                "From Tegallalang's steep coconut-lined ridges north of Ubud to the sprawling UNESCO Jatiluwih amphitheater beneath Mount Batukaru, Bali's subak water-temple cooperative balances ecology and highland agriculture.",
                "Walk Jatiluwih's 5.5 km red trail early morning after renting an electric trail bike, then stop at a family warung for red rice harvest plates.",
                1);

            long beratanId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                baliId, "Ulun Danu Beratan Lake", "ulun-danu-beratan", "Bali", "Culture & Rice Terrace",
                75000, 2, -8.2752, 115.1668,
                "https://images.unsplash.com/photo-1555400038-63f5ba517a47?auto=format&fit=crop&w=1200&q=80",
                "Eleven-tiered meru water temple floating atop glassy crater lake mist in Bedugul.",
                "Perched at 1,200 meters in Tabanan's volcanic highlands, Pura Ulun Danu honors Dewi Danu, goddess of lakes and fertility. Surrounding botanical gardens and Twin Lakes ridge trails make Bedugul an ideal highland base.",
                "Arrive right at 07:00 WITA before highland cloud rolls down from Mount Catur; pair with Banyumala twin waterfalls nearby.",
                0);

            long padarId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                floresId, "Padar Island & Komodo Park", "padar-komodo-park", "East Nusa Tenggara", "Archipelago & Safari",
                450000, 4, -8.6472, 119.5754,
                "https://images.unsplash.com/photo-1657788405193-c2039edaad6b?auto=format&fit=crop&w=1200&q=80",
                "Rugged dragon-spine ridges dividing white, black, and pink coral coves across the Flores Sea.",
                "Departing from Labuan Bajo harbor aboard a traditional Sulawesi wooden phinisi, travelers explore Padar's iconic three-bay summit, walk beside wild Komodo monitors on Rinca Island with park rangers, and drift over Taka Makassar's crescent sandbar.",
                "Book a 3D2N liveaboard charter rather than a hurried speedboat; bring reef-safe mineral sunscreen and 2 liters of water for the Padar ridge climb.",
                1);

            long labuanId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                floresId, "Kalong & Pink Coral Lagoons", "kalong-pink-lagoons", "East Nusa Tenggara", "Marine & Reef",
                150000, 2, -8.5988, 119.4882,
                "https://images.unsplash.com/photo-1736523075712-398e8a18767a?auto=format&fit=crop&w=1200&q=80",
                "Crimson-coral shallows and mangrove anchorages beneath thousands of sunset flying foxes.",
                "Komodo National Park's sheltered bays create kaleidoscopic reef gardens accessible directly from wooden tender boats. At dusk near Kalong Island, vast colonies of fruit bats stream across amber skies toward Flores.",
                "Bring your own well-fitting mask and fins for drift snorkeling along Batu Bolong and Siaba Besar turtle channels.",
                0);

            long wayagId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                papuaId, "Wayag & Piaynemo Karst Lagoons", "wayag-piaynemo-karst", "Southwest Papua", "Marine & Reef",
                1000000, 5, 0.1722, 130.0306,
                "https://images.unsplash.com/photo-1703769605296-b5ab3cece3d9?auto=format&fit=crop&w=1200&q=80",
                "Hundreds of jungle-clad limestone domes scattered across jade lagoons on the equator.",
                "Reaching Raja Ampat requires flying into Sorong, catching the morning ferry to Waisai, and navigating remote boat passages to Piaynemo and equatorial Wayag. Locally run Papuan homestays on Kri, Arborek, and Gam put travelers steps away from house reefs teeming with schooling fusiliers and wobbegong sharks.",
                "Purchase the annual Raja Ampat Marine Park Pin in Waisai, carry sufficient IDR cash from Sorong, and share longboat fuel costs with fellow homestay guests.",
                1);

            long misoolId = insertAndGetId(conn,
                "INSERT INTO destinations (region_id, name, slug, province, category, entry_fee_idr, recommended_days, latitude, longitude, hero_image, summary, guide_overview, travel_tips, is_featured) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                papuaId, "East Misool Atolls & Jellyfish Lake", "east-misool-atolls", "Southwest Papua", "Marine & Reef",
                1000000, 5, -1.9445, 130.4833,
                "https://images.unsplash.com/photo-1637060532519-1b9675514711?auto=format&fit=crop&w=1200&q=80",
                "Southern Raja Ampat limestone mazes hiding stingless jellyfish lakes and ancient cliff rock art.",
                "More remote than northern Dampier Strait, East Misool protects pristine soft-coral gardens, sacred sea caves with prehistoric red hand stencils, and heart-shaped Dafalen karst lagoons.",
                "Plan transfers around twice-weekly Sorong-Misool vessels or eco-resort boat schedules between November and March.",
                0);

            // 4. Seed 10 Wild Expedition Rentals
            long r1 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                padarId, "KLM Nusantara 24m Wooden Phinisi", "Phinisi Yacht", "Wild Fleet Labuan Bajo", 14500000, 10, "5 Cabins · Twin Marine Diesel · Tender Boat",
                "https://images.unsplash.com/photo-1559628233-100c798642d4?auto=format&fit=crop&w=1200&q=80",
                "Captain & 4 crew, all island meals, snorkel sets, paddleboards, park anchorage permits.", 1);
            long r2 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                bromoId, "1982 Toyota Land Cruiser FJ40 Expedition", "4x4 Volcano Jeep", "Tengger Overland Garage", 950000, 5, "4WD Manual · High-Clearance Sand Tires",
                "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?auto=format&fit=crop&w=1200&q=80",
                "Experienced mountain driver, fuel for 4 caldera stops, dawn thermal blankets, hot coffee flask.", 1);
            long r3 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                ubudId, "Bamboo Canopy Ridge Eco-Villa", "Eco Villa", "Wild Sanctuary Ubud", 1850000, 4, "2 Open-Air Bedrooms · Saltwater Plunge Pool",
                "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80",
                "Organic farm breakfast, 150 Mbps fiber Wi-Fi, morning yoga deck, filtered spring water.", 1);
            long r4 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                kelingkingId, "Yamaha XMAX 250 Coastal Tourer", "Island Motorbike", "Penida Drift Rentals", 320000, 2, "Automatic 250cc · Dual ABS · Phone Mount",
                "https://images.unsplash.com/photo-1577717903315-1691ae25ab3f?auto=format&fit=crop&w=1200&q=80",
                "Two full-face helmets, waterproof dry bag, harbor delivery at Toyapakeh pier, roadside swap.", 1);
            long r5 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                wayagId, "Fiberglass Twin-Outboard Karst Longboat", "Phinisi Yacht", "Waisai Marine Co-op", 3800000, 8, "Twin 40HP Yamaha · GPS Chartplotter · Canopy",
                "https://images.unsplash.com/photo-1703769605296-b5ab3cece3d9?auto=format&fit=crop&w=1200&q=80",
                "Papuan skipper, life vests, cooler box with ice, reef-safe dry bags.", 1);
            long r6 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                ijenId, "Crater Rim Expedition Kit (2-Person)", "Expedition Gear", "Banyuwangi Basecamp", 280000, 2, "P100 Respirators · 1000lm Headlamps · Poles",
                "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?auto=format&fit=crop&w=1200&q=80",
                "Dual sulfur gas masks, insulated windshells, carbon trekking poles, trail map.", 1);
            long r7 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                borobudurId, "Menoreh Valley Heritage E-Bike Fleet", "Island Motorbike", "Magelang Slow Trails", 240000, 1, "Pedal-Assist Bosch Drive · 70 km Range",
                "https://images.unsplash.com/photo-1566559532224-6d65e9fc0f37?auto=format&fit=crop&w=1200&q=80",
                "Helmet, GPS village audio guide, mid-ride herbal jamu tasting stop.", 1);
            long r8 = insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                misoolId, "Overwater Sago-Palm Coral Bungalow", "Eco Villa", "Misool Community Stay", 1250000, 2, "Stilt Cabin over House Reef · Solar Power",
                "https://images.unsplash.com/photo-1637060532519-1b9675514711?auto=format&fit=crop&w=1200&q=80",
                "Three home-cooked seafood & sago meals daily, kayak access, freshwater bucket shower.", 1);
            insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                beratanId, "Suzuki Jimny 4x4 Highland Camper", "4x4 Volcano Jeep", "Tabanan Overland", 880000, 3, "4WD · Rooftop Tent · Camp Stove",
                "https://images.unsplash.com/photo-1555400038-63f5ba517a47?auto=format&fit=crop&w=1200&q=80",
                "Rooftop sleeping mattress, sleeping bags for 10 C, kettle & Kintamani coffee beans.", 1);
            insertAndGetId(conn,
                "INSERT INTO rentals (destination_id, title, rental_type, provider_name, price_per_day_idr, capacity_guests, transmission_or_spec, image_url, inclusions, is_available) VALUES (?,?,?,?,?,?,?,?,?,?)",
                labuanId, "Speedboat Day-Hopper 500HP", "Phinisi Yacht", "Flores Horizon Charters", 7200000, 12, "Twin 250HP Outboards · Shaded Cabin",
                "https://images.unsplash.com/photo-1516690561799-46d8f74f9abf?auto=format&fit=crop&w=1200&q=80",
                "Captain, guide, lunch boxes, chilled water, snorkel masks & fins.", 0);

            // 5. Seed 8 Certified Local Guides
            long g1 = insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                padarId, "Kornelius Manggarai", "Komodo Marine & Trekking", "Indonesian, English", 11, 750000, "HPI-NTT-2015-084",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
                "Born in Flores with over a decade navigating Komodo currents and Padar sunrise photography ridges.", 1);
            long g2 = insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                bromoId, "Budi Hartono", "Volcanic Geology & Astrophotography", "Indonesian, Javanese, English", 9, 550000, "HPI-JTM-2017-219",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=80",
                "East Java mountain leader specializing in quiet lesser-known Bromo ridge trails away from jeep crowds.", 1);
            long g3 = insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                borobudurId, "Ratna Wijaya", "Javanese Archaeology & Batik", "Indonesian, English, Japanese", 14, 650000, "HPI-JTG-2012-031",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                "Cultural historian trained in Sailendra reliefs and rural Menoreh artisan crafts.", 1);
            long g4 = insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                kelingkingId, "I Made Agus Suryawan", "Freediving & Cliff Safety", "Indonesian, Balinese, English", 8, 600000, "HPI-BAL-2018-412",
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80",
                "Certified rescue diver and Penida resident who tracks seasonal manta cleaning stations safely.", 1);
            long g5 = insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                wayagId, "Yohanes Mambrasar", "Papuan Reef & Birding Guide", "Indonesian, English", 10, 850000, "HPI-PBD-2016-019",
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=800&q=80",
                "Conservation ranger from Gam Island guiding Wilson's bird-of-paradise dawn treks and Wayag passages.", 1);
            insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                ijenId, "Siti Nurhaliza", "Sulfur Crater Safety & Coffee Trail", "Indonesian, English", 6, 500000, "HPI-JTM-2020-504",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80",
                "Banyuwangi mountaineer pairing Kawah Ijen sunrise hikes with Osing village robusta tastings.", 1);
            insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                ubudId, "Ni Luh Putu Sari", "Subak Ecology & Culinary Walks", "Indonesian, English, French", 7, 520000, "HPI-BAL-2019-177",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
                "Ethnobotanist leading serene morning walks through Jatiluwih terraces and village spice gardens.", 1);
            insertAndGetId(conn,
                "INSERT INTO local_guides (destination_id, full_name, specialty, languages, years_experience, daily_rate_idr, license_number, photo_url, bio, is_verified) VALUES (?,?,?,?,?,?,?,?,?,?)",
                prambananId, "Bagus Santoso", "Ramayana Heritage & Architecture", "Indonesian, English", 5, 450000, "HPI-DIY-2021-093",
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
                "Architecture graduate mapping Hindu-Javanese candi geometry across Prambanan and Sewu.", 1);

            // 6. Seed 6 Nusantara Itineraries
            insertAndGetId(conn,
                "INSERT INTO itineraries (user_id, region_id, title, duration_days, estimated_budget_idr, pace, route_stops, day_by_day_plan, cover_image, is_public) VALUES (?,?,?,?,?,?,?,?,?,?)",
                curatorId, javaId, "Java Volcanoes & Ancient Stones (Surabaya - Yogya)", 5, 6800000, "Active Explorer",
                "Surabaya -> Mount Bromo -> Kawah Ijen -> Solo -> Borobudur",
                "Day 1: Train to Probolinggo, ascend Cemoro Lawang.\nDay 2: 04:00 Bromo FJ40 caldera circuit, transfer to Banyuwangi.\nDay 3: Kawah Ijen turquoise crater hike.\nDay 4: Scenic Southern Java rail to Yogyakarta & Prambanan sunset.\nDay 5: Borobudur dawn mist & Menoreh e-bike loop.",
                "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?auto=format&fit=crop&w=1200&q=80", 1);
            insertAndGetId(conn,
                "INSERT INTO itineraries (user_id, region_id, title, duration_days, estimated_budget_idr, pace, route_stops, day_by_day_plan, cover_image, is_public) VALUES (?,?,?,?,?,?,?,?,?,?)",
                editor2Id, floresId, "Komodo Phinisi Drift: 3D2N Archipelago Loop", 3, 9500000, "Sailing & Island Hop",
                "Labuan Bajo -> Kelor -> Padar Island -> Pink Beach -> Taka Makassar -> Kanawa",
                "Day 1: Board phinisi at 10:00 WITA, snorkel Kelor Reef, sunset bat flight at Kalong.\nDay 2: Dawn trek on Padar Island ridges, Komodo ranger walk, Pink Coral cove.\nDay 3: Swim with mantas at Makassar sandbar, return to harbor by 15:00.",
                "https://images.unsplash.com/photo-1657788405193-c2039edaad6b?auto=format&fit=crop&w=1200&q=80", 1);
            insertAndGetId(conn,
                "INSERT INTO itineraries (user_id, region_id, title, duration_days, estimated_budget_idr, pace, route_stops, day_by_day_plan, cover_image, is_public) VALUES (?,?,?,?,?,?,?,?,?,?)",
                travelerId, baliId, "Slow Subak & Outer Cliffs of Bali", 6, 5400000, "Slow Culture",
                "Ubud -> Jatiluwih -> Bedugul -> Sanur -> Nusa Penida -> Atuh",
                "Day 1-2: Eco-villa stay in Ubud, morning walk along Tegallalang.\nDay 3: Jimny drive to Ulun Danu Beratan & Jatiluwih terraces.\nDay 4-6: Cross to Nusa Penida, rent XMAX 250, explore Kelingking & Diamond Cove.",
                "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80", 1);
            insertAndGetId(conn,
                "INSERT INTO itineraries (user_id, region_id, title, duration_days, estimated_budget_idr, pace, route_stops, day_by_day_plan, cover_image, is_public) VALUES (?,?,?,?,?,?,?,?,?,?)",
                haidarId, papuaId, "Wayag & Dampier Coral Homestay Trail", 6, 14200000, "Remote Expedition",
                "Sorong -> Waisai -> Kri Island -> Piaynemo -> Wayag -> Arborek",
                "Day 1: Ferry from Sorong to Waisai, longboat to Kri house reef.\nDay 2: Dawn Wilson's bird-of-paradise walk in Gam.\nDay 3-4: Longboat north across the equator to Wayag karst peaks.\nDay 5-6: Piaynemo lagoon overlook & Arborek village snorkel.",
                "https://images.unsplash.com/photo-1703769605296-b5ab3cece3d9?auto=format&fit=crop&w=1200&q=80", 1);
            insertAndGetId(conn,
                "INSERT INTO itineraries (user_id, region_id, title, duration_days, estimated_budget_idr, pace, route_stops, day_by_day_plan, cover_image, is_public) VALUES (?,?,?,?,?,?,?,?,?,?)",
                fakhrianId, javaId, "Yogyakarta Sacred Stone & Artisan Loop", 3, 2900000, "Budget Heritage",
                "Kotagede -> Prambanan -> Ratu Boko -> Borobudur -> Setumbu",
                "Day 1: Silver craft walk in Kotagede & dusk spires at Prambanan.\nDay 2: Dawn Setumbu hill vista & Borobudur upper terraces.\nDay 3: Coffee roastery and Merapi volcanic slope museum.",
                "https://images.unsplash.com/photo-1576233475048-55f08b53a8bd?auto=format&fit=crop&w=1200&q=80", 1);
            insertAndGetId(conn,
                "INSERT INTO itineraries (user_id, region_id, title, duration_days, estimated_budget_idr, pace, route_stops, day_by_day_plan, cover_image, is_public) VALUES (?,?,?,?,?,?,?,?,?,?)",
                curatorId, papuaId, "South Misool Atoll & Cave Sanctuary", 5, 16500000, "Marine Conservation",
                "Sorong -> Yellu -> East Misool -> Lenmakana Jellyfish Lake -> Harfat Peak",
                "Day 1: Overnight vessel transfer south from Sorong.\nDay 2: Overwater sago bungalow check-in & coral wall drift.\nDay 3-4: Stingless jellyfish lake & prehistoric cliff art caves.\nDay 5: Harfat summit view & return to Sorong.",
                "https://images.unsplash.com/photo-1637060532519-1b9675514711?auto=format&fit=crop&w=1200&q=80", 1);

            // 7. Seed 6 Rental & Guide Bookings
            insertAndGetId(conn,
                "INSERT INTO bookings (user_id, rental_id, guide_id, booking_code, start_date, end_date, guests_count, total_price_idr, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                travelerId, r2, g2, "WLD-2026-8410", "2026-12-10", "2026-12-12", 4, 3000000,
                "Pickup at Cemoro Lawang homestay at 03:15 WIB; bring two extra blankets.", "Confirmed");
            insertAndGetId(conn,
                "INSERT INTO bookings (user_id, rental_id, guide_id, booking_code, start_date, end_date, guests_count, total_price_idr, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                travelerId, r4, g4, "WLD-2026-8419", "2026-12-18", "2026-12-20", 2, 1840000,
                "Deliver bike to Toyapakeh Harbor at 08:30 WITA upon Sanur ferry arrival.", "Pending");
            insertAndGetId(conn,
                "INSERT INTO bookings (user_id, rental_id, guide_id, booking_code, start_date, end_date, guests_count, total_price_idr, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                haidarId, r1, g1, "WLD-2026-7902", "2026-12-22", "2026-12-25", 8, 45750000,
                "Halal galley kitchen preferred; vegetarian meal option for two crew guests.", "Confirmed");
            insertAndGetId(conn,
                "INSERT INTO bookings (user_id, rental_id, guide_id, booking_code, start_date, end_date, guests_count, total_price_idr, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                fakhrianId, r6, null, "WLD-2026-7831", "2026-11-28", "2026-11-29", 2, 280000,
                "Size M and L windshell jackets with spare AA batteries for headlamps.", "Completed");
            insertAndGetId(conn,
                "INSERT INTO bookings (user_id, rental_id, guide_id, booking_code, start_date, end_date, guests_count, total_price_idr, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                travelerId, r3, null, "WLD-2026-8501", "2027-01-05", "2027-01-08", 2, 5550000,
                "Quiet corner villa facing morning rice terrace light.", "Pending");
            insertAndGetId(conn,
                "INSERT INTO bookings (user_id, rental_id, guide_id, booking_code, start_date, end_date, guests_count, total_price_idr, special_requests, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                fakhrianId, r5, g5, "WLD-2026-8612", "2027-02-12", "2027-02-15", 6, 13950000,
                "Include snorkeling stop at Arborek jetty and Piaynemo homestay pickup.", "Pending");

            // 8. Seed 8 Field Stories & Reviews
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                travelerId, bromoId, "First Light Over the Tengger Sea of Sand", 5, "September 2026",
                "We left Cemoro Lawang in the 1982 Land Cruiser while Orion still hung sharp above the caldera. As twilight shifted amber-indigo, mist drifted through the valley separating Mount Batok and smoking Bromo before a soul reached the lower ridge.",
                "https://images.unsplash.com/photo-1530172202330-0b30ddcfc7b5?auto=format&fit=crop&w=1200&q=80",
                "Walk 15 minutes past the main concrete viewpoint on King Kong Hill for unobstructed framing.");
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                haidarId, padarId, "Three Bays From One Ridge in Komodo", 5, "August 2026",
                "Sailing through Linta Strait on a timber phinisi sets an unhurried rhythm impossible on day boats. Climbing Padar before sunrise gave us cool savanna breezes and panoramic views across white, charcoal, and rose-pink beaches below.",
                "https://images.unsplash.com/photo-1657788405193-c2039edaad6b?auto=format&fit=crop&w=1200&q=80",
                "Wear proper trail shoes—the dry volcanic scree near the summit ridge gets slick on the descent.");
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                fakhrianId, kelingkingId, "Dawn Above Kelingking Limestone Headlands", 5, "October 2026",
                "Staying on Nusa Penida overnight transformed the island. At 06:15 the viewing ridge was quiet save for frigatebirds riding thermal updrafts over churning white foam 200 meters below.",
                "https://images.unsplash.com/photo-1576363399501-0dd08fc750e1?auto=format&fit=crop&w=1200&q=80",
                "Park by 06:30 and save East Nusa Penida for a second day so you are never rushed back to the pier.");
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                curatorId, borobudurId, "Volcanic Andesite Amid Morning Palm Mist", 5, "July 2026",
                "Walking barefoot along the upper Arupadhatu terraces while morning valley mist curls around Menoreh Hill reveals why Borobudur remains one of humanity's finest stone monuments.",
                "https://images.unsplash.com/photo-1566559532224-6d65e9fc0f37?auto=format&fit=crop&w=1200&q=80",
                "Book the 08:30 structure-climb ticket online two weeks ahead and bring socks for the upanat sandals.");
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                editor2Id, wayagId, "Glassy Jade Channels at the Equator", 5, "November 2026",
                "Stepping out of our Kri Island timber bungalow directly onto healthy staghorn coral felt unreal. In Wayag, our boat captain cut the outboard engine so we could drift silently among vertical limestone spires.",
                "https://images.unsplash.com/photo-1703769605296-b5ab3cece3d9?auto=format&fit=crop&w=1200&q=80",
                "Pack biodegradable reef sunscreen and bring dry bags for camera gear on open longboat crossings.");
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                travelerId, ubudId, "Following Subak Water Canals in Jatiluwih", 4, "August 2026",
                "Unlike crowded central Ubud streets, Jatiluwih's wide green amphitheater felt timeless. Farmers greeted us along narrow earthen embankments while Mount Batukaru poked above light highland clouds.",
                "https://images.unsplash.com/photo-1557093793-d149a38a1be8?auto=format&fit=crop&w=1200&q=80",
                "Start walking the Red Route by 07:30 before midday tropical sun hits the terraces.");
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                fakhrianId, ijenId, "Turquoise Acid Mirror Above Banyuwangi", 5, "September 2026",
                "Renting proper P100 masks from basecamp made the steep crater approach safe and comfortable. Seeing daylight turn the crater lake neon turquoise from a sheer volcanic rim is unforgettable.",
                "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?auto=format&fit=crop&w=1200&q=80",
                "Avoid rushing down on loose gravel at dawn—trekking poles save your knees on the return.");
            insertAndGetId(conn,
                "INSERT INTO travel_stories (user_id, destination_id, title, rating, visited_month, story_body, photo_url, travel_tip) VALUES (?,?,?,?,?,?,?,?)",
                curatorId, misoolId, "Quiet Sago Stilt Stays in East Misool", 5, "October 2026",
                "No Wi-Fi towers, just gentle lap of water beneath the floorboards and sea fans blooming right under the jetty. Community-owned Misool stays prove conservation and hospitality belong together.",
                "https://images.unsplash.com/photo-1637060532519-1b9675514711?auto=format&fit=crop&w=1200&q=80",
                "Download offline maps and Indonesian phrases before boarding the Sorong marine shuttle.");

            // 9. Seed 8 Saved Places (Personal Bucket-List)
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                travelerId, wayagId, "November 2026", "Must Visit", "Coordinate 5-day longboat share with 4 friends from Waisai pier.");
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                travelerId, padarId, "June 2027", "Must Visit", "Book phinisi cabin 3 months early for green-to-gold ridge transition.");
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                travelerId, ijenId, "May 2027", "Next Trip", "Pair with Banyuwangi night sleeper train from Surabaya Gubeng.");
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                travelerId, misoolId, "January 2027", "Dream List", "Explore Lenmakana stingless jellyfish lake and Dafalen lagoon.");
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                fakhrianId, bromoId, "December 2026", "Must Visit", "Test 4x4 FJ40 dawn pickup schedule for QA expedition review.");
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                fakhrianId, prambananId, "July 2027", "Next Trip", "Catch late afternoon light + Ramayana open-air stage.");
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                haidarId, diamondId, "August 2027", "Next Trip", "Walk down the cliffside stairs at 06:30 before tides rise.");
            insertAndGetId(conn,
                "INSERT INTO saved_places (user_id, destination_id, target_season, priority_level, personal_note) VALUES (?,?,?,?,?)",
                curatorId, beratanId, "September 2027", "Dream List", "Camp overnight in the Suzuki Jimny above Twin Lakes.");

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static long insertAndGetId(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        return -1;
    }
}
