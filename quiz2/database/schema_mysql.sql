-- ============================================================================
-- Wild Indonesia — Tourist Guide & Expedition Rental Platform
-- MySQL 8+ 3NF Relational Schema (9 Tables, Foreign Keys with Cascading Rules)
-- ============================================================================
CREATE DATABASE IF NOT EXISTS wild_indonesia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wild_indonesia;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('traveler', 'curator') NOT NULL DEFAULT 'traveler',
    nationality VARCHAR(80),
    phone VARCHAR(40),
    bio TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS regions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    island_group VARCHAR(100) NOT NULL,
    climate_notes TEXT NOT NULL,
    best_season VARCHAR(100) NOT NULL,
    cover_image VARCHAR(500) NOT NULL,
    summary TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS destinations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    region_id BIGINT UNSIGNED NOT NULL,
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
    is_featured TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dest_region FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rentals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    destination_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(160) NOT NULL,
    rental_type VARCHAR(60) NOT NULL,
    provider_name VARCHAR(120) NOT NULL,
    price_per_day_idr INT NOT NULL,
    capacity_guests INT NOT NULL DEFAULT 2,
    transmission_or_spec VARCHAR(120) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    inclusions TEXT NOT NULL,
    is_available TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rental_dest FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS local_guides (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    destination_id BIGINT UNSIGNED NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    languages VARCHAR(160) NOT NULL,
    years_experience INT NOT NULL DEFAULT 3,
    daily_rate_idr INT NOT NULL,
    license_number VARCHAR(60) NOT NULL UNIQUE,
    photo_url VARCHAR(500) NOT NULL,
    bio TEXT NOT NULL,
    is_verified TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_guide_dest FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS itineraries (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    region_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(160) NOT NULL,
    duration_days INT NOT NULL DEFAULT 4,
    estimated_budget_idr INT NOT NULL,
    pace VARCHAR(40) NOT NULL,
    route_stops VARCHAR(300) NOT NULL,
    day_by_day_plan TEXT NOT NULL,
    cover_image VARCHAR(500) NOT NULL,
    is_public TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_itin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_itin_region FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    rental_id BIGINT UNSIGNED NOT NULL,
    guide_id BIGINT UNSIGNED NULL,
    booking_code VARCHAR(40) NOT NULL UNIQUE,
    start_date VARCHAR(20) NOT NULL,
    end_date VARCHAR(20) NOT NULL,
    guests_count INT NOT NULL DEFAULT 2,
    total_price_idr BIGINT NOT NULL,
    special_requests TEXT,
    status ENUM('Pending', 'Confirmed', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_rental FOREIGN KEY (rental_id) REFERENCES rentals(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_guide FOREIGN KEY (guide_id) REFERENCES local_guides(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS travel_stories (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    destination_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(160) NOT NULL,
    rating INT NOT NULL,
    visited_month VARCHAR(40) NOT NULL,
    story_body TEXT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    travel_tip VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_story_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_story_dest FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS saved_places (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    destination_id BIGINT UNSIGNED NOT NULL,
    target_season VARCHAR(80) NOT NULL,
    priority_level VARCHAR(40) NOT NULL DEFAULT 'Must Visit',
    personal_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_saved_user_dest (user_id, destination_id),
    CONSTRAINT fk_saved_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_dest FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
) ENGINE=InnoDB;
