-- ============================================================================
-- Wild Indonesia — Tourist Guide & Expedition Rental Platform
-- SQLite 3NF Relational Schema (9 Tables, Foreign Keys with Cascading Rules)
-- ============================================================================
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'traveler' CHECK (role IN ('traveler', 'curator')),
    nationality TEXT,
    phone TEXT,
    bio TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS regions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    island_group TEXT NOT NULL,
    climate_notes TEXT NOT NULL,
    best_season TEXT NOT NULL,
    cover_image TEXT NOT NULL,
    summary TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS destinations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    region_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    province TEXT NOT NULL,
    category TEXT NOT NULL,
    entry_fee_idr INTEGER NOT NULL DEFAULT 0,
    recommended_days INTEGER NOT NULL DEFAULT 2,
    latitude REAL,
    longitude REAL,
    hero_image TEXT NOT NULL,
    summary TEXT NOT NULL,
    guide_overview TEXT NOT NULL,
    travel_tips TEXT NOT NULL,
    is_featured INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rentals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    destination_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    rental_type TEXT NOT NULL,
    provider_name TEXT NOT NULL,
    price_per_day_idr INTEGER NOT NULL,
    capacity_guests INTEGER NOT NULL DEFAULT 2,
    transmission_or_spec TEXT NOT NULL,
    image_url TEXT NOT NULL,
    inclusions TEXT NOT NULL,
    is_available INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS local_guides (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    destination_id INTEGER NOT NULL,
    full_name TEXT NOT NULL,
    specialty TEXT NOT NULL,
    languages TEXT NOT NULL,
    years_experience INTEGER NOT NULL DEFAULT 3,
    daily_rate_idr INTEGER NOT NULL,
    license_number TEXT NOT NULL UNIQUE,
    photo_url TEXT NOT NULL,
    bio TEXT NOT NULL,
    is_verified INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS itineraries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    region_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    duration_days INTEGER NOT NULL DEFAULT 4,
    estimated_budget_idr INTEGER NOT NULL,
    pace TEXT NOT NULL,
    route_stops TEXT NOT NULL,
    day_by_day_plan TEXT NOT NULL,
    cover_image TEXT NOT NULL,
    is_public INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    rental_id INTEGER NOT NULL,
    guide_id INTEGER,
    booking_code TEXT NOT NULL UNIQUE,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    guests_count INTEGER NOT NULL DEFAULT 2,
    total_price_idr INTEGER NOT NULL,
    special_requests TEXT,
    status TEXT NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending', 'Confirmed', 'Completed', 'Cancelled')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    FOREIGN KEY (guide_id) REFERENCES local_guides(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS travel_stories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    destination_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    visited_month TEXT NOT NULL,
    story_body TEXT NOT NULL,
    photo_url TEXT NOT NULL,
    travel_tip TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS saved_places (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    destination_id INTEGER NOT NULL,
    target_season TEXT NOT NULL,
    priority_level TEXT NOT NULL DEFAULT 'Must Visit',
    personal_note TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (user_id, destination_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);
