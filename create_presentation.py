import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def create_deck():
    prs = Presentation()
    # 16:9 Widescreen layout
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Color Palette
    C_BG = RGBColor(11, 15, 25)          # #0B0F19 Deep Tech Navy
    C_CARD = RGBColor(19, 26, 43)        # #131A2B Dark Card
    C_CARD_BORDER = RGBColor(37, 51, 82) # #253352 Card Border
    C_BLUE = RGBColor(37, 99, 235)       # #2563EB Vibrant Blue
    C_CYAN = RGBColor(6, 182, 212)       # #06B6D4 Electric Cyan
    C_EMERALD = RGBColor(16, 185, 129)   # #10B981 Emerald Green
    C_AMBER = RGBColor(245, 158, 11)     # #F59E0B Amber
    C_WHITE = RGBColor(255, 255, 255)
    C_SLATE_200 = RGBColor(226, 232, 240)
    C_SLATE_400 = RGBColor(148, 163, 184)
    C_SLATE_500 = RGBColor(100, 116, 139)

    assets_dir = 'presentation_assets'

    # Team Members (Strictly Name & Roll Number only)
    members = [
        ("M. Akshitha", "24241-CS-033"),
        ("G. Ashlesh", "24241-CS-034"),
        ("G. Bhargavi", "24241-CS-035"),
        ("E. Sakshi", "24241-CS-036"),
        ("A. Varun", "24241-CS-037")
    ]

    def add_bg(slide):
        bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
        bg.fill.solid()
        bg.fill.fore_color.rgb = C_BG
        bg.line.fill.background()
        return bg

    def add_header(slide, category, title, subtitle):
        tb = slide.shapes.add_textbox(Inches(0.8), Inches(0.5), Inches(11.7), Inches(1.1))
        tf = tb.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

        p0 = tf.paragraphs[0]
        p0.text = category.upper()
        p0.font.name = "Segoe UI"
        p0.font.size = Pt(11)
        p0.font.bold = True
        p0.font.color.rgb = C_CYAN
        p0.space_after = Pt(2)

        p1 = tf.add_paragraph()
        p1.text = title
        p1.font.name = "Segoe UI"
        p1.font.size = Pt(22)
        p1.font.bold = True
        p1.font.color.rgb = C_WHITE
        p1.space_after = Pt(2)

        if subtitle:
            p2 = tf.add_paragraph()
            p2.text = subtitle
            p2.font.name = "Segoe UI"
            p2.font.size = Pt(12)
            p2.font.color.rgb = C_SLATE_400

    def add_footer(slide, slide_num):
        tb = slide.shapes.add_textbox(Inches(0.8), Inches(6.9), Inches(11.7), Inches(0.4))
        tf = tb.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0
        p = tf.paragraphs[0]
        p.text = f"APEX TELECOM — Major Project Presentation  |  Team 07  |  Slide {slide_num} of 13"
        p.font.name = "Segoe UI"
        p.font.size = Pt(9.5)
        p.font.color.rgb = C_SLATE_500

    def create_card(slide, left, top, width, height, fill_color=C_CARD, border_color=C_CARD_BORDER):
        shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        shape.fill.solid()
        shape.fill.fore_color.rgb = fill_color
        shape.line.color.rgb = border_color
        shape.line.width = Pt(1.2)
        return shape

    # ==========================================
    # SLIDE 1: TITLE SLIDE
    # ==========================================
    s1 = prs.slides.add_slide(blank_layout)
    add_bg(s1)

    bar = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.1))
    bar.fill.solid()
    bar.fill.fore_color.rgb = C_BLUE
    bar.line.fill.background()

    # Brand pill badge
    badge = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(0.8), Inches(3.2), Inches(0.45))
    badge.fill.solid()
    badge.fill.fore_color.rgb = RGBColor(30, 41, 59)
    badge.line.color.rgb = C_BLUE
    badge.line.width = Pt(1)
    tf_b = badge.text_frame
    tf_b.text = "⚡ COLLEGE MAJOR PROJECT 2026"
    tf_b.paragraphs[0].font.name = "Segoe UI"
    tf_b.paragraphs[0].font.size = Pt(10.5)
    tf_b.paragraphs[0].font.bold = True
    tf_b.paragraphs[0].font.color.rgb = C_CYAN
    tf_b.paragraphs[0].alignment = PP_ALIGN.CENTER

    # Project Title
    tb_title = s1.shapes.add_textbox(Inches(0.8), Inches(1.4), Inches(7.5), Inches(2.2))
    tf_t = tb_title.text_frame
    tf_t.word_wrap = True
    p = tf_t.paragraphs[0]
    p.text = "Telephone Bill\nManagement System"
    p.font.name = "Segoe UI"
    p.font.size = Pt(36)
    p.font.bold = True
    p.font.color.rgb = C_WHITE
    p.space_after = Pt(8)

    p_sub = tf_t.add_paragraph()
    p_sub.text = "An Enterprise-Grade Telecom Billing & Subscriber Management Suite\nDeveloped in Java 21 (JavaFX + SQLite + OpenPDF) with Live Cloud Web Deployment"
    p_sub.font.name = "Segoe UI"
    p_sub.font.size = Pt(13)
    p_sub.font.color.rgb = C_SLATE_400

    # Live Badge pill
    live_badge = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(3.8), Inches(4.8), Inches(0.5))
    live_badge.fill.solid()
    live_badge.fill.fore_color.rgb = RGBColor(6, 78, 59)
    live_badge.line.color.rgb = C_EMERALD
    tf_lb = live_badge.text_frame
    tf_lb.text = "🟢 Live Web App: apex-telecom-billing.vercel.app"
    tf_lb.paragraphs[0].font.name = "Segoe UI"
    tf_lb.paragraphs[0].font.size = Pt(10.5)
    tf_lb.paragraphs[0].font.bold = True
    tf_lb.paragraphs[0].font.color.rgb = RGBColor(167, 243, 208)
    tf_lb.paragraphs[0].alignment = PP_ALIGN.CENTER

    # Right Card: Presented by Team 07 (Names and Roll Numbers ONLY)
    create_card(s1, Inches(8.5), Inches(0.8), Inches(4.0), Inches(5.8), C_CARD, C_BLUE)
    tb_team = s1.shapes.add_textbox(Inches(8.8), Inches(1.1), Inches(3.4), Inches(5.2))
    tf_team = tb_team.text_frame
    tf_team.word_wrap = True

    p = tf_team.paragraphs[0]
    p.text = "TEAM 07"
    p.font.name = "Segoe UI"
    p.font.size = Pt(15)
    p.font.bold = True
    p.font.color.rgb = C_CYAN
    p.space_after = Pt(2)

    p = tf_team.add_paragraph()
    p.text = "Department of Computer Science & Engineering"
    p.font.name = "Segoe UI"
    p.font.size = Pt(10)
    p.font.color.rgb = C_SLATE_400
    p.space_after = Pt(16)

    p = tf_team.add_paragraph()
    p.text = "Presented by:"
    p.font.name = "Segoe UI"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = C_WHITE
    p.space_after = Pt(12)

    for name, roll in members:
        p = tf_team.add_paragraph()
        p.text = f"•  {name} ({roll})"
        p.font.name = "Segoe UI"
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = C_SLATE_200
        p.space_after = Pt(10)

    add_footer(s1, 1)

    # ==========================================
    # SLIDE 2: PROJECT OVERVIEW & PROBLEM STATEMENT
    # ==========================================
    s2 = prs.slides.add_slide(blank_layout)
    add_bg(s2)
    add_header(s2, "Introduction", "Problem Statement & Project Objectives", "Overcoming legacy telecom billing bottlenecks through modern automated software architecture")

    create_card(s2, Inches(0.8), Inches(1.8), Inches(5.6), Inches(4.8))
    tb = s2.shapes.add_textbox(Inches(1.1), Inches(2.0), Inches(5.0), Inches(4.4))
    tf = tb.text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.text = "⚠️ Challenges in Legacy Telecom Billing"
    p.font.name = "Segoe UI"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = RGBColor(248, 113, 113)
    p.space_after = Pt(12)

    problems = [
        ("Manual Rating Inefficiencies", "Legacy systems struggle with real-time classification of call logs into Local, STD, and ISD categories, causing calculation delays."),
        ("Pulse Rounding & Allowance Errors", "Discrepancies in 60-second pulse rounding and inaccurate deduction of free monthly minute quotas frustrate customers."),
        ("Complex Tax Compliance (GST)", "Non-automated handling of 18% GST (CGST 9% + SGST 9%), late fee penalties, and promotional adjustments."),
        ("Lack of Itemized Transparency", "Subscribers lack clear, itemized tax invoices showing exact call timestamps, duration, destination numbers, and cost breakdowns.")
    ]

    for title, desc in problems:
        p = tf.add_paragraph()
        p.text = f"•  {title}:"
        p.font.name = "Segoe UI"
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = C_WHITE
        p_desc = tf.add_paragraph()
        p_desc.text = f"    {desc}"
        p_desc.font.name = "Segoe UI"
        p_desc.font.size = Pt(10.5)
        p_desc.font.color.rgb = C_SLATE_400
        p_desc.space_after = Pt(8)

    create_card(s2, Inches(6.9), Inches(1.8), Inches(5.6), Inches(4.8))
    tb2 = s2.shapes.add_textbox(Inches(7.2), Inches(2.0), Inches(5.0), Inches(4.4))
    tf2 = tb2.text_frame
    tf2.word_wrap = True

    p = tf2.paragraphs[0]
    p.text = "🎯 Proposed Solution & Key Objectives"
    p.font.name = "Segoe UI"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = C_EMERALD
    p.space_after = Pt(12)

    solutions = [
        ("Automated Telecom Rating Engine", "Instant duration pulse conversion (60s ceil), free minutes deduction, and multi-tier rate calculation (Local, STD, ISD)."),
        ("Intelligent Prefix Auto-Detection", "Real-time categorization based on international prefix (+ / 00), national STD trunk codes (022, 080), or domestic lines."),
        ("Airtel/Jio Styled Tax Invoice (Centerpiece)", "Full itemized invoice layout complying with TRAI norms, featuring CGST/SGST tax breakdown, CDR tables, and OpenPDF export."),
        ("Dual-Platform Architecture", "High-performance native Java 21 / JavaFX desktop application alongside an accessible cloud-hosted Vercel web app.")
    ]

    for title, desc in solutions:
        p = tf2.add_paragraph()
        p.text = f"✓  {title}:"
        p.font.name = "Segoe UI"
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = C_WHITE
        p_desc = tf2.add_paragraph()
        p_desc.text = f"    {desc}"
        p_desc.font.name = "Segoe UI"
        p_desc.font.size = Pt(10.5)
        p_desc.font.color.rgb = C_SLATE_400
        p_desc.space_after = Pt(8)

    add_footer(s2, 2)

    # ==========================================
    # SLIDE 3: SYSTEM ARCHITECTURE (MVC)
    # ==========================================
    s3 = prs.slides.add_slide(blank_layout)
    add_bg(s3)
    add_header(s3, "Engineering Design", "System Architecture & MVC Pattern", "Clean separation of presentation, business rules, and persistence layers for maximum maintainability")

    cols = [
        ("VIEW LAYER", C_CYAN, [
            ("JavaFX 21 (FXML)", "Declarative XML UI views with CSS styling and dark/light themes."),
            ("Custom CSS3", "Modern sleek design with responsive layouts and glowing accent pills."),
            ("Chart Visualizations", "JavaFX BarChart & PieChart + Web Chart.js for operational analytics."),
            ("Invoice Templates", "Printable and exportable Airtel/Jio styled vector PDF templates.")
        ]),
        ("CONTROLLER LAYER", C_BLUE, [
            ("DashboardController", "KPI metric binding and real-time chart synchronization."),
            ("BillController", "Invoice generation workflows and payment settlement triggers."),
            ("CallRecordController", "Real-time prefix typing listener and CDR filtering."),
            ("CustomerController", "Subscriber registration, status updates, and KYC management.")
        ]),
        ("SERVICE ENGINE", C_EMERALD, [
            ("BillingEngine", "Pulse ceiling, free-minute quota deduction, GST, late fees."),
            ("CallService", "Prefix auto-detection regex (+/00 -> ISD, 0 -> STD, +91 -> Local)."),
            ("PdfInvoiceService", "Vector PDF generation with OpenPDF & itemized CDR tables."),
            ("AuthService", "SHA-256 password hashing with salt and Role-Based Access Control.")
        ]),
        ("DATA & PERSISTENCE", C_AMBER, [
            ("SQLite 3 (JDBC)", "Serverless zero-config relational database stored locally in telecom.db."),
            ("DatabaseManager", "Automated DDL execution (schema.sql) and initial seed data loading."),
            ("Entity DAOs", "CustomerDAO, BillDAO, CallRecordDAO, TariffPlanDAO, PaymentDAO."),
            ("Data Integrity", "Foreign key constraints, cascading checks, and transaction safety.")
        ])
    ]

    card_w = Inches(2.75)
    card_gap = Inches(0.2)
    start_x = Inches(0.8)

    for i, (col_title, color, items) in enumerate(cols):
        cx = start_x + i * (card_w + card_gap)
        create_card(s3, cx, Inches(1.8), card_w, Inches(4.8), C_CARD, color)

        tb = s3.shapes.add_textbox(cx + Inches(0.2), Inches(2.0), card_w - Inches(0.4), Inches(4.4))
        tf = tb.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = col_title
        p.font.name = "Segoe UI"
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = color
        p.space_after = Pt(12)

        for ititle, idesc in items:
            p = tf.add_paragraph()
            p.text = f"• {ititle}"
            p.font.name = "Segoe UI"
            p.font.size = Pt(11)
            p.font.bold = True
            p.font.color.rgb = C_WHITE

            p2 = tf.add_paragraph()
            p2.text = idesc
            p2.font.name = "Segoe UI"
            p2.font.size = Pt(9.5)
            p2.font.color.rgb = C_SLATE_400
            p2.space_after = Pt(8)

    add_footer(s3, 3)

    # ==========================================
    # SLIDE 4: TECHNOLOGY STACK
    # ==========================================
    s4 = prs.slides.add_slide(blank_layout)
    add_bg(s4)
    add_header(s4, "Technology Matrix", "Enterprise Technology Stack", "Robust industrial libraries chosen for performance, standards compliance, and zero deployment friction")

    tech_cards = [
        ("Java 21 (LTS)", "Core Runtime", C_CYAN, "Latest Long-Term Support release offering enhanced pattern matching, record types, virtual threads readiness, and superior enterprise stability."),
        ("JavaFX 21", "Desktop GUI", C_BLUE, "Modern GUI framework utilizing FXML separation of design and logic, CSS3 skinning, smooth hardware-accelerated animations, and responsive scene graphs."),
        ("SQLite 3 via JDBC", "Relational Database", C_EMERALD, "Serverless, zero-configuration local database engine. Eliminates external DBMS setup while maintaining full ACID compliance and relational integrity."),
        ("OpenPDF 1.3.40", "Invoice PDF Engine", C_AMBER, "LGPL/MPL compliant vector PDF library generating authentic pixel-perfect billing statements with dynamic tables, borders, and status watermarks."),
        ("Apache Maven 3.9+", "Build & Dependency", C_CYAN, "Standardized project lifecycle management, automated compilation, dependency management, and reproducible packaging into standalone executable JARs."),
        ("Vercel Cloud & JS", "Web Deployment", C_EMERALD, "Hoisted on Vercel's global edge network. Single Page Application (SPA) with Chart.js analytics, html2pdf export, and mobile responsiveness.")
    ]

    for i, (tech, category, color, desc) in enumerate(tech_cards):
        row = i // 3
        col = i % 3
        x = Inches(0.8) + col * Inches(3.95)
        y = Inches(1.8) + row * Inches(2.4)

        create_card(s4, x, y, Inches(3.75), Inches(2.2), C_CARD, C_CARD_BORDER)

        tb = s4.shapes.add_textbox(x + Inches(0.25), y + Inches(0.2), Inches(3.25), Inches(1.8))
        tf = tb.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = category.upper()
        p.font.name = "Segoe UI"
        p.font.size = Pt(9.5)
        p.font.bold = True
        p.font.color.rgb = color
        p.space_after = Pt(2)

        p = tf.add_paragraph()
        p.text = tech
        p.font.name = "Segoe UI"
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = C_WHITE
        p.space_after = Pt(6)

        p = tf.add_paragraph()
        p.text = desc
        p.font.name = "Segoe UI"
        p.font.size = Pt(10)
        p.font.color.rgb = C_SLATE_400

    add_footer(s4, 4)

    # Helper for App Feature Slides (with Screenshot on right or left)
    def add_feature_slide(slide_num, tag, title, subtitle, bullets, img_name, img_caption):
        s = prs.slides.add_slide(blank_layout)
        add_bg(s)
        add_header(s, tag, title, subtitle)

        create_card(s, Inches(0.8), Inches(1.8), Inches(5.5), Inches(4.8))
        tb = s.shapes.add_textbox(Inches(1.1), Inches(2.0), Inches(4.9), Inches(4.4))
        tf = tb.text_frame
        tf.word_wrap = True

        for i, (btitle, bdesc) in enumerate(bullets):
            p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
            p.text = f"•  {btitle}"
            p.font.name = "Segoe UI"
            p.font.size = Pt(12.5)
            p.font.bold = True
            p.font.color.rgb = C_CYAN if i == 0 else C_WHITE

            p_desc = tf.add_paragraph()
            p_desc.text = f"   {bdesc}"
            p_desc.font.name = "Segoe UI"
            p_desc.font.size = Pt(10.5)
            p_desc.font.color.rgb = C_SLATE_400
            p_desc.space_after = Pt(10)

        img_path = os.path.join(assets_dir, img_name)
        create_card(s, Inches(6.6), Inches(1.8), Inches(5.9), Inches(4.8), C_CARD, C_BLUE)

        if os.path.exists(img_path):
            s.shapes.add_picture(img_path, Inches(6.75), Inches(1.95), Inches(5.6), Inches(4.2))
            cap = s.shapes.add_textbox(Inches(6.75), Inches(6.2), Inches(5.6), Inches(0.3))
            tf_c = cap.text_frame
            p_c = tf_c.paragraphs[0]
            p_c.text = f"📷 Live System Screen: {img_caption}"
            p_c.font.name = "Segoe UI"
            p_c.font.size = Pt(9.5)
            p_c.font.color.rgb = C_CYAN
            p_c.alignment = PP_ALIGN.CENTER

        add_footer(s, slide_num)
        return s

    # ==========================================
    # SLIDES 5 TO 10: APP MODULES
    # ==========================================
    add_feature_slide(
        5,
        "System Showcase — Module 1",
        "Executive Operational Dashboard",
        "Real-time visibility into billing revenues, subscriber provisioning, and telecom traffic volume",
        [
            ("Executive KPI Summary Cards", "Live aggregates displaying Monthly Invoiced Revenue (₹995.45), Active Billable Subscribers (5 Lines), Overdue Invoices (1 Invoice), and Total Usage (107 Mins)."),
            ("Monthly Billing Trend (Bar Chart)", "Tracks 6-month historical billing collections from March to August 2026, visualizing revenue seasonality and growth."),
            ("Tariff Plan Distribution (Doughnut)", "Visual breakdown of subscribers enrolled across Prepaid Starter, Postpaid Silver, Gold Unlimited, and Enterprise tiers."),
            ("College Viva Quick Controls", "Instant action bar allowing single-click bill generation, call logging, and subscriber registration during presentations.")
        ],
        "dashboard.png",
        "Apex Telecom Operational Dashboard with Live Charts"
    )

    add_feature_slide(
        6,
        "System Showcase — Module 2",
        "Call Detail Records (CDR) & Prefix Engine",
        "Itemized telephone traffic logging with automatic prefix recognition and pulse duration rounding",
        [
            ("Intelligent Prefix Auto-Detection", "Evaluates dialed numbers dynamically: '+' or '00' (excluding +91) maps to ISD; '0' trunk codes (022, 080) map to STD; +91/10-digits map to Local."),
            ("Real-Time Tariff Cost Estimation", "As the operator inputs the duration, the system automatically computes and previews the estimated call charge before saving."),
            ("Color-Coded Classification Badges", "Distinctive visual tags (Blue for LOCAL, Indigo for STD, Purple for ISD) ensure clear distinction across large log volumes."),
            ("Bulk CSV Import Facility", "Allows batch ingestion of CDR records from telecom switch logs (timestamp, destination, duration) directly into SQLite.")
        ],
        "calls.png",
        "CDR Call Logger with Real-Time Classification Pills"
    )

    add_feature_slide(
        7,
        "Core Business Logic — Module 3",
        "Automated Telecom Billing Engine",
        "Algorithmic implementation of telecom rating standards, free quota deduction, and statutory taxes",
        [
            ("60-Second Pulse Ceiling", "Standard telecom pulse rule: Math.ceil(durationSeconds / 60.0). Even a 65-second call is billed as 2 whole minutes."),
            ("Free Minutes Priority Deduction", "Monthly free minutes quota (e.g. 150 mins) is deducted against local call minutes first; only excess usage is billed at plan rates."),
            ("Taxable Value Subtotal Calculation", "Subtotal = (Billable Local Mins × Rate) + (STD Mins × Rate) + (ISD Mins × Rate) + Fixed Monthly Rental."),
            ("Statutory GST & Adjustments", "Applies 18% GST (CGST 9% + SGST 9%) on the taxable value, incorporates ₹50 late surcharge for overdue accounts, and deducts promotional discounts.")
        ],
        "reports.png",
        "Financial Analytics & Top Calling Subscribers"
    )

    add_feature_slide(
        8,
        "System Centerpiece — Module 4",
        "Airtel / Jio Styled Tax Invoice",
        "Authentic telecom billing statement layout complying with Indian TRAI regulations and tax formatting",
        [
            ("Authentic Telecom Layout", "Features corporate Telco branding, GSTIN (27AAACA1234F1Z5), DOT license numbers, toll-free customer care details, and Net-15 payment terms."),
            ("Itemized Summary of Charges", "Tabular breakdown of Monthly Rental, CDR itemized pulse charges, Taxable Subtotal, CGST 9%, SGST 9%, Late Surcharge, and Total Payable."),
            ("Itemized CDR Table Inclusion", "Comprehensive list of every call made during the billing cycle (timestamp, dialed destination, classification, duration, and individual charge)."),
            ("Dual Export (OpenPDF & In-Browser)", "Direct vector PDF invoice download via OpenPDF on Desktop and html2pdf on Web, plus in-browser printing with dynamic status stamps (PAID/UNPAID/OVERDUE).")
        ],
        "invoice.png",
        "Authentic Airtel/Jio Styled Tax Invoice Modal"
    )

    add_feature_slide(
        9,
        "System Showcase — Module 5",
        "Subscriber KYC & Tariff Plan Management",
        "End-to-end subscriber provisioning and dynamic calling package configuration",
        [
            ("Comprehensive KYC & Line Directory", "Captures full name, verified phone number, email address, physical billing address, connection date, assigned plan, and line status."),
            ("Subscriber Status Lifecycle", "Manages state transitions between ACTIVE (billable), SUSPENDED (overdue/non-payment), and INACTIVE lines with visual color pills."),
            ("Dynamic Tariff Plan Customizer", "Configure multi-tier calling plans: Prepaid Starter (₹99), Postpaid Silver (₹249), Postpaid Gold Unlimited (₹499), Corporate Enterprise (₹899)."),
            ("Pulse Rate & Free Quota Matrix", "Customizable parameters per plan: Local/STD/ISD pulse rates, free monthly talk time, SMS rates, and data bundle charges.")
        ],
        "plans.png",
        "Tariff Plans & Calling Package Tier Cards"
    )

    add_feature_slide(
        10,
        "System Showcase — Module 6",
        "Financial Analytics & Top Callers",
        "Executive telecom intelligence with revenue collection metrics and caller rankings",
        [
            ("Financial Health Summary Cards", "Instant reporting of Cumulative Invoiced Volume (₹1,215.37), Total Settled Collections (₹366.63), and Outstanding Receivables (₹848.74)."),
            ("Top Calling Subscribers Leaderboard", "Ranks subscribers by total CDR talk time and calls count with 🥇 1st (Sneha Reddy), 🥈 2nd (Priya Patel), 🥉 3rd (Rajesh Sharma) medals."),
            ("Revenue Leakage Prevention", "Enables telecom managers to rapidly identify delinquent accounts, overdue payment windows, and initiate automated late fees."),
            ("Spreadsheet CSV Exporting", "One-click export of financial reports and subscriber traffic tables to CSV for external auditing, accounting, and presentation review.")
        ],
        "customers.png",
        "Subscriber KYC Directory & Provisioning Table"
    )

    # ==========================================
    # SLIDE 11: QUALITY ASSURANCE & TESTING
    # ==========================================
    s11 = prs.slides.add_slide(blank_layout)
    add_bg(s11)
    add_header(s11, "Verification & Testing", "Automated Testing & Quality Assurance", "10/10 automated JUnit 5 test cases passing with 100% build success across all modules")

    tests = [
        ("BillingEngineTest (5/5 Passing)", C_CYAN, [
            ("testUnderFreeMinutesAllowance", "Verifies 80 mins <= 100 free mins results in ₹0.00 call charge; only rental ₹200 + 18% GST (₹236.00) is billed."),
            ("testExceedingFreeMinutesAllowance", "Verifies 120 mins local call correctly consumes 100 free mins and bills only the remaining 20 excess minutes."),
            ("testMixedCallCategories", "Validates simultaneous Local, STD, and ISD calls with duration ceiling and appropriate tariff application."),
            ("testDiscountAndLateFee", "Ensures late fee additions (+₹50) and promotional credit subtractions calculate accurate net payable sums."),
            ("testZeroCalls", "Verifies that zero telecom usage accurately bills only fixed monthly rental plus statutory GST.")
        ]),
        ("CallServiceTest (4/4 Passing)", C_EMERALD, [
            ("testIsdDetection", "Confirms prefixes +1, +44, and 001 are classified as ISD (International) calls."),
            ("testStdDetection", "Confirms national area trunk codes (022, 080, 011) are accurately categorized as STD calls."),
            ("testLocalDetection", "Verifies +91 prefix and standard 10-digit mobile numbers are identified as Local calls."),
            ("testPhoneValidation", "Validates input sanitization against invalid characters, excessive lengths, or malformed strings.")
        ]),
        ("Database & PDF Integration (1/1 Passing)", C_AMBER, [
            ("testDatabaseAndPdfIntegration", "Auto-bootstraps SQLite schema, populates 6 customers, and validates creation of non-empty OpenPDF vector invoice file (>2KB).")
        ])
    ]

    card_w = Inches(3.75)
    start_x = Inches(0.8)

    for i, (suite_name, color, suite_tests) in enumerate(tests):
        cx = start_x + i * (card_w + Inches(0.2))
        create_card(s11, cx, Inches(1.8), card_w, Inches(4.8), C_CARD, color)

        tb = s11.shapes.add_textbox(cx + Inches(0.25), Inches(2.0), card_w - Inches(0.5), Inches(4.4))
        tf = tb.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = f"✓ {suite_name}"
        p.font.name = "Segoe UI"
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = color
        p.space_after = Pt(12)

        for tname, tdesc in suite_tests:
            p = tf.add_paragraph()
            p.text = f"• {tname}"
            p.font.name = "Consolas"
            p.font.size = Pt(10.5)
            p.font.bold = True
            p.font.color.rgb = C_WHITE

            p2 = tf.add_paragraph()
            p2.text = tdesc
            p2.font.name = "Segoe UI"
            p2.font.size = Pt(9.5)
            p2.font.color.rgb = C_SLATE_400
            p2.space_after = Pt(8)

    add_footer(s11, 11)

    # ==========================================
    # SLIDE 12: DEPLOYMENT & ACCESSIBILITY
    # ==========================================
    s12 = prs.slides.add_slide(blank_layout)
    add_bg(s12)
    add_header(s12, "Hosting & Accessibility", "Multi-Platform Deployment & Distribution", "Instant accessibility across desktop operating systems and worldwide cloud web browsers")

    dep_cards = [
        ("Cloud Web Deployment (Vercel)", C_EMERALD, [
            ("Live Production URL", "https://apex-telecom-billing-gurramashlesh-8133s-projects.vercel.app"),
            ("Global Edge Hosting", "Deployed on Vercel Serverless CDN with zero latency and automatic HTTPS SSL security."),
            ("Full Feature Parity", "Replicates Java billing engine, interactive Chart.js graphs, prefix classifier, and in-browser PDF generator."),
            ("Zero-Install Evaluation", "Allows external evaluators and professors to test the system directly from any laptop, tablet, or smartphone.")
        ]),
        ("Public GitHub Repository", C_CYAN, [
            ("Repository Link", "https://github.com/ashlesh0805/TelephoneBillManagementSystem"),
            ("Clean Commit History", "Version-controlled with sanitized configuration and complete automated build scripts."),
            ("Documentation & Badges", "Comprehensive README with architecture diagrams, setup instructions, and college viva Q&A."),
            ("Open Source Standards", "Structured Maven pom.xml with clear directory segregation for models, DAOs, controllers, and services.")
        ]),
        ("Offline Desktop Package (ZIP)", C_AMBER, [
            ("Portable Archive", "Telephone-Bill-Management-System.zip (~400 KB compact archive)."),
            ("One-Click Windows Launcher", "Includes run.bat with automated environment detection and dependency resolution."),
            ("Local SQLite Persistence", "Preloaded with sample telecom data; zero database server configuration required."),
            ("Standalone Executable JAR", "Packaged target/telephone-bill-system-1.0.0.jar ready for classroom projector demonstration.")
        ])
    ]

    for i, (dtitle, dcolor, ditems) in enumerate(dep_cards):
        cx = start_x + i * (card_w + Inches(0.2))
        create_card(s12, cx, Inches(1.8), card_w, Inches(4.8), C_CARD, dcolor)

        tb = s12.shapes.add_textbox(cx + Inches(0.25), Inches(2.0), card_w - Inches(0.5), Inches(4.4))
        tf = tb.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = dtitle
        p.font.name = "Segoe UI"
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = dcolor
        p.space_after = Pt(12)

        for ititle, idesc in ditems:
            p = tf.add_paragraph()
            p.text = f"• {ititle}:"
            p.font.name = "Segoe UI"
            p.font.size = Pt(10.5)
            p.font.bold = True
            p.font.color.rgb = C_WHITE

            p2 = tf.add_paragraph()
            p2.text = idesc
            p2.font.name = "Segoe UI"
            p2.font.size = Pt(9.5)
            p2.font.color.rgb = C_SLATE_400
            p2.space_after = Pt(8)

    add_footer(s12, 12)

    # ==========================================
    # SLIDE 13: CONCLUSION & FUTURE SCOPE
    # ==========================================
    s13 = prs.slides.add_slide(blank_layout)
    add_bg(s13)
    add_header(s13, "Conclusion & Roadmap", "Project Outcomes & Future Scope", "Reflections on project milestones and future directions for telecommunication infrastructure")

    create_card(s13, Inches(0.8), Inches(1.8), Inches(5.6), Inches(4.8), C_CARD, C_EMERALD)
    tb = s13.shapes.add_textbox(Inches(1.1), Inches(2.0), Inches(5.0), Inches(4.4))
    tf = tb.text_frame
    tf.word_wrap = True

    p = tf.paragraphs[0]
    p.text = "🏆 Key Milestones Achieved"
    p.font.name = "Segoe UI"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = C_EMERALD
    p.space_after = Pt(12)

    achievements = [
        ("Production-Grade Java Desktop System", "Fully working Java 21 / JavaFX desktop software following rigorous MVC separation, equipped with dark/light themes and SQLite persistence."),
        ("100% Automated Test Coverage", "All 10 unit and integration tests successfully verified billing accuracy, free minute deductions, and database auto-bootstrapping."),
        ("Airtel/Jio Styled Vector Invoicing", "Complete professional billing statement compliant with TRAI and Indian GST guidelines with instant PDF export."),
        ("Public Web Deployment on Vercel", "Zero-friction cloud hosting allowing anyone to interact with the billing suite on any device worldwide.")
    ]

    for atitle, adesc in achievements:
        p = tf.add_paragraph()
        p.text = f"✓  {atitle}"
        p.font.name = "Segoe UI"
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = C_WHITE
        p2 = tf.add_paragraph()
        p2.text = f"    {adesc}"
        p2.font.name = "Segoe UI"
        p2.font.size = Pt(10)
        p2.font.color.rgb = C_SLATE_400
        p2.space_after = Pt(8)

    create_card(s13, Inches(6.9), Inches(1.8), Inches(5.6), Inches(4.8), C_CARD, C_CYAN)
    tb2 = s13.shapes.add_textbox(Inches(7.2), Inches(2.0), Inches(5.0), Inches(4.4))
    tf2 = tb2.text_frame
    tf2.word_wrap = True

    p = tf2.paragraphs[0]
    p.text = "🚀 Future Enhancement Roadmap"
    p.font.name = "Segoe UI"
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = C_CYAN
    p.space_after = Pt(12)

    roadmap = [
        ("Online Payment Gateway Integration", "Direct integration with Razorpay / Stripe webhooks for instant automated invoice settlement without manual intervention."),
        ("5G VoNR & Data Usage Meters", "Expanding the billing engine to rate dynamic packet data consumption and high-bandwidth 5G slicing tiers."),
        ("Automated SMS & Email Dispatch", "Integrating Twilio / SendGrid APIs to dispatch itemized PDF invoices and payment reminder notifications automatically."),
        ("Customer Self-Service Mobile App", "Extending the backend APIs to power Flutter / React Native Android & iOS mobile applications for subscribers.")
    ]

    for rtitle, rdesc in roadmap:
        p = tf2.add_paragraph()
        p.text = f"➔  {rtitle}"
        p.font.name = "Segoe UI"
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = C_WHITE
        p2 = tf2.add_paragraph()
        p2.text = f"    {rdesc}"
        p2.font.name = "Segoe UI"
        p2.font.size = Pt(10)
        p2.font.color.rgb = C_SLATE_400
        p2.space_after = Pt(8)

    p = tf2.add_paragraph()
    p.text = "Thank You! Questions & Discussion Welcome."
    p.font.name = "Segoe UI"
    p.font.size = Pt(12.5)
    p.font.bold = True
    p.font.color.rgb = C_AMBER
    p.alignment = PP_ALIGN.CENTER

    add_footer(s13, 13)

    out_file = "Telephone_Bill_Management_System_Team07.pptx"
    prs.save(out_file)
    print(f"Presentation saved successfully to {out_file} ({os.path.getsize(out_file)} bytes)")

if __name__ == "__main__":
    create_deck()
