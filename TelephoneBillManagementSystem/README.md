# ⚡ APEX TELECOM — Telephone Bill Management System

A production-grade, visually stunning desktop application for telecom subscriber management, Call Detail Record (CDR) logging, automated tariff calculations, and itemized invoice generation. Built as a college major project in **Java 21**, **JavaFX**, **SQLite**, and **OpenPDF** following the **Model-View-Controller (MVC)** design pattern.

---

## 📸 Key Features

1. **Executive Operational Dashboard**:
   - Real-time KPI summary cards: Monthly Revenue, Active Subscribers, Overdue Invoices, and Total Billable Minutes.
   - Interactive JavaFX Charts: Monthly Billing Trends (`BarChart`) and Subscriber Plan Distribution (`PieChart`).
2. **Subscriber Management (CRUD)**:
   - Complete subscriber lifecycle: register, edit, search, filter by status (`ACTIVE`, `SUSPENDED`, `INACTIVE`), and assign tariff plans.
3. **Call Detail Records (CDR) & Prefix Auto-Detection**:
   - Manual call logger with **real-time call category detection**:
     - `+` or `00` (non-Indian prefix) $\to$ **ISD (International)**
     - `0` prefix (trunk area codes, e.g. 022, 080) $\to$ **STD (National Long Distance)**
     - `+91` or 10-digit mobile numbers $\to$ **Local Call**
   - Live tariff estimation preview while typing call duration.
   - **Bulk CSV Import**: Import hundreds of call logs directly from a CSV file (`timestamp,destination,duration_seconds`).
4. **Automated Telecom Billing Engine**:
   - Deducts **free-minutes allowance** first against local calls.
   - Rounds duration to standard telecom billing pulses (ceil to whole minutes).
   - Computes call usage across Local, STD, and ISD tariff rates.
   - Adds monthly plan rental.
   - Applies statutory GST (18% — 9% CGST + 9% SGST).
   - Adjusts late surcharges and promotional credits.
5. **Authentic Telecom Invoice (Centerpiece)**:
   - Visual invoice layout mimicking real-world telecom statements (**Airtel / Jio / BSNL style**).
   - In-app printable preview with company header, GSTIN, subscriber account card, itemized summary of charges, and full CDR call log table.
   - **OpenPDF Invoice Generator**: Instant export of professional vector PDF invoices.
6. **Multi-Mode Payment Tracking**:
   - Settle bills via **UPI, Credit Card, Debit Card, Net Banking, or Cash**.
   - Auto-updates invoice state (`PAID`, `PARTIALLY_PAID`, `UNPAID`, `OVERDUE`).
7. **Tariff Plan Management**:
   - Create, edit, and configure custom calling packages with independent Local, STD, ISD rates, monthly rental, and free minutes.
8. **Role-Based Access Control (RBAC)**:
   - **Admin**: Full access (Tariff Plan modification, deletions, system analytics).
   - **Staff**: Operational access (log calls, register customers, process bills).
   - Passwords hashed with salt via **SHA-256**.
9. **Dark Mode & Light Mode**:
   - One-click modern dark slate theme switch.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 (LTS) |
| **GUI Framework** | JavaFX 21 (FXML + CSS) |
| **Icons & Styling** | Custom CSS3 + Feather Icons (`Ikonli`) |
| **Database** | SQLite 3 via JDBC (`sqlite-jdbc`) |
| **PDF Generation** | OpenPDF 1.3.40 (LGPL/MPL compliant) |
| **Build Tool** | Apache Maven 3.9+ |
| **Testing** | JUnit 5 (`junit-jupiter`) |

---

## 📂 Project Architecture (MVC Pattern)

```
src/
├── main/
│   ├── java/com/telecom/
│   │   ├── AppLauncher.java             # Main wrapper bypassing module path
│   │   ├── MainApp.java                 # JavaFX Application lifecycle & theme manager
│   │   ├── model/                       # Data entities (Customer, TariffPlan, CallRecord, Bill, Payment, User)
│   │   ├── dao/                         # SQLite JDBC queries & DatabaseManager
│   │   ├── service/                     # BillingEngine, CallService, PdfInvoiceService, ReportService, CsvService, AuthService
│   │   ├── controller/                  # JavaFX Controllers (Dashboard, Customer, CallRecord, Bill, Invoice, etc.)
│   │   └── util/                        # Currency, Date, Validation, and Alert utilities
│   └── resources/
│       ├── database/                    # schema.sql (DDL) and seed_data.sql (Demo data)
│       ├── fxml/                        # Clean FXML views for all UI screens
│       ├── css/                         # styles.css, light_theme.css, dark_theme.css
│       └── sample_data/                 # sample_call_logs.csv for demo import
└── test/java/com/telecom/               # JUnit 5 test cases (BillingEngineTest, CallServiceTest)
```

---

## 🚀 How to Run the Application

### Prerequisites
- **Java 21+** installed and available in environment (`java -version`).
- (Optional) Maven installed on PATH, or use the pre-configured launcher.

### Method 1: One-Click Run (Windows)
Double-click `run.bat` in the project root. It will automatically detect Maven, compile dependencies, and launch the application!

### Method 2: Command Line (Maven)
```bash
mvn clean javafx:run
```

### Method 3: Running Unit Tests
```bash
mvn test
```

---

## 🔑 Default Login Credentials

| Role | Username | Password | Permissions |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Full access (Plans CRUD, Delete, Analytics) |
| **Staff** | `staff` | `staff123` | Call Logging, Customer Registration, Billing, Payments |

*(Quick demo buttons are also present directly on the login screen for viva convenience).*

---

## 📐 Billing Calculation Formula

$$\text{Call Minutes} = \left\lceil \frac{\text{Duration in Seconds}}{60} \right\rceil$$

$$\text{Chargeable Local Mins} = \max(0, \text{Total Local Mins} - \text{Free Minutes Allowance})$$

$$\text{Call Charges} = (\text{Local Mins} \times \text{Rate}_{\text{local}}) + (\text{STD Mins} \times \text{Rate}_{\text{std}}) + (\text{ISD Mins} \times \text{Rate}_{\text{isd}})$$

$$\text{Subtotal} = \text{Monthly Rental} + \text{Call Charges}$$

$$\text{GST (18\%)} = \text{Subtotal} \times 0.18 \quad (\text{CGST 9\%} + \text{SGST 9\%})$$

$$\text{Total Payable} = \text{Subtotal} + \text{GST} + \text{Late Fee} - \text{Discounts}$$

---

## 🎓 College Viva & Examination Q&A

### Q1: Why did you choose SQLite over MySQL for this project?
> **Answer**: SQLite requires zero server installation or daemon configuration, making the desktop application completely portable and demo-ready on any computer. The architecture uses standard JDBC interfaces through DAO classes (`CustomerDAO`, `BillDAO`), which means the data access layer can be swapped for MySQL or PostgreSQL by simply altering the JDBC connection string without changing a single line of business logic.

### Q2: How does Call Prefix Auto-Detection work?
> **Answer**: In `CallService.detectCallType()`, the destination telephone string is sanitized and inspected using regex prefix checks:
> - If it starts with `+` or `00` (and not followed by India's `+91`), it is categorized as **ISD**.
> - If it starts with `0` and contains an STD area code (e.g. `022` for Mumbai, `011` for Delhi), it is classified as **STD**.
> - Local mobile numbers (starting with `+91` or 10-digit formats) are classified as **LOCAL**.

### Q3: Explain how the MVC pattern is applied here.
> **Answer**:
> - **Model**: Encapsulates state (`Customer`, `Bill`, `CallRecord`, `TariffPlan`).
> - **View**: Declarative FXML files (`bills.fxml`, `dashboard.fxml`) styled with modern CSS (`styles.css`, `dark_theme.css`).
> - **Controller**: Intermediaries (`BillController`, `DashboardController`) that capture user interactions, communicate with the `service` and `dao` layers, and update UI bindings.
> - **Service Layer**: Houses core business logic like `BillingEngine` and `PdfInvoiceService` independently of UI components, enabling automated testing via JUnit.

### Q4: How are passwords stored and secured?
> **Answer**: Passwords are never stored in plain text. When a user is created, a cryptographic salt is combined with the password and hashed using **SHA-256** (`AuthService.hashPassword`). On login, the entered password is salted, hashed, and matched against the stored hash.

### Q5: How is the database initialized on the first run?
> **Answer**: `DatabaseManager` runs as a singleton. When `getInstance()` is first called, it connects to `telecom.db`, executes `schema.sql` (creating tables and indexes with `IF NOT EXISTS`), and checks if the `users` table is populated. If empty, it automatically executes `seed_data.sql` to populate sample customers, tariff plans, and call records so the application is immediately demo-ready.
