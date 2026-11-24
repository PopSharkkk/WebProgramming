# Wild Indonesia — JSP Tourist Guide & Expedition Rental Platform

**Course:** EF234301 Web Programming (Class D) — Quiz 2  
**Institution:** Department of Informatics, Institut Teknologi Sepuluh Nopember (ITS), Surabaya  
**Stack:** Java 21 · Jakarta Server Pages (JSP 3.1) · Jakarta Servlets 6.0 · JSTL 3.0 · Embedded Apache Tomcat 10.1.33 · Dual SQLite / MySQL JDBC  

---

## Team Members & Work Split (33.33% Equal Distribution)

| Team Member | Student ID (NRP) | Share | Core Responsibilities |
|---|---|:---:|---|
| **Rinaltra Nabasa S** | `5025251024` | 33.33% | Wild Pill Hero Web Animations (`index.jsp` & `wild-hero.html`), archival paper SVG curves, responsive UI/UX & JSP view styling |
| **Haidar Abiyyu at Taqy** | `5025521121` | 33.33% | 9-table 3NF CDM/PDM schema (`schema_sqlite.sql`, `schema_mysql.sql`), dual SQLite/MySQL JDBC `DatabaseManager`, `DataStore` DAO & Jakarta Servlets |
| **Fakhrian Elanta** | `5025251019` | 33.33% | Quality Control, QA Registered Accounts Inspector (`/debug/accounts`), JUnit 5 full-stack CRUD integration suite (`10/10` passing) & Quiz 2 Report |

---

## Quick Start (Local Development)

### Option A: One-Click Windows Launch
Double-click `start-server.bat` inside `quiz2/`. It automatically uses JDK 21, builds if needed, initializes SQLite (`data/wild_indonesia.db`), seeds all 9 tables, and opens `http://localhost:8080`.

### Option B: Command Line (Maven Wrapper)
```powershell
cd quiz2
.\mvnw.cmd package
java -cp "target\classes;target\dependency\*" com.wildindonesia.AppServer
```

To run the automated JUnit 5 integration tests:
```powershell
.\mvnw.cmd test
```

---

## Dual Database Configuration (SQLite & MySQL)

- **SQLite (Default):** Boots out-of-the-box with zero configuration at `data/wild_indonesia.db` (`PRAGMA foreign_keys = ON`). DDL reference: `database/schema_sqlite.sql`.
- **MySQL 8+:** Import `database/schema_mysql.sql`, then set environment variables before starting `AppServer`:
  ```powershell
  $env:DB_URL = "jdbc:mysql://localhost:3306/wild_indonesia?useSSL=false&allowPublicKeyRetrieval=true"
  $env:DB_USER = "root"
  $env:DB_PASS = "your_password"
  java -cp "target\classes;target\dependency\*" com.wildindonesia.AppServer
  ```

---

## Free Cloud Hosting Deployment (Render / Railway / Koyeb)

This directory includes a production multi-stage `Dockerfile` and `render.yaml`:
1. Push the repository to GitHub (when ready).
2. Create a new **Web Service** on [Render.com](https://render.com) (Free tier) pointing to root directory `quiz2` with runtime **Docker**.
3. Render automatically builds Java 21 + Maven, exposes `PORT=8080`, and launches `AppServer` with auto-seeded SQLite data.

---

## Implemented Features (Wild Hero + 8 CRUD Modules)

* **Landing Stage (`/` & `/wild-hero.html`):** Full-screen Wild Pill Hero opening from a hairline into a looping 60fps floating-island stadium pill pushing `WI` and `LD` outward (`?rec=1` recording loop supported).
* **01 · Destinations Guide CRUD (`/destinations`):** Filterable Indonesian landmarks (Bromo, Kawah Ijen, Borobudur, Nusa Penida, Padar, Raja Ampat) with Create, Edit, Spotlight Toggle, Detail view, and Delete.
* **02 · Island Regions CRUD (`/regions`):** Archipelago climate zones and monsoon windows with Create, Edit, and Delete.
* **03 · Wild Rentals Fleet CRUD (`/rentals`):** 24m wooden phinisi boats, 1982 FJ40 volcano jeeps, eco-villas, and coastal scooters with availability toggles and CRUD.
* **04 · Certified Local Guides CRUD (`/guides`):** HPI-licensed guides searchable by specialty and languages with verification toggles and CRUD.
* **05 · Nusantara Trip Itineraries CRUD (`/itineraries`):** Multi-day route planning with IDR budgets, route stops, and day-by-day views.
* **06 · Rental & Guide Bookings CRUD (`/bookings`):** Server-calculated IDR totals, date/request editing, status transitions (`Pending` → `Confirmed` → `Completed` / `Cancelled`), and cancellation.
* **07 · Traveler Field Stories CRUD (`/stories`):** Destination reviews, ratings (1–5), photography, and practical travel tips.
* **08 · Personal Bucket-List CRUD (`/saved`):** 1-click destination bookmarking, priority tiers, target travel season, and planning notes.
* **QA & Schema Tools:** Registered Accounts Inspector (`/debug/accounts`) and live CDM & PDM Diagrams (`/database-diagrams`).

---

## Demo Credentials (Password: `password`)

* **Curator (Admin):** `curator@wildindonesia.id`
* **Traveler:** `traveler@wildindonesia.id`
* **1-Click Session Switcher:** `http://localhost:8080/debug/accounts`
