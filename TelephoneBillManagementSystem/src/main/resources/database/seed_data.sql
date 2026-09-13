-- Sample Seed Data for Telephone Bill Management System

-- 1. Preloaded Users (Admin & Staff)
-- Passwords: 'admin123' and 'staff123'
INSERT OR IGNORE INTO users (id, username, password_hash, salt, full_name, role, email) VALUES
(1, 'admin', '8218d66f35a4be0224d67d71002f0cb0c22f3cc8289862cb1ce07b1e1e6fd264', 'telecom_salt_2026', 'Administrator', 'ADMIN', 'admin@apextelecom.com'),
(2, 'staff', '91765df23b26f2961feb8163612c86779beeee3a72a4e6255fd1083ab7f34e54', 'telecom_salt_2026', 'Support Staff', 'STAFF', 'staff@apextelecom.com');

-- 2. Preloaded Tariff Plans
INSERT OR IGNORE INTO tariff_plans (id, name, rate_per_min_local, rate_per_min_std, rate_per_min_isd, monthly_rental, free_minutes, sms_rate, data_rate, description) VALUES
(1, 'Prepaid Starter', 0.60, 1.20, 8.00, 99.00, 50, 0.50, 15.00, 'Entry-level budget plan with low rental and essential calling'),
(2, 'Postpaid Silver', 0.45, 1.00, 6.50, 249.00, 150, 0.25, 10.00, 'Popular postpaid plan suitable for regular household callers'),
(3, 'Postpaid Gold Unlimited', 0.25, 0.70, 5.00, 499.00, 400, 0.15, 8.00, 'High-usage plan with generous free minutes and discounted ISD'),
(4, 'Corporate Enterprise', 0.15, 0.40, 3.50, 899.00, 1200, 0.05, 5.00, 'Tailored for businesses requiring intensive national and international connectivity');

-- 3. Preloaded Customers
INSERT OR IGNORE INTO customers (id, name, phone_number, email, address, connection_date, plan_id, status) VALUES
(1, 'Rajesh Sharma', '+919820011223', 'rajesh.sharma@gmail.com', 'Flat 402, Sea View Apartments, Bandra West, Mumbai - 400050', '2025-01-10', 2, 'ACTIVE'),
(2, 'Priya Patel', '+919845033445', 'priya.patel@outlook.com', 'Villa 12, Palm Meadows, Whitefield, Bengaluru - 560066', '2025-02-15', 3, 'ACTIVE'),
(3, 'Amit Verma', '+919811055667', 'amit.verma@yahoo.com', 'B-34, Connaught Place, New Delhi - 110001', '2025-03-01', 1, 'ACTIVE'),
(4, 'Sneha Reddy', '+919884077889', 'sneha.reddy@techcorp.in', 'Plot 89, Jubilee Hills, Road No. 36, Hyderabad - 500033', '2025-04-12', 4, 'ACTIVE'),
(5, 'Vikramaditya Roy', '+919830099001', 'vikram.roy@kolkata.ac.in', '15A, Ballygunge Circular Road, Kolkata - 700019', '2025-05-20', 2, 'ACTIVE'),
(6, 'Ananya Sengupta', '+919876543210', 'ananya.s@freemail.com', 'Shop 7, MG Road, Pune - 411001', '2025-06-05', 1, 'SUSPENDED');

-- 4. Sample Call Records for Customer 1 (Rajesh Sharma)
INSERT OR IGNORE INTO call_records (id, customer_id, call_timestamp, destination_number, call_type, duration_seconds, computed_cost) VALUES
(1, 1, '2026-08-02 09:30:15', '+919820099887', 'LOCAL', 180, 1.35),
(2, 1, '2026-08-05 14:12:00', '02224567890', 'STD', 320, 5.33),
(3, 1, '2026-08-10 18:45:22', '+14155552671', 'ISD', 240, 26.00),
(4, 1, '2026-08-15 11:20:05', '+919820055443', 'LOCAL', 125, 0.94),
(5, 1, '2026-08-22 20:05:40', '08023456789', 'STD', 450, 7.50),
(6, 1, '2026-08-28 16:30:10', '+442079460912', 'ISD', 190, 20.58);

-- Sample Call Records for Customer 2 (Priya Patel)
INSERT OR IGNORE INTO call_records (id, customer_id, call_timestamp, destination_number, call_type, duration_seconds, computed_cost) VALUES
(7, 2, '2026-08-01 10:15:00', '+919845012345', 'LOCAL', 600, 2.50),
(8, 2, '2026-08-08 11:45:30', '01123456789', 'STD', 420, 4.90),
(9, 2, '2026-08-14 17:00:15', '+12025550199', 'ISD', 300, 25.00),
(10, 2, '2026-08-20 09:10:00', '+919845098765', 'LOCAL', 360, 1.50);

-- Sample Call Records for Customer 4 (Sneha Reddy - Corporate)
INSERT OR IGNORE INTO call_records (id, customer_id, call_timestamp, destination_number, call_type, duration_seconds, computed_cost) VALUES
(11, 4, '2026-08-03 08:30:00', '+919884011222', 'LOCAL', 900, 2.25),
(12, 4, '2026-08-07 15:20:00', '04027654321', 'STD', 1200, 8.00),
(13, 4, '2026-08-18 19:45:00', '+6567890123', 'ISD', 650, 37.92),
(14, 4, '2026-08-25 12:00:00', '+919884099888', 'LOCAL', 480, 1.20);

-- 5. Sample Bills (August 2026)
INSERT OR IGNORE INTO bills (id, bill_number, customer_id, billing_month, billing_year, total_call_charges, rental_charges, tax_percentage, tax_amount, discount_amount, late_fee, total_amount, due_date, payment_status, generated_date) VALUES
(1, 'INV-202608-001', 1, 8, 2026, 61.70, 249.00, 18.0, 55.93, 0.0, 0.0, 366.63, '2026-09-15', 'PAID', '2026-09-01'),
(2, 'INV-202608-002', 2, 8, 2026, 33.90, 499.00, 18.0, 95.92, 0.0, 0.0, 628.82, '2026-09-15', 'UNPAID', '2026-09-01'),
(3, 'INV-202607-003', 3, 7, 2026, 45.00, 99.00, 18.0, 25.92, 0.0, 50.0, 219.92, '2026-08-15', 'OVERDUE', '2026-08-01');

-- 6. Sample Payment for Bill 1
INSERT OR IGNORE INTO payments (id, bill_id, amount_paid, payment_date, payment_mode, transaction_reference, notes) VALUES
(1, 1, 366.63, '2026-09-05', 'UPI', 'UPI/20260905/7891234', 'Settled via Google Pay');
