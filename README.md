# ⚡ APEX TELECOM — Telephone Bill Management System

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://apex-telecom-billing-gurramashlesh-8133s-projects.vercel.app)
[![Presentation PPTX](https://img.shields.io/badge/Presentation-Team%2007%20PPTX-D24726?style=for-the-badge&logo=microsoftpowerpoint&logoColor=white)](Telephone_Bill_Management_System_Team07.pptx)
[![Java Version](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/)
[![JavaFX](https://img.shields.io/badge/GUI-JavaFX%2021-FF5722?style=for-the-badge&logo=javafx&logoColor=white)](https://openjfx.io/)
[![Database](https://img.shields.io/badge/Database-SQLite%20JDBC-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://sqlite.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

A production-grade, visually stunning **Telephone Bill Management System** built as a college major project. It features both:
1. **Desktop Application**: Pure **Java 21 / JavaFX (FXML + CSS)** with **SQLite** and **OpenPDF** vector invoice export.
2. **Live Production Web App**: Hoisted on **Vercel** with real-time billing calculations, interactive Chart.js analytics, and in-browser PDF generation.

---

## 🌐 Live Web Version

- **Live URL**: [https://apex-telecom-billing-gurramashlesh-8133s-projects.vercel.app](https://apex-telecom-billing-gurramashlesh-8133s-projects.vercel.app)
- **Directory**: `TelephoneBillWeb/`

---

## 📸 Key System Features

### 1. Executive Operational Dashboard
- **4 Real-Time KPI Cards**: Monthly Revenue (₹995.45), Active Subscribers (5 Active), Overdue Invoices (1 Overdue), Total Billable Minutes (107 mins).
- **Interactive Visualizations**: 6-Month Monthly Billing Trends (`BarChart`) and Subscriber Tariff Plan Distribution (`DoughnutChart`).
- **Quick Actions**: One-click generation of monthly bills, CDR logging, and subscriber registration for viva presentations.

### 2. Subscriber Lifecycle & KYC Management (CRUD)
- Complete subscriber directory: register new lines, modify subscriber profile, search, filter by status (`ACTIVE`, `SUSPENDED`, `INACTIVE`), and assign tariff packages.

### 3. Call Detail Records (CDR) & Automated Prefix Detection
- Call logging engine with **real-time prefix classification**:
  - `+` or `00` (non-Indian prefix) $\to$ **ISD (International)**
  - `0` prefix (trunk area codes, e.g. `022`, `080`, `011`) $\to$ **STD (National Long Distance)**
  - `+91` or 10-digit mobile numbers $\to$ **Local Call**
- Live cost estimation preview based on call duration.
- **Bulk CSV Import**: Import hundreds of call records directly from a CSV file (`sample_data/sample_call_logs.csv`).

### 4. Automated Telecom Billing Engine
- Implemented in `BillingEngine.java`:
  - **Free-minutes allowance** deduction from local calls.
  - Duration ceiling to standard 60-second telecom billing pulses.
  - Usage calculation across Local, STD, and ISD tariff rates.
  - Fixed monthly plan rental.
  - Statutory GST (18% — 9% CGST + 9% SGST).
  - Late payment surcharges and promotional credits.

### 5. Authentic Telecom Tax Invoice (Centerpiece)
- Visual invoice layout mimicking real-world telecom statements (**Airtel / Jio / BSNL style**):
  - Company header, tax ID (`GSTIN: 27AAACA1234F1Z5`), subscriber account card.
  - Itemized summary of charges with CGST & SGST breakdown.
  - Complete itemized Call Detail Records (CDR) table.
  - Dynamic status stamps (`PAID`, `UNPAID`, `OVERDUE`).
  - **Vector PDF Generator**: Instant export via OpenPDF (desktop) and `html2pdf.js` (web).

### 6. Multi-Mode Payment Tracking
- Settle invoices via **UPI, Credit Card, Debit Card, Net Banking, or Cash**.
- Automatic invoice status transitions (`PAID`, `UNPAID`, `OVERDUE`).

### 7. Tariff Plan Management
- Configure packages with custom monthly rental, free allowances, and pulse rates (Local, STD, ISD).

### 8. Analytics & Top Callers Leaderboard
- Financial metrics: Cumulative Invoiced, Total Settled & Collected, Outstanding Receivables.
- Subscriber leaderboard ranked by billable talk time with 🥇 1st, 🥈 2nd, 🥉 3rd medals and CSV export.

---

## 🛠️ Technology Stack

| Component | Desktop Version | Web Version |
| :--- | :--- | :--- |
| **Language** | Java 21 (LTS) | Modern JavaScript (ES6+) |
| **User Interface** | JavaFX 21 (FXML + CSS) | HTML5, Tailwind CSS, Lucide Icons |
| **Database** | SQLite 3 via JDBC | LocalStorage Persistence & In-Memory Store |
| **Charts** | JavaFX Charts (`BarChart`, `PieChart`) | Chart.js |
| **PDF Generation** | OpenPDF 1.3.40 (Vector PDF) | html2pdf.js / jsPDF |
| **Build & Deployment** | Apache Maven 3.9+ | Vercel Serverless Hosting |
| **Testing** | JUnit 5 (10/10 Passing) | BrowserOS neo Automation Suite |

---

## 📂 Repository Structure

```
├── TelephoneBillManagementSystem/       # Java 21 / JavaFX Desktop Application
│   ├── pom.xml                          # Maven build configuration
│   ├── run.bat                          # One-click Windows launcher
│   ├── src/
│   │   ├── main/java/com/telecom/
│   │   │   ├── AppLauncher.java         # Main executable entry point
│   │   │   ├── MainApp.java             # JavaFX Application lifecycle
│   │   │   ├── model/                   # Data models (Customer, TariffPlan, CallRecord, Bill, Payment, User)
│   │   │   ├── dao/                     # SQLite JDBC queries & DatabaseManager
│   │   │   ├── service/                 # BillingEngine, CallService, PdfInvoiceService, ReportService
│   │   │   ├── controller/              # JavaFX UI Controllers
│   │   │   └── util/                    # Currency, Date, Validation, Alert helpers
│   │   ├── main/resources/
│   │   │   ├── database/                # schema.sql (DDL) and seed_data.sql
│   │   │   ├── fxml/                    # Screen layouts (dashboard, bills, calls, customers, etc.)
│   │   │   ├── css/                     # styles.css, dark_theme.css, light_theme.css
│   │   │   └── sample_data/             # sample_call_logs.csv
│   │   └── test/java/com/telecom/       # JUnit 5 test cases
│   └── README.md
│
├── TelephoneBillWeb/                    # Web Application deployed to Vercel
│   ├── index.html                       # Responsive Single Page Application
│   ├── app.js                           # State store, billing engine, prefix detector, charts
│   ├── vercel.json                      # Vercel routing configuration
│   └── package.json                     # Web metadata
│
├── run.bat                              # Root one-click launcher for the Java app
├── Telephone-Bill-Management-System.zip # Standalone ~400 KB shareable archive
└── README.md                            # Main project overview
```

---

## 🚀 Quick Start Guide

### Running the Desktop Java Application
1. **One-Click Launch (Windows)**:
   Double-click [`run.bat`](run.bat) in the project root.
2. **Or via Maven**:
   ```bash
   cd TelephoneBillManagementSystem
   mvn clean javafx:run
   ```
3. **Run Automated Test Suite**:
   ```bash
   cd TelephoneBillManagementSystem
   mvn test
   ```

### Default Login Credentials
- **Administrator**: `admin` / `admin123` (or click `🔑 Admin Login` demo button)
- **Staff User**: `staff` / `staff123` (or click `👤 Staff Login` demo button)

---

## 🎓 College Viva & Oral Examination Q&A

<details>
<summary><strong>Click to expand Viva Examination Q&A</strong></summary>

### Q1: What design pattern is used in this project?
> **Answer**: The **Model-View-Controller (MVC)** architectural pattern. The **Model** (`Customer`, `Bill`, `CallRecord`) encapsulates data and business entities; the **View** (`FXML` files + `CSS`) handles UI presentation; and the **Controller** (`DashboardController`, `BillController`, etc.) manages user events and orchestrates data flow between the view and DAOs/services.

### Q2: How does the billing engine calculate final amounts?
> **Answer**: 
> 1. Local call minutes are summed (rounded up to the nearest minute per standard telecom pulse rate).
> 2. Free minutes included in the tariff plan are deducted from local call minutes first.
> 3. Net call charges are calculated: `(Billable Local Mins × Rate) + (STD Mins × Rate) + (ISD Mins × Rate)`.
> 4. Monthly plan rental is added to establish the taxable subtotal.
> 5. Statutory GST at 18% is applied (`9% CGST + 9% SGST`).
> 6. Late fees are added and promotional discounts subtracted to yield the final payable amount.

### Q3: Why use SQLite instead of MySQL or PostgreSQL?
> **Answer**: SQLite is serverless, zero-configuration, and stores the entire database in a single local file (`telecom.db`). It eliminates the requirement for evaluators or users to configure database servers, while still supporting standard SQL queries, foreign keys, and transactions. The DAO layer abstracts SQL queries, allowing a future transition to MySQL by merely switching the JDBC connection string.

### Q4: How does call prefix auto-detection work?
> **Answer**: The `CallService` inspects the leading digits of the destination phone number:
> - Prefix `+` or `00` (excluding `+91`/`0091`) classifies the call as **ISD (International)**.
> - Leading `0` with standard area codes (`022`, `080`, `011`) classifies the call as **STD (National Long Distance)**.
> - `+91` or domestic 10-digit formats are classified as **Local Calls**.

</details>

---

## 📜 License
This project is open-source and available under the [MIT License](LICENSE).
