-- Telephone Bill Management System - Database Schema (SQLite)
-- Enables foreign key constraints
PRAGMA foreign_keys = ON;

-- 1. Users Table (Authentication & RBAC)
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'STAFF')),
    email TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tariff Plans Table
CREATE TABLE IF NOT EXISTS tariff_plans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    rate_per_min_local REAL NOT NULL DEFAULT 0.50,
    rate_per_min_std REAL NOT NULL DEFAULT 1.20,
    rate_per_min_isd REAL NOT NULL DEFAULT 6.50,
    monthly_rental REAL NOT NULL DEFAULT 199.00,
    free_minutes INTEGER NOT NULL DEFAULT 100,
    sms_rate REAL NOT NULL DEFAULT 0.25,
    data_rate REAL NOT NULL DEFAULT 10.00,
    description TEXT
);

-- 3. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone_number TEXT UNIQUE NOT NULL,
    email TEXT,
    address TEXT,
    connection_date TEXT NOT NULL,
    plan_id INTEGER NOT NULL REFERENCES tariff_plans(id) ON DELETE RESTRICT,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- 4. Call Records Table
CREATE TABLE IF NOT EXISTS call_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    call_timestamp TEXT NOT NULL,
    destination_number TEXT NOT NULL,
    call_type TEXT NOT NULL CHECK(call_type IN ('LOCAL', 'STD', 'ISD')),
    duration_seconds INTEGER NOT NULL CHECK(duration_seconds >= 0),
    computed_cost REAL NOT NULL DEFAULT 0.0
);

-- 5. Bills Table
CREATE TABLE IF NOT EXISTS bills (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bill_number TEXT UNIQUE NOT NULL,
    customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    billing_month INTEGER NOT NULL CHECK(billing_month BETWEEN 1 AND 12),
    billing_year INTEGER NOT NULL,
    total_call_charges REAL NOT NULL DEFAULT 0.0,
    rental_charges REAL NOT NULL DEFAULT 0.0,
    tax_percentage REAL NOT NULL DEFAULT 18.0,
    tax_amount REAL NOT NULL DEFAULT 0.0,
    discount_amount REAL NOT NULL DEFAULT 0.0,
    late_fee REAL NOT NULL DEFAULT 0.0,
    total_amount REAL NOT NULL DEFAULT 0.0,
    due_date TEXT NOT NULL,
    payment_status TEXT NOT NULL DEFAULT 'UNPAID' CHECK(payment_status IN ('PAID', 'PARTIALLY_PAID', 'UNPAID', 'OVERDUE')),
    generated_date TEXT NOT NULL,
    UNIQUE(customer_id, billing_year, billing_month)
);

-- 6. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bill_id INTEGER NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    amount_paid REAL NOT NULL,
    payment_date TEXT NOT NULL,
    payment_mode TEXT NOT NULL CHECK(payment_mode IN ('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING')),
    transaction_reference TEXT,
    notes TEXT
);

-- Useful indexes for search and billing lookups
CREATE INDEX IF NOT EXISTS idx_customer_phone ON customers(phone_number);
CREATE INDEX IF NOT EXISTS idx_call_customer ON call_records(customer_id, call_timestamp);
CREATE INDEX IF NOT EXISTS idx_bill_customer ON bills(customer_id);
CREATE INDEX IF NOT EXISTS idx_bill_status ON bills(payment_status);
CREATE INDEX IF NOT EXISTS idx_payment_bill ON payments(bill_id);
