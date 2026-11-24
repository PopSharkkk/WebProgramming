import os
import shutil
import zipfile
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

QUIZ2_DIR = r"C:\Users\Rinaltra\Documents\GitHub\WebProgramming\quiz2"
SHOT_DIR = r"C:\Users\Rinaltra\.gemini\antigravity\brain\a4b7e33e-038a-448d-9f13-d100a58d83ab\scratch\screenshots"
DOWNLOADS_DIR = r"C:\Users\Rinaltra\Downloads"

ZIP_NAME = "EF234301_WebPro_Q2_5025251024_RinaltraNabasaS_5025251019_FakhrianElanta_5025521121_HaidarAbiyyuatTaqy.ZIP"

COLOR_INK = RGBColor(29, 29, 29)
COLOR_SECONDARY = RGBColor(58, 58, 58)
COLOR_MUTED = RGBColor(100, 100, 96)
COLOR_ACCENT = RGBColor(15, 118, 110)

HEX_PRIMARY = "1D1D1D"
HEX_TEAL = "0F766E"
HEX_PAPER = "F2F2F1"


def set_cell_bg(cell, hex_color):
    shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
    cell._tc.get_or_add_tcPr().append(shading)


def set_cell_margins(cell, top=75, bottom=75, left=110, right=110):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)


def set_table_borders(table, color="D4D4D0", sz="4"):
    tblPr = table._tbl.tblPr
    borders = parse_xml(
        f'<w:tblBorders {nsdecls("w")}>'
        f'<w:top w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:bottom w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:left w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:right w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:insideH w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'<w:insideV w:val="single" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'</w:tblBorders>'
    )
    tblPr.append(borders)


def configure_doc(doc):
    for s in doc.sections:
        s.top_margin = Inches(0.8)
        s.bottom_margin = Inches(0.8)
        s.left_margin = Inches(0.85)
        s.right_margin = Inches(0.85)
    normal = doc.styles['Normal']
    normal.font.name = 'Calibri'
    normal.font.size = Pt(10.5)
    normal.font.color.rgb = COLOR_SECONDARY
    normal.paragraph_format.space_after = Pt(5)
    normal.paragraph_format.line_spacing = 1.18


def add_h1(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.size = Pt(14.5)
    r.font.bold = True
    r.font.color.rgb = COLOR_INK
    pPr = p._p.get_or_add_pPr()
    pBdr = parse_xml(f'<w:pBdr {nsdecls("w")}><w:bottom w:val="single" w:sz="8" w:space="3" w:color="{HEX_PRIMARY}"/></w:pBdr>')
    pPr.append(pBdr)
    return p


def add_h2(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.size = Pt(12)
    r.font.bold = True
    r.font.color.rgb = COLOR_ACCENT
    return p


def add_p(doc, text, bold_prefix=None):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    if bold_prefix:
        rp = p.add_run(bold_prefix)
        rp.font.bold = True
        rp.font.color.rgb = COLOR_INK
    r = p.add_run(text)
    r.font.color.rgb = COLOR_SECONDARY
    return p


def add_bullet(doc, lead, body):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.space_after = Pt(3)
    r1 = p.add_run(lead)
    r1.font.bold = True
    r1.font.color.rgb = COLOR_INK
    r2 = p.add_run(body)
    r2.font.color.rgb = COLOR_SECONDARY
    return p


def add_figure(doc, filename, caption, width_inches=6.2):
    path = os.path.join(SHOT_DIR, filename)
    if not os.path.exists(path):
        print(f"Warning: screenshot missing: {path}")
        return
    p_img = doc.add_paragraph()
    p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_img.paragraph_format.space_before = Pt(6)
    p_img.paragraph_format.space_after = Pt(3)
    p_img.paragraph_format.keep_with_next = True
    p_img.add_run().add_picture(path, width=Inches(width_inches))

    p_cap = doc.add_paragraph()
    p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_cap.paragraph_format.space_after = Pt(8)
    rc = p_cap.add_run(caption)
    rc.font.italic = True
    rc.font.size = Pt(9)
    rc.font.color.rgb = COLOR_MUTED


def build_report():
    doc = docx.Document()
    configure_doc(doc)

    # Header & Footer
    sec = doc.sections[0]
    hp = sec.header.paragraphs[0]
    hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    hr = hp.add_run("EF234301 Web Programming (D) Quiz 2  |  Wild Indonesia JSP Platform")
    hr.font.size = Pt(8.5)
    hr.font.color.rgb = COLOR_MUTED

    # Cover Page
    p_inst = doc.add_paragraph()
    p_inst.paragraph_format.space_before = Pt(18)
    r_inst = p_inst.add_run("INSTITUT TEKNOLOGI SEPULUH NOPEMBER\nDEPARTMENT OF INFORMATICS · CLASS EF234301 WEB PROGRAMMING (D)")
    r_inst.font.bold = True
    r_inst.font.size = Pt(10)
    r_inst.font.color.rgb = COLOR_ACCENT

    p_title = doc.add_paragraph()
    p_title.paragraph_format.space_before = Pt(16)
    p_title.paragraph_format.space_after = Pt(6)
    rt = p_title.add_run("Wild Indonesia: Archipelago Tourist Guide & Expedition Rental Portal")
    rt.font.bold = True
    rt.font.size = Pt(24)
    rt.font.color.rgb = COLOR_INK

    p_sub = doc.add_paragraph()
    p_sub.paragraph_format.space_after = Pt(16)
    rs = p_sub.add_run("Quiz 2 Technical Project Report — Jakarta Server Pages (JSP 3.1), Dual SQLite/MySQL 3NF Database, Wild Pill Hero Motion Architecture, and 8 Full-Stack CRUD Features")
    rs.font.size = Pt(12)
    rs.font.color.rgb = COLOR_SECONDARY

    meta_tbl = doc.add_table(rows=7, cols=2)
    meta_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(meta_tbl, color="D4D4D0", sz="4")
    meta_rows = [
        ("Course & Exam", "EF234301 Web Programming (D) — Quiz 2"),
        ("Technology Stack", "Java 21, Jakarta Server Pages (JSP 3.1), Jakarta Servlets 6.0, JSTL 3.0, Embedded Tomcat 10.1.33"),
        ("Database Engines", "SQLite 3 (JDBC 3.47.1.0 embedded default) & MySQL 8+ (Connector/J 9.1.0 configurable)"),
        ("Automated Test Suite", "JUnit 5 Integration Tests — 10 Tests / 0 Failures / 100% Pass Rate"),
        ("Instructors & TAs", "MM Irfan Subakti | Callista Fidelya Roba Gultom, Kevin Andreas"),
        ("Submission Date", "Wednesday, 25 November 2026"),
        ("Authors & Work Split",
         "1. Rinaltra Nabasa S (NRP: 5025251024) — 33.33% (Wild Pill Hero & Frontend Architecture)\n"
         "2. Haidar Abiyyu at Taqy (NRP: 5025521121) — 33.33% (3NF Database Design & Servlet/DAO Backend)\n"
         "3. Fakhrian Elanta (NRP: 5025251019) — 33.33% (Quality Control, QA Switcher, JUnit 5 Suite & Report)")
    ]
    for i, (k, v) in enumerate(meta_rows):
        row = meta_tbl.rows[i]
        row.cells[0].width = Inches(1.9)
        row.cells[1].width = Inches(4.7)
        set_cell_bg(row.cells[0], HEX_PAPER)
        set_cell_bg(row.cells[1], "FFFFFF")
        set_cell_margins(row.cells[0], 65, 65, 100, 100)
        set_cell_margins(row.cells[1], 65, 65, 100, 100)
        p0 = row.cells[0].paragraphs[0]
        p0.paragraph_format.space_after = Pt(0)
        r0 = p0.add_run(k)
        r0.font.bold = True
        r0.font.size = Pt(9.5)
        r0.font.color.rgb = COLOR_INK
        p1 = row.cells[1].paragraphs[0]
        p1.paragraph_format.space_after = Pt(0)
        r1 = p1.add_run(v)
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = COLOR_SECONDARY

    doc.add_page_break()

    # SECTION 1: DESIGN (20 POINTS)
    add_h1(doc, "1. System Architecture & Database Design [20 Points]")
    add_p(doc,
          "Wild Indonesia is an archipelago tourist guide and expedition rental platform built in Jakarta Server Pages (JSP 3.1) and Jakarta Servlets running on Embedded Apache Tomcat 10.1.33. "
          "The landing screen pairs an archival paper aesthetic with an orchestrated 2.1-second Web Animations hero that expands a hairline between the letters WI and LD into a full-bleed looping video stadium, before transitioning readers into eight database-backed CRUD workflows.")

    add_h2(doc, "1.1 Three-Tier JSP / Servlet / JDBC Architecture")
    add_bullet(doc, "Presentation Layer (JSP 3.1 + JSTL 3.0): ", "Clean server-rendered templates under WEB-INF/views paired with archival editorial styling (assets/css/editorial.css) and self-hosted Inter Variable typography.")
    add_bullet(doc, "Controller Layer (Jakarta Servlets): ", "Ten dedicated HttpServlet endpoints handling query filters, PBKDF2 authentication, booking price computation, and HTTP Post-Redirect-Get flows.")
    add_bullet(doc, "Persistence Layer (DataStore DAO + Dual JDBC): ", "Unified PreparedStatement data-access layer communicating with embedded SQLite by default and supporting MySQL 8+ via DB_URL, DB_USER, and DB_PASS configuration.")

    add_h2(doc, "1.2 Conceptual & Physical Data Model (CDM & PDM — 9 Tables)")
    add_p(doc,
          "The relational database comprises nine interconnected entities in Third Normal Form (3NF), isolating archipelago regions, Indonesian destinations, expedition rentals, local guides, itineraries, reservations, field stories, user accounts, and saved bucket-list bookmarks.")

    add_figure(doc, "diagrams.png", "Figure 1: Live Conceptual Data Model (CDM) and Physical Data Model (PDM) rendered inside Wild Indonesia.", 6.3)

    schema_tbl = doc.add_table(rows=10, cols=4)
    schema_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(schema_tbl)
    s_widths = [Inches(1.25), Inches(1.4), Inches(2.2), Inches(1.75)]
    for idx, h in enumerate(["Table Name", "Primary & Unique Keys", "Foreign Keys & Cascade Rules", "Role / Feature"]):
        c = schema_tbl.rows[0].cells[idx]
        c.width = s_widths[idx]
        set_cell_bg(c, HEX_PRIMARY)
        set_cell_margins(c, 65, 65, 80, 80)
        p = c.paragraphs[0]
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(h)
        r.font.bold = True
        r.font.size = Pt(8.5)
        r.font.color.rgb = RGBColor(255, 255, 255)

    schema_rows = [
        ("users", "id PK, email UNIQUE", "-", "Auth, Profile & QA Switcher"),
        ("regions", "id PK, code UNIQUE", "-", "Feature 2: Island Regions CRUD"),
        ("destinations", "id PK, slug UNIQUE", "region_id -> regions(id) CASCADE", "Feature 1: Destinations Guide CRUD"),
        ("rentals", "id PK", "destination_id -> destinations(id) CASCADE", "Feature 3: Expedition Rentals CRUD"),
        ("local_guides", "id PK, license_number UQ", "destination_id -> destinations(id) CASCADE", "Feature 4: Local Guides CRUD"),
        ("itineraries", "id PK", "user_id -> users(id), region_id -> regions(id) CASCADE", "Feature 5: Trip Itineraries CRUD"),
        ("bookings", "id PK, booking_code UQ", "user_id CASCADE, rental_id CASCADE, guide_id SET NULL", "Feature 6: Bookings & Ledger CRUD"),
        ("travel_stories", "id PK", "user_id CASCADE, destination_id CASCADE", "Feature 7: Field Stories CRUD"),
        ("saved_places", "id PK, UQ(user_id, dest_id)", "user_id CASCADE, destination_id CASCADE", "Feature 8: Bucket List CRUD")
    ]
    for r_i, row_vals in enumerate(schema_rows):
        row = schema_tbl.rows[r_i + 1]
        bg = HEX_PAPER if r_i % 2 == 1 else "FFFFFF"
        for c_i, val in enumerate(row_vals):
            c = row.cells[c_i]
            c.width = s_widths[c_i]
            set_cell_bg(c, bg)
            set_cell_margins(c, 50, 50, 80, 80)
            p = c.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            r = p.add_run(val)
            r.font.size = Pt(8.2)
            r.font.bold = (c_i == 0)
            r.font.color.rgb = COLOR_INK if c_i == 0 else COLOR_SECONDARY

    add_h2(doc, "1.3 Database Normalization Compliance (1NF, 2NF, 3NF)")
    add_bullet(doc, "First Normal Form (1NF): ", "Every table enforces a single atomic value per column (such as numeric IDR entry fees and ISO reservation dates) and a surrogate integer primary key.")
    add_bullet(doc, "Second Normal Form (2NF): ", "All non-key columns depend on the entire primary key; junction entities such as bookings and saved_places bind attributes directly to the composite traveler-asset relationship.")
    add_bullet(doc, "Third Normal Form (3NF): ", "Transitive dependencies are removed by separating island climate metadata into regions, fleet operator data into rentals, and certified ranger profiles into local_guides.")

    # SECTION 2: IMPLEMENTATION (50 POINTS)
    add_h1(doc, "2. Implementation: Wild Pill Hero & 8 CRUD Features [50 Points]")

    add_h2(doc, "2.1 Hero Reveal Experience (Desktop & Responsive Mobile)")
    add_p(doc,
          "The landing screen opens on pure white with the display word WILD and a crisp grey hairline between I and L. Once self-hosted Inter Variable fonts finish loading, a synchronized 2.1-second Web Animations timeline opens the hairline vertically, widens it into a slit, circle, and 2:1 stadium photo pill pushing WI and LD outward while showcasing real Indonesian Unsplash photography (Padar Island, Raja Ampat, Mount Bromo, Nusa Penida, and Borobudur), fades in the warm archival paper background with 15 flowing white S-curves, drops in the top navigation, rises the drop-cap footer and draws the centered horizontal rule.")
    add_figure(doc, "hero_desktop.png", "Figure 2: Wild Indonesia desktop hero stage (1440x960) showing stadium video pill, drop-cap copy, 1px center rule, and 15 flowing S-curves.", 6.0)
    add_figure(doc, "hero_mobile.png", "Figure 3: Mobile responsive layout (390x844) with stacked WI / pill / LD arrangement and vertical slit expansion.", 2.6)

    features = [
        ("Feature 1: Indonesian Destinations Guide CRUD", "/destinations, /destinations/view, /destinations/save, /destinations/delete (DestinationServlet)",
         "destinations.png",
         "Multi-criteria filter bar querying by keyword, region, category, maximum IDR entry fee, and sort mode. Supports creating new Indonesian landmarks, toggling homepage spotlight flags, viewing linked rentals/guides/stories, updating metadata, and cascading deletion."),
        ("Feature 2: Archipelago Island Regions & Seasonal Atlas CRUD", "/regions, /regions/save, /regions/delete (RegionServlet)",
         "regions.png",
         "Catalogues Indonesian island zones (Java, Bali & Nusa, Komodo & Flores, Raja Ampat, Sumatra) with monsoon seasons, climate notes, destination counters, and full create/edit/delete forms."),
        ("Feature 3: Wild Expedition Rentals Fleet CRUD", "/rentals, /rentals/save, /rentals/toggle, /rentals/delete (RentalServlet)",
         "rentals.png",
         "Manages wooden phinisi liveaboards, 1982 4x4 volcano Land Cruisers, bamboo canopy eco-villas, coastal motorbikes, and crater safety gear with 1-click availability toggles and full CRUD editing."),
        ("Feature 4: Certified Indonesian Local Guides Directory CRUD", "/guides, /guides/save, /guides/toggle, /guides/delete (GuideServlet)",
         "guides.png",
         "Stores licensed HPI local guides paired to Indonesian destinations, searchable by specialty and spoken languages, with license verification toggles and daily IDR rate management."),
        ("Feature 5: Nusantara Multi-Day Trip Itineraries CRUD", "/itineraries, /itineraries/view, /itineraries/save, /itineraries/delete (ItineraryServlet)",
         "itineraries.png",
         "Allows travelers to filter and author multi-day routes across islands with duration, estimated IDR budget, travel pace, route stops, and stage-by-stage breakdown."),
        ("Feature 6: Rental & Guide Bookings Ledger CRUD", "/bookings, /bookings/save, /bookings/status, /bookings/delete (BookingServlet)",
         "bookings.png",
         "Computes trip duration and combined rental + guide IDR pricing automatically on the server. Supports creating reservations, updating dates and special requests, advancing status (Pending, Confirmed, Completed, Cancelled), and cancellation."),
        ("Feature 7: Traveler Field Stories & Tips CRUD", "/stories, /stories/save, /stories/delete (StoryServlet)",
         "stories.png",
         "Community trail dispatches attached to Indonesian destinations featuring 1-5 ratings, visited month, photography, narrative body, and practical travel tips with full edit and delete support."),
        ("Feature 8: Personal Archipelago Bucket-List CRUD", "/saved, /saved/toggle, /saved/update, /saved/delete (SavedPlaceServlet)",
         "qa_accounts.png",
         "Provides 1-click bookmarking of destinations backed by a composite unique (user_id, destination_id) constraint, inline priority and target season updates, plus a dedicated QA Registered Accounts Inspector at /debug/accounts for role-switching verification.")
    ]

    for title, route, shot, desc in features:
        add_h2(doc, f"2.{features.index((title, route, shot, desc)) + 2} {title}")
        add_p(doc, route, "Endpoint & Servlet: ")
        add_p(doc, desc)
        add_figure(doc, shot, f"Figure {features.index((title, route, shot, desc)) + 4}: Live screenshot of {title}.", 5.9)

    # SECTION 3: ANALYSIS & EVALUATION (25 POINTS)
    add_h1(doc, "3. Technical Analysis & Automated Verification [25 Points]")
    add_h2(doc, "3.1 Security & Persistence Integrity")
    add_bullet(doc, "PBKDF2 Password Hashing: ", "All account credentials are salted and hashed with PBKDF2WithHmacSHA256 at 31,000 iterations and verified via constant-time comparison.")
    add_bullet(doc, "Parameterized SQL Execution: ", "Every CRUD operation inside DataStore uses JDBC PreparedStatement bindings, neutralizing SQL injection across both SQLite and MySQL dialects.")
    add_bullet(doc, "Session Guards & Cascade Integrity: ", "Mutations check authenticated session state, enforce PRAGMA foreign_keys = ON on SQLite, and cascade deletions cleanly across all child entities.")

    add_h2(doc, "3.2 JUnit 5 Automated Integration Suite Results")
    add_p(doc,
          "WildIndonesiaCrudTest boots Embedded Apache Tomcat on an ephemeral port against an isolated SQLite database and executes 10 ordered HTTP integration tests verifying the Wild Hero plus full Create, Read, Update, and Delete round-trips across all 8 features.")

    test_tbl = doc.add_table(rows=11, cols=3)
    test_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(test_tbl)
    tw = [Inches(2.3), Inches(3.2), Inches(1.1)]
    for idx, h in enumerate(["Test Case", "Verification Scope", "Outcome"]):
        c = test_tbl.rows[0].cells[idx]
        c.width = tw[idx]
        set_cell_bg(c, HEX_PRIMARY)
        set_cell_margins(c, 55, 55, 80, 80)
        p = c.paragraphs[0]
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(h)
        r.font.bold = True
        r.font.size = Pt(8.5)
        r.font.color.rgb = RGBColor(255, 255, 255)

    test_rows = [
        ("heroLandingRendersWildPillAndGuide", "Hero DOM, Indonesian Unsplash photo gallery, Padar Island & featured cards", "PASSED"),
        ("loginAndQaAccountSwitchWork", "POST /login session creation & /debug/accounts inspector rendering", "PASSED"),
        ("crudFeature1DestinationsRoundTrip", "Create, filter, update, and delete destination record", "PASSED"),
        ("crudFeature2RegionsRoundTrip", "Create, read, update, and delete archipelago region", "PASSED"),
        ("crudFeature3RentalsRoundTrip", "Create rental, toggle availability flag, delete rental", "PASSED"),
        ("crudFeature4GuidesRoundTrip", "Create guide, toggle HPI verification badge, delete guide", "PASSED"),
        ("crudFeature5ItinerariesRoundTrip", "Create, update, read, and delete multi-day itinerary", "PASSED"),
        ("crudFeature6BookingsRoundTrip", "Server-calculated booking total, status update, delete", "PASSED"),
        ("crudFeature7StoriesRoundTrip", "Publish field story, edit narrative/rating, delete story", "PASSED"),
        ("crudFeature8SavedPlacesRoundTrip", "Toggle bucket-list bookmark, update note/season, remove", "PASSED")
    ]
    for i, (tc, scope, res) in enumerate(test_rows):
        row = test_tbl.rows[i + 1]
        bg = HEX_PAPER if i % 2 == 1 else "FFFFFF"
        for c_i, val in enumerate([tc, scope, res]):
            c = row.cells[c_i]
            c.width = tw[c_i]
            set_cell_bg(c, bg)
            set_cell_margins(c, 45, 45, 80, 80)
            p = c.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            r = p.add_run(val)
            r.font.size = Pt(8.2)
            if c_i == 2:
                r.font.bold = True
                r.font.color.rgb = COLOR_ACCENT

    # SECTION 4: CONCLUSION & CONTRIBUTIONS (5 POINTS)
    add_h1(doc, "4. Conclusion & Equal Team Contributions [5 Points]")
    add_p(doc,
          "Wild Indonesia successfully unifies the precision Web Animations kinetic hero with a complete 9-table Indonesian tourist guide and expedition rental platform powered by Jakarta Server Pages (JSP), Servlets, and dual SQLite/MySQL persistence. "
          "All 8 CRUD modules pass end-to-end automated integration testing without errors.")

    add_h2(doc, "4.1 Work Distribution Statement (33.33% Equal Split)")
    ctbl = doc.add_table(rows=4, cols=3)
    ctbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(ctbl)
    cw = [Inches(1.8), Inches(0.95), Inches(3.85)]
    for idx, h in enumerate(["Student Name & NRP", "Share", "Assigned Responsibilities & Deliverables"]):
        c = ctbl.rows[0].cells[idx]
        c.width = cw[idx]
        set_cell_bg(c, HEX_PRIMARY)
        set_cell_margins(c, 60, 60, 80, 80)
        p = c.paragraphs[0]
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(h)
        r.font.bold = True
        r.font.size = Pt(8.5)
        r.font.color.rgb = RGBColor(255, 255, 255)

    contribs = [
        ("Rinaltra Nabasa S\nNRP: 5025251024", "33.33%",
         "Web design and frontend engineering: Wild Pill Hero Web Animations 2.1s timeline (desktop & mobile vertical split), SVG archival paper S-curves, JSP editorial CSS system, and responsive views for Destinations, Rentals & Stories."),
        ("Haidar Abiyyu at Taqy\nNRP: 5025521121", "33.33%",
         "Database & backend engineering: 9-table 3NF CDM & PDM design, SQLite/MySQL JDBC DatabaseManager, PBKDF2 security, DataStore DAO, and all Jakarta Servlets implementing Features 1 through 8."),
        ("Fakhrian Elanta\nNRP: 5025251019", "33.33%",
         "Quality control, debugging & documentation: QA Registered Accounts Inspector (/debug/accounts), JUnit 5 full-stack integration test suite (10/10 passing), browser verification screenshots, and Quiz 2 Technical Report.")
    ]
    for i, cdata in enumerate(contribs):
        row = ctbl.rows[i + 1]
        bg = HEX_PAPER if i % 2 == 1 else "FFFFFF"
        for c_i, val in enumerate(cdata):
            c = row.cells[c_i]
            c.width = cw[c_i]
            set_cell_bg(c, bg)
            set_cell_margins(c, 55, 55, 80, 80)
            p = c.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            r = p.add_run(val)
            r.font.size = Pt(8.5)
            if c_i in (0, 1):
                r.font.bold = True
                r.font.color.rgb = COLOR_INK if c_i == 0 else COLOR_ACCENT

    add_h2(doc, "4.2 Academic Integrity Pledge")
    add_p(doc,
          "\"By the name of Allah (God) Almighty, I hereby pledge and declare that I have completed Quiz 2 independently. I have not engaged in cheating, plagiarism, or received unauthorized assistance in any form. I further declare that any use of AI tools was limited to a supportive role (such as for grammar checking or debugging), and that the final solution is the product of my own intellectual effort. I understand that I will accept all consequences if I am found to have violated this academic integrity pledge.\"")
    add_p(doc, "Surabaya, 25 November 2026 — Signed digitally by Rinaltra Nabasa S (5025251024), Fakhrian Elanta (5025251019), and Haidar Abiyyu at Taqy (5025521121).")

    out_docx = os.path.join(QUIZ2_DIR, "Report.docx")
    doc.save(out_docx)
    return out_docx


def build_declaration():
    doc = docx.Document()
    configure_doc(doc)

    p_hdr = doc.add_paragraph()
    p_hdr.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_hdr.paragraph_format.space_before = Pt(18)
    r_hdr = p_hdr.add_run("OFFICIAL ACADEMIC INTEGRITY DECLARATION\nEF234301 WEB PROGRAMMING (D) — QUIZ 2")
    r_hdr.font.bold = True
    r_hdr.font.size = Pt(15)
    r_hdr.font.color.rgb = COLOR_INK

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(24)
    rs = p_sub.add_run("Department of Informatics · Institut Teknologi Sepuluh Nopember (ITS)")
    rs.font.size = Pt(10.5)
    rs.font.color.rgb = COLOR_ACCENT

    box = doc.add_table(rows=1, cols=1)
    box.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(box, color=HEX_PRIMARY, sz="8")
    cell = box.rows[0].cells[0]
    cell.width = Inches(6.4)
    set_cell_bg(cell, HEX_PAPER)
    set_cell_margins(cell, 160, 160, 180, 180)
    bp = cell.paragraphs[0]
    bp.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    bp.paragraph_format.line_spacing = 1.35
    br = bp.add_run(
        "\"By the name of Allah (God) Almighty, I hereby pledge and declare that I have completed Quiz 2 independently. "
        "I have not engaged in cheating, plagiarism, or received unauthorized assistance in any form. "
        "I further declare that any use of AI tools was limited to a supportive role (such as for grammar checking or debugging), "
        "and that the final solution is the product of my own intellectual effort. "
        "I understand that I will accept all consequences if I am found to have violated this academic integrity pledge.\""
    )
    br.font.italic = True
    br.font.size = Pt(11)
    br.font.color.rgb = COLOR_INK

    p_place = doc.add_paragraph()
    p_place.paragraph_format.space_before = Pt(24)
    p_place.paragraph_format.space_after = Pt(28)
    rp = p_place.add_run("Surabaya, 25 November 2026")
    rp.font.bold = True
    rp.font.color.rgb = COLOR_INK

    sig_tbl = doc.add_table(rows=2, cols=3)
    sig_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(sig_tbl, color="FFFFFF", sz="0")
    members = [
        ("Rinaltra Nabasa S", "5025251024"),
        ("Fakhrian Elanta", "5025251019"),
        ("Haidar Abiyyu at Taqy", "5025521121")
    ]
    for i, (name, nrp) in enumerate(members):
        sig_tbl.rows[0].cells[i].width = Inches(2.15)
        sig_tbl.rows[1].cells[i].width = Inches(2.15)
        p0 = sig_tbl.rows[0].cells[i].paragraphs[0]
        r0 = p0.add_run("<Signed digitally>\n\n\n______________________")
        r0.font.color.rgb = COLOR_MUTED
        p1 = sig_tbl.rows[1].cells[i].paragraphs[0]
        r1 = p1.add_run(f"{name}\n{nrp}")
        r1.font.bold = True
        r1.font.color.rgb = COLOR_INK

    out_docx = os.path.join(QUIZ2_DIR, "Declaration.docx")
    doc.save(out_docx)
    return out_docx


def export_pdfs_and_zip(report_docx, decl_docx):
    import subprocess
    report_pdf = os.path.join(QUIZ2_DIR, "Report.PDF")
    decl_pdf = os.path.join(QUIZ2_DIR, "Declaration.PDF")

    ps = f"""
$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {{
    $d1 = $word.Documents.Open('{report_docx}')
    $d1.SaveAs([ref]'{report_pdf}', [ref]17)
    $d1.Close()

    $d2 = $word.Documents.Open('{decl_docx}')
    $d2.SaveAs([ref]'{decl_pdf}', [ref]17)
    $d2.Close()
}} finally {{
    $word.Quit()
}}
"""
    subprocess.run(["powershell", "-NoProfile", "-Command", ps], check=True)

    zip_path = os.path.join(QUIZ2_DIR, ZIP_NAME)
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.write(report_pdf, "Report.PDF")
        zf.write(decl_pdf, "Declaration.PDF")

    dl_zip = os.path.join(DOWNLOADS_DIR, ZIP_NAME)
    shutil.copyfile(zip_path, dl_zip)
    print("Created Report.PDF:", report_pdf)
    print("Created Declaration.PDF:", decl_pdf)
    print("Created ZIP:", zip_path)
    print("Copied ZIP to Downloads:", dl_zip)


if __name__ == "__main__":
    rep = build_report()
    dec = build_declaration()
    export_pdfs_and_zip(rep, dec)
