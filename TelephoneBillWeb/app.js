/**
 * Apex Telecom — Telephone Billing & Subscriber Management Suite
 * Full Client-Side Web Application Logic
 * 
 * Implements:
 * - LocalStorage data persistence with realistic seed data matching Java SQLite schema
 * - BillingEngine matching Java 21 logic (Free minutes deduction, 18% GST CGST/SGST, late fee, discounts)
 * - Real-time Prefix Classification (Local, STD, ISD)
 * - Interactive Chart.js charts (Billing trends & Plan distribution)
 * - Airtel/Jio styled Tax Invoice with itemized CDR table & html2pdf export
 * - Full CRUD for Subscribers, Call Records, Bills, and Payment recording
 */

// ==================== DEFAULT SEED DATA ====================
const DEFAULT_PLANS = [
    { id: 1, name: 'Prepaid Starter', rateLocal: 0.60, rateStd: 1.20, rateIsd: 8.00, monthlyRental: 99.00, freeMinutes: 50, smsRate: 0.50, dataRate: 15.00, description: 'Entry-level budget plan with low rental and essential calling.' },
    { id: 2, name: 'Postpaid Silver', rateLocal: 0.45, rateStd: 1.00, rateIsd: 6.50, monthlyRental: 249.00, freeMinutes: 150, smsRate: 0.25, dataRate: 10.00, description: 'Popular postpaid plan suitable for regular household callers.' },
    { id: 3, name: 'Postpaid Gold Unlimited', rateLocal: 0.25, rateStd: 0.70, rateIsd: 5.00, monthlyRental: 499.00, freeMinutes: 400, smsRate: 0.15, dataRate: 8.00, description: 'High-usage plan with generous free minutes and discounted ISD.' },
    { id: 4, name: 'Corporate Enterprise', rateLocal: 0.15, rateStd: 0.40, rateIsd: 3.50, monthlyRental: 899.00, freeMinutes: 1200, smsRate: 0.05, dataRate: 5.00, description: 'Tailored for businesses requiring intensive national and international connectivity.' }
];

const DEFAULT_CUSTOMERS = [
    { id: 1, name: 'Rajesh Sharma', phone: '+919820011223', email: 'rajesh.sharma@gmail.com', address: 'Flat 402, Sea View Apartments, Bandra West, Mumbai - 400050', connectionDate: '2025-01-10', planId: 2, status: 'ACTIVE' },
    { id: 2, name: 'Priya Patel', phone: '+919845033445', email: 'priya.patel@outlook.com', address: 'Villa 12, Palm Meadows, Whitefield, Bengaluru - 560066', connectionDate: '2025-02-15', planId: 3, status: 'ACTIVE' },
    { id: 3, name: 'Amit Verma', phone: '+919811055667', email: 'amit.verma@yahoo.com', address: 'B-34, Connaught Place, New Delhi - 110001', connectionDate: '2025-03-01', planId: 1, status: 'ACTIVE' },
    { id: 4, name: 'Sneha Reddy', phone: '+919884077889', email: 'sneha.reddy@techcorp.in', address: 'Plot 89, Jubilee Hills, Road No. 36, Hyderabad - 500033', connectionDate: '2025-04-12', planId: 4, status: 'ACTIVE' },
    { id: 5, name: 'Vikramaditya Roy', phone: '+919830099001', email: 'vikram.roy@kolkata.ac.in', address: '15A, Ballygunge Circular Road, Kolkata - 700019', connectionDate: '2025-05-20', planId: 2, status: 'ACTIVE' },
    { id: 6, name: 'Ananya Sengupta', phone: '+919876543210', email: 'ananya.s@freemail.com', address: 'Shop 7, MG Road, Pune - 411001', connectionDate: '2025-06-05', planId: 1, status: 'SUSPENDED' }
];

const DEFAULT_CALLS = [
    // Rajesh Sharma (Cust 1)
    { id: 1, customerId: 1, timestamp: '2026-08-02 09:30:15', destination: '+919820099887', callType: 'LOCAL', durationSeconds: 180, cost: 1.35 },
    { id: 2, customerId: 1, timestamp: '2026-08-05 14:12:00', destination: '02224567890', callType: 'STD', durationSeconds: 320, cost: 5.33 },
    { id: 3, customerId: 1, timestamp: '2026-08-10 18:45:22', destination: '+14155552671', callType: 'ISD', durationSeconds: 240, cost: 26.00 },
    { id: 4, customerId: 1, timestamp: '2026-08-15 11:20:05', destination: '+919820055443', callType: 'LOCAL', durationSeconds: 125, cost: 0.94 },
    { id: 5, customerId: 1, timestamp: '2026-08-22 20:05:40', destination: '08023456789', callType: 'STD', durationSeconds: 450, cost: 7.50 },
    { id: 6, customerId: 1, timestamp: '2026-08-28 16:30:10', destination: '+442079460912', callType: 'ISD', durationSeconds: 190, cost: 20.58 },

    // Priya Patel (Cust 2)
    { id: 7, customerId: 2, timestamp: '2026-08-01 10:15:00', destination: '+919845012345', callType: 'LOCAL', durationSeconds: 600, cost: 2.50 },
    { id: 8, customerId: 2, timestamp: '2026-08-08 11:45:30', destination: '01123456789', callType: 'STD', durationSeconds: 420, cost: 4.90 },
    { id: 9, customerId: 2, timestamp: '2026-08-14 17:00:15', destination: '+12025550199', callType: 'ISD', durationSeconds: 300, cost: 25.00 },
    { id: 10, customerId: 2, timestamp: '2026-08-20 09:10:00', destination: '+919845098765', callType: 'LOCAL', durationSeconds: 360, cost: 1.50 },

    // Sneha Reddy (Cust 4)
    { id: 11, customerId: 4, timestamp: '2026-08-03 08:30:00', destination: '+919884011222', callType: 'LOCAL', durationSeconds: 900, cost: 2.25 },
    { id: 12, customerId: 4, timestamp: '2026-08-07 15:20:00', destination: '04027654321', callType: 'STD', durationSeconds: 1200, cost: 8.00 },
    { id: 13, customerId: 4, timestamp: '2026-08-18 19:45:00', destination: '+6567890123', callType: 'ISD', durationSeconds: 650, cost: 37.92 },
    { id: 14, customerId: 4, timestamp: '2026-08-25 12:00:00', destination: '+919884099888', callType: 'LOCAL', durationSeconds: 480, cost: 1.20 }
];

const DEFAULT_BILLS = [
    {
        id: 1,
        billNumber: 'INV-202608-001',
        customerId: 1,
        billingMonth: 8,
        billingYear: 2026,
        callCharges: 61.70,
        rentalCharges: 249.00,
        taxPercentage: 18.0,
        taxAmount: 55.93,
        discountAmount: 0.0,
        lateFee: 0.0,
        totalAmount: 366.63,
        dueDate: '2026-09-15',
        paymentStatus: 'PAID',
        generatedDate: '2026-09-01'
    },
    {
        id: 2,
        billNumber: 'INV-202608-002',
        customerId: 2,
        billingMonth: 8,
        billingYear: 2026,
        callCharges: 33.90,
        rentalCharges: 499.00,
        taxPercentage: 18.0,
        taxAmount: 95.92,
        discountAmount: 0.0,
        lateFee: 0.0,
        totalAmount: 628.82,
        dueDate: '2026-09-15',
        paymentStatus: 'UNPAID',
        generatedDate: '2026-09-01'
    },
    {
        id: 3,
        billNumber: 'INV-202607-003',
        customerId: 3,
        billingMonth: 7,
        billingYear: 2026,
        callCharges: 45.00,
        rentalCharges: 99.00,
        taxPercentage: 18.0,
        taxAmount: 25.92,
        discountAmount: 0.0,
        lateFee: 50.0,
        totalAmount: 219.92,
        dueDate: '2026-08-15',
        paymentStatus: 'OVERDUE',
        generatedDate: '2026-08-01'
    }
];

const DEFAULT_PAYMENTS = [
    {
        id: 1,
        billId: 1,
        amount: 366.63,
        paymentDate: '2026-09-05',
        paymentMode: 'UPI',
        reference: 'UPI/20260905/7891234',
        notes: 'Settled via Google Pay'
    }
];

// ==================== STATE MANAGEMENT ====================
class TelecomStore {
    constructor() {
        this.STORAGE_KEY = 'apex_telecom_v1_store';
        this.load();
    }

    load() {
        const stored = localStorage.getItem(this.STORAGE_KEY);
        if (stored) {
            try {
                const parsed = JSON.parse(stored);
                this.plans = parsed.plans || DEFAULT_PLANS;
                this.customers = parsed.customers || DEFAULT_CUSTOMERS;
                this.calls = parsed.calls || DEFAULT_CALLS;
                this.bills = parsed.bills || DEFAULT_BILLS;
                this.payments = parsed.payments || DEFAULT_PAYMENTS;
                return;
            } catch (e) {
                console.error('Failed to parse stored data, resetting:', e);
            }
        }
        this.reset();
    }

    save() {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify({
            plans: this.plans,
            customers: this.customers,
            calls: this.calls,
            bills: this.bills,
            payments: this.payments
        }));
    }

    reset() {
        this.plans = JSON.parse(JSON.stringify(DEFAULT_PLANS));
        this.customers = JSON.parse(JSON.stringify(DEFAULT_CUSTOMERS));
        this.calls = JSON.parse(JSON.stringify(DEFAULT_CALLS));
        this.bills = JSON.parse(JSON.stringify(DEFAULT_BILLS));
        this.payments = JSON.parse(JSON.stringify(DEFAULT_PAYMENTS));
        this.save();
    }
}

const store = new TelecomStore();

// ==================== BILLING ENGINE LOGIC ====================
const BillingEngine = {
    DEFAULT_TAX_PERCENTAGE: 18.0,

    round2(val) {
        return Math.round((val + Number.EPSILON) * 100) / 100;
    },

    computeBillBreakdown(plan, calls, discount = 0, lateFee = 0) {
        const breakdown = {
            rentalCharges: this.round2(plan.monthlyRental),
            discountAmount: this.round2(discount),
            lateFee: this.round2(lateFee),
            totalCallSeconds: 0,
            totalCallMinutes: 0,
            freeMinutesUsed: 0,
            freeMinutesRemaining: 0,
            localCharges: 0,
            stdCharges: 0,
            isdCharges: 0,
            totalCallCharges: 0,
            taxAmount: 0,
            totalPayable: 0
        };

        let localSecs = 0;
        let stdSecs = 0;
        let isdSecs = 0;

        calls.forEach(call => {
            const secs = call.durationSeconds;
            breakdown.totalCallSeconds += secs;
            if (call.callType === 'LOCAL') localSecs += secs;
            else if (call.callType === 'STD') stdSecs += secs;
            else if (call.callType === 'ISD') isdSecs += secs;
        });

        // 60-second pulse rounding
        const localMins = Math.ceil(localSecs / 60);
        const stdMins = Math.ceil(stdSecs / 60);
        const isdMins = Math.ceil(isdSecs / 60);
        breakdown.totalCallMinutes = localMins + stdMins + isdMins;

        // Apply free minutes against Local calls first
        let availableFree = plan.freeMinutes || 0;
        let chargeableLocal = localMins;

        if (availableFree > 0) {
            if (chargeableLocal <= availableFree) {
                breakdown.freeMinutesUsed = chargeableLocal;
                breakdown.freeMinutesRemaining = availableFree - chargeableLocal;
                chargeableLocal = 0;
            } else {
                breakdown.freeMinutesUsed = availableFree;
                breakdown.freeMinutesRemaining = 0;
                chargeableLocal -= availableFree;
            }
        }

        breakdown.localCharges = this.round2(chargeableLocal * plan.rateLocal);
        breakdown.stdCharges = this.round2(stdMins * plan.rateStd);
        breakdown.isdCharges = this.round2(isdMins * plan.rateIsd);
        breakdown.totalCallCharges = this.round2(breakdown.localCharges + breakdown.stdCharges + breakdown.isdCharges);

        const subtotal = breakdown.totalCallCharges + breakdown.rentalCharges;
        breakdown.taxAmount = this.round2(subtotal * (this.DEFAULT_TAX_PERCENTAGE / 100));

        const rawTotal = subtotal + breakdown.taxAmount + breakdown.lateFee - breakdown.discountAmount;
        breakdown.totalPayable = this.round2(Math.max(0, rawTotal));

        return breakdown;
    },

    computeCallCost(callType, durationSeconds, plan) {
        const minutes = Math.ceil(durationSeconds / 60);
        let rate = plan.rateLocal;
        if (callType === 'STD') rate = plan.rateStd;
        else if (callType === 'ISD') rate = plan.rateIsd;
        return this.round2(minutes * rate);
    }
};

// ==================== PREFIX DETECTION ====================
function detectCallType(dest) {
    if (!dest) return 'LOCAL';
    const cleaned = dest.replace(/[\s\-()]/g, '');

    if (cleaned.startsWith('+')) {
        if (cleaned.startsWith('+91')) return 'LOCAL';
        return 'ISD';
    }
    if (cleaned.startsWith('00')) {
        if (cleaned.startsWith('0091')) return 'LOCAL';
        return 'ISD';
    }
    if (cleaned.startsWith('0') && cleaned.length > 5) {
        return 'STD';
    }
    return 'LOCAL';
}

// ==================== CHART INSTANCES ====================
let revenueChartInstance = null;
let planChartInstance = null;

// ==================== APP INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
    initLiveDate();
    initMobileMenu();
    populateSelectOptions();
    renderAll();
});

function initLiveDate() {
    const liveDateEl = document.getElementById('liveDate');
    if (liveDateEl) {
        const now = new Date();
        liveDateEl.innerText = now.toLocaleDateString('en-IN', {
            weekday: 'short',
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }
}

function initMobileMenu() {
    const btn = document.getElementById('mobileMenuBtn');
    const sidebar = document.getElementById('sidebar');
    if (btn && sidebar) {
        btn.addEventListener('click', () => {
            sidebar.classList.toggle('hidden');
        });
    }
}

function toggleTheme() {
    const html = document.documentElement;
    const isDark = html.classList.toggle('dark');
    const txt = document.getElementById('themeBtnText');
    if (txt) {
        txt.innerText = isDark ? 'Light Mode' : 'Dark Mode';
    }
    // Re-render charts for theme contrast
    renderCharts();
}

function resetDemoData() {
    if (confirm('Reset all subscribers, call records, and invoices to initial demo seed data?')) {
        store.reset();
        populateSelectOptions();
        renderAll();
        alert('Data successfully reset to initial demo state.');
    }
}

// ==================== TAB SWITCHING ====================
const TAB_TITLES = {
    dashboard: { title: 'Operational Dashboard', subtitle: 'Real-time metrics, revenue performance, and key telecom indicators' },
    customers: { title: 'Subscriber Directory', subtitle: 'Manage active lines, customer KYC, tariff assignments, and provisioning' },
    calls: { title: 'Call Detail Records (CDR)', subtitle: 'Itemized telephone traffic logs with automatic prefix classification' },
    bills: { title: 'Billing & Invoicing Suite', subtitle: 'Generate itemized monthly invoices with 18% GST and settlement tracking' },
    plans: { title: 'Tariff Plans & Packages', subtitle: 'Configure monthly rentals, free allowances, and pulse rates' },
    reports: { title: 'Analytics & Financial Reports', subtitle: 'Gross billing summaries, collection rates, and caller leaderboards' }
};

function switchTab(tabId) {
    const tabIds = ['dashboard', 'customers', 'calls', 'bills', 'plans', 'reports'];
    tabIds.forEach(id => {
        const sec = document.getElementById(`tab-${id}`);
        if (sec) {
            sec.classList.toggle('hidden', id !== tabId);
        }
    });

    document.querySelectorAll('.tab-btn').forEach(btn => {
        if (btn.getAttribute('data-tab') === tabId) {
            btn.classList.add('active');
            btn.classList.remove('hover:bg-slate-800', 'text-slate-300');
        } else {
            btn.classList.remove('active');
            btn.classList.add('hover:bg-slate-800', 'text-slate-300');
        }
    });

    const info = TAB_TITLES[tabId];
    if (info) {
        document.getElementById('pageTitle').innerText = info.title;
        document.getElementById('pageSubtitle').innerText = info.subtitle;
    }

    // Hide mobile sidebar upon selection
    const sidebar = document.getElementById('sidebar');
    if (sidebar && window.innerWidth < 768) {
        sidebar.classList.add('hidden');
    }

    if (tabId === 'dashboard') {
        renderCharts();
    } else if (tabId === 'reports') {
        renderReports();
    }
}

// ==================== POPULATE DROPDOWNS ====================
function populateSelectOptions() {
    // 1. Customer Plan dropdown in Customer modal
    const custPlanSelect = document.getElementById('custPlan');
    if (custPlanSelect) {
        custPlanSelect.innerHTML = store.plans.map(p =>
            `<option value="${p.id}">${p.name} (₹${p.monthlyRental}/mo)</option>`
        ).join('');
    }

    // 2. Customers in Call filter & modals
    const callFilter = document.getElementById('callCustomerFilter');
    const logCallCustomer = document.getElementById('logCallCustomer');
    const genBillCustomer = document.getElementById('genBillCustomer');

    const customerOpts = store.customers.map(c =>
        `<option value="${c.id}">${c.name} (${c.phone})</option>`
    ).join('');

    if (callFilter) {
        callFilter.innerHTML = '<option value="ALL">All Subscribers</option>' + customerOpts;
    }
    if (logCallCustomer) {
        logCallCustomer.innerHTML = customerOpts;
    }
    if (genBillCustomer) {
        genBillCustomer.innerHTML = customerOpts;
    }
}

// ==================== RENDER ALL VIEWS ====================
function renderAll() {
    renderKPIs();
    renderCharts();
    renderCustomersTable();
    renderCallsTable();
    renderBillsTable();
    renderPlansGrid();
    renderReports();
    lucide.createIcons();
}

// ==================== DASHBOARD KPIS & CHARTS ====================
function renderKPIs() {
    // Monthly revenue: sum of August 2026 bills
    const totalRev = store.bills
        .filter(b => b.billingMonth === 8 && b.billingYear === 2026)
        .reduce((sum, b) => sum + b.totalAmount, 0);

    const activeSubs = store.customers.filter(c => c.status === 'ACTIVE').length;
    const overdueCount = store.bills.filter(b => b.paymentStatus === 'OVERDUE').length;
    const totalMins = Math.round(store.calls.reduce((sum, c) => sum + c.durationSeconds, 0) / 60);

    document.getElementById('statRevenue').innerText = `₹${totalRev.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById('statSubscribers').innerText = `${activeSubs} Active`;
    document.getElementById('statOverdue').innerText = `${overdueCount} Invoices`;
    document.getElementById('statMinutes').innerText = `${totalMins} mins`;
}

function renderCharts() {
    const isDark = document.documentElement.classList.contains('dark');
    const textColor = isDark ? '#94a3b8' : '#64748b';
    const gridColor = isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)';

    // 1. Revenue Chart
    const revCtx = document.getElementById('revenueChart');
    if (revCtx) {
        if (revenueChartInstance) revenueChartInstance.destroy();
        revenueChartInstance = new Chart(revCtx, {
            type: 'bar',
            data: {
                labels: ['Mar 2026', 'Apr 2026', 'May 2026', 'Jun 2026', 'Jul 2026', 'Aug 2026'],
                datasets: [{
                    label: 'Invoiced Amount (₹)',
                    data: [840, 1120, 950, 1260, 1080, 1215.37],
                    backgroundColor: 'rgba(59, 130, 246, 0.85)',
                    borderRadius: 8,
                    hoverBackgroundColor: 'rgba(37, 99, 235, 1)'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (ctx) => ` ₹${ctx.raw.toLocaleString('en-IN')}`
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: { color: textColor, font: { family: 'Plus Jakarta Sans', size: 11 } },
                        grid: { display: false }
                    },
                    y: {
                        ticks: { color: textColor, font: { family: 'Plus Jakarta Sans', size: 11 }, callback: (v) => `₹${v}` },
                        grid: { color: gridColor }
                    }
                }
            }
        });
    }

    // 2. Plan Distribution Chart
    const planCtx = document.getElementById('planChart');
    if (planCtx) {
        if (planChartInstance) planChartInstance.destroy();

        const planCounts = store.plans.map(p => {
            return store.customers.filter(c => c.planId === p.id).length;
        });

        planChartInstance = new Chart(planCtx, {
            type: 'doughnut',
            data: {
                labels: store.plans.map(p => p.name),
                datasets: [{
                    data: planCounts,
                    backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { color: textColor, boxWidth: 12, padding: 12, font: { family: 'Plus Jakarta Sans', size: 11 } }
                    }
                }
            }
        });
    }
}

// ==================== 1. SUBSCRIBERS CRUD ====================
function renderCustomersTable(list = store.customers) {
    const tbody = document.getElementById('customersTableBody');
    if (!tbody) return;

    if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="p-8 text-center text-slate-400">No subscribers found matching query.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(c => {
        const plan = store.plans.find(p => p.id === c.planId) || { name: 'Unknown Plan' };
        let statusBadge = '';
        if (c.status === 'ACTIVE') {
            statusBadge = `<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400">ACTIVE</span>`;
        } else if (c.status === 'SUSPENDED') {
            statusBadge = `<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400">SUSPENDED</span>`;
        } else {
            statusBadge = `<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-400">INACTIVE</span>`;
        }

        return `
            <tr class="hover:bg-slate-50/60 dark:hover:bg-slate-800/30 transition">
                <td class="p-4 font-mono font-bold text-slate-400 text-[11px]">#${c.id}</td>
                <td class="p-4">
                    <p class="font-bold text-slate-900 dark:text-white">${c.name}</p>
                    <p class="text-[10px] text-slate-400">Joined ${c.connectionDate}</p>
                </td>
                <td class="p-4 font-mono text-slate-700 dark:text-slate-300">${c.phone}</td>
                <td class="p-4 text-slate-600 dark:text-slate-400">${c.email || '—'}</td>
                <td class="p-4 text-slate-500 text-[11px] max-w-xs truncate" title="${c.address}">${c.address || '—'}</td>
                <td class="p-4">
                    <span class="px-2.5 py-1 rounded-lg text-[10px] font-semibold bg-blue-50 text-blue-700 dark:bg-blue-950/60 dark:text-blue-400 border border-blue-200 dark:border-blue-900/40">
                        ${plan.name}
                    </span>
                </td>
                <td class="p-4">${statusBadge}</td>
                <td class="p-4 text-right space-x-1 whitespace-nowrap">
                    <button onclick="openEditCustomerModal(${c.id})" class="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition" title="Edit Subscriber">
                        <i data-lucide="edit-3" class="w-4 h-4"></i>
                    </button>
                    <button onclick="deleteCustomer(${c.id})" class="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition" title="Delete Subscriber">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');

    lucide.createIcons();
}

function filterCustomers() {
    const query = (document.getElementById('customerSearch')?.value || '').toLowerCase();
    const status = document.getElementById('customerStatusFilter')?.value || 'ALL';

    const filtered = store.customers.filter(c => {
        const matchesQuery = c.name.toLowerCase().includes(query) ||
            c.phone.toLowerCase().includes(query) ||
            (c.email && c.email.toLowerCase().includes(query)) ||
            (c.address && c.address.toLowerCase().includes(query));
        const matchesStatus = status === 'ALL' || c.status === status;
        return matchesQuery && matchesStatus;
    });

    renderCustomersTable(filtered);
}

function openAddCustomerModal() {
    document.getElementById('custModalTitle').innerText = 'Register New Subscriber';
    document.getElementById('custEditId').value = '';
    document.getElementById('custName').value = '';
    document.getElementById('custPhone').value = '';
    document.getElementById('custEmail').value = '';
    document.getElementById('custAddress').value = '';
    document.getElementById('custStatus').value = 'ACTIVE';
    if (store.plans.length > 0) {
        document.getElementById('custPlan').value = store.plans[0].id;
    }
    openModal('customerModal');
}

function openEditCustomerModal(id) {
    const cust = store.customers.find(c => c.id === id);
    if (!cust) return;

    document.getElementById('custModalTitle').innerText = 'Edit Subscriber Details';
    document.getElementById('custEditId').value = cust.id;
    document.getElementById('custName').value = cust.name;
    document.getElementById('custPhone').value = cust.phone;
    document.getElementById('custEmail').value = cust.email || '';
    document.getElementById('custAddress').value = cust.address || '';
    document.getElementById('custPlan').value = cust.planId;
    document.getElementById('custStatus').value = cust.status;
    openModal('customerModal');
}

function handleCustomerSubmit(e) {
    e.preventDefault();
    const editId = document.getElementById('custEditId').value;
    const name = document.getElementById('custName').value.trim();
    const phone = document.getElementById('custPhone').value.trim();
    const email = document.getElementById('custEmail').value.trim();
    const address = document.getElementById('custAddress').value.trim();
    const planId = parseInt(document.getElementById('custPlan').value, 10);
    const status = document.getElementById('custStatus').value;

    if (editId) {
        // Update
        const idx = store.customers.findIndex(c => c.id === parseInt(editId, 10));
        if (idx !== -1) {
            store.customers[idx] = { ...store.customers[idx], name, phone, email, address, planId, status };
        }
    } else {
        // Create new
        const newId = store.customers.length > 0 ? Math.max(...store.customers.map(c => c.id)) + 1 : 1;
        const now = new Date().toISOString().split('T')[0];
        store.customers.push({
            id: newId,
            name,
            phone,
            email,
            address,
            connectionDate: now,
            planId,
            status
        });
    }

    store.save();
    closeModal('customerModal');
    populateSelectOptions();
    renderAll();
}

function deleteCustomer(id) {
    const cust = store.customers.find(c => c.id === id);
    if (!cust) return;
    if (confirm(`Are you sure you want to delete subscriber ${cust.name}? Any associated calls and bills will remain.`)) {
        store.customers = store.customers.filter(c => c.id !== id);
        store.save();
        populateSelectOptions();
        renderAll();
    }
}

// ==================== 2. CALL RECORDS (CDR) ====================
function renderCallsTable(list = store.calls) {
    const tbody = document.getElementById('callsTableBody');
    if (!tbody) return;

    if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="p-8 text-center text-slate-400">No call records found.</td></tr>`;
        return;
    }

    // Sort descending by timestamp
    const sorted = [...list].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    tbody.innerHTML = sorted.map(call => {
        const cust = store.customers.find(c => c.id === call.customerId) || { name: 'Unknown', phone: '—' };
        const mins = Math.floor(call.durationSeconds / 60);
        const secs = call.durationSeconds % 60;
        const durStr = `${mins}m ${secs.toString().padStart(2, '0')}s`;

        let typeBadge = '';
        if (call.callType === 'LOCAL') {
            typeBadge = `<span class="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-100 text-blue-700 dark:bg-blue-950/60 dark:text-blue-400">LOCAL</span>`;
        } else if (call.callType === 'STD') {
            typeBadge = `<span class="px-2 py-0.5 rounded text-[10px] font-bold bg-indigo-100 text-indigo-700 dark:bg-indigo-950/60 dark:text-indigo-400">STD</span>`;
        } else {
            typeBadge = `<span class="px-2 py-0.5 rounded text-[10px] font-bold bg-purple-100 text-purple-700 dark:bg-purple-950/60 dark:text-purple-400">ISD</span>`;
        }

        return `
            <tr class="hover:bg-slate-50/60 dark:hover:bg-slate-800/30 transition">
                <td class="p-4 font-mono font-bold text-slate-400 text-[11px]">#CDR-${call.id}</td>
                <td class="p-4 font-mono text-[11px] text-slate-600 dark:text-slate-400">${call.timestamp}</td>
                <td class="p-4">
                    <p class="font-bold text-slate-900 dark:text-white">${cust.name}</p>
                    <p class="text-[10px] text-slate-400 font-mono">${cust.phone}</p>
                </td>
                <td class="p-4 font-mono font-semibold text-slate-800 dark:text-slate-200">${call.destination}</td>
                <td class="p-4">${typeBadge}</td>
                <td class="p-4 font-medium text-slate-700 dark:text-slate-300">${durStr} (${call.durationSeconds}s)</td>
                <td class="p-4 font-bold text-emerald-600 dark:text-emerald-400">₹${call.cost.toFixed(2)}</td>
                <td class="p-4 text-right">
                    <button onclick="deleteCallRecord(${call.id})" class="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition" title="Delete Call Record">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');

    lucide.createIcons();
}

function filterCalls() {
    const custId = document.getElementById('callCustomerFilter')?.value || 'ALL';
    const type = document.getElementById('callTypeFilter')?.value || 'ALL';

    const filtered = store.calls.filter(c => {
        const matchesCust = custId === 'ALL' || c.customerId === parseInt(custId, 10);
        const matchesType = type === 'ALL' || c.callType === type;
        return matchesCust && matchesType;
    });

    renderCallsTable(filtered);
}

function openLogCallModal() {
    document.getElementById('logCallDest').value = '';
    document.getElementById('logCallDuration').value = '180';
    handleDestNumberChange();
    updateLiveCallCost();
    openModal('logCallModal');
}

function handleDestNumberChange() {
    const dest = document.getElementById('logCallDest').value;
    const type = detectCallType(dest);
    const badge = document.getElementById('logCallTypeBadge');
    if (badge) {
        badge.innerText = type;
        if (type === 'LOCAL') {
            badge.className = 'px-2.5 py-0.5 rounded text-[11px] font-bold bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-400';
        } else if (type === 'STD') {
            badge.className = 'px-2.5 py-0.5 rounded text-[11px] font-bold bg-indigo-100 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-400';
        } else {
            badge.className = 'px-2.5 py-0.5 rounded text-[11px] font-bold bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-400';
        }
    }
    updateLiveCallCost();
}

function updateLiveCallCost() {
    const custId = parseInt(document.getElementById('logCallCustomer').value, 10);
    const duration = parseInt(document.getElementById('logCallDuration').value, 10) || 0;
    const dest = document.getElementById('logCallDest').value;
    const type = detectCallType(dest);

    const cust = store.customers.find(c => c.id === custId);
    if (!cust) return;
    const plan = store.plans.find(p => p.id === cust.planId) || store.plans[0];

    const cost = BillingEngine.computeCallCost(type, duration, plan);
    document.getElementById('logCallCostPreview').innerText = `₹${cost.toFixed(2)}`;
}

function handleLogCallSubmit(e) {
    e.preventDefault();
    const custId = parseInt(document.getElementById('logCallCustomer').value, 10);
    const dest = document.getElementById('logCallDest').value.trim();
    const duration = parseInt(document.getElementById('logCallDuration').value, 10) || 0;
    const type = detectCallType(dest);

    const cust = store.customers.find(c => c.id === custId);
    if (!cust) return;
    const plan = store.plans.find(p => p.id === cust.planId) || store.plans[0];
    const cost = BillingEngine.computeCallCost(type, duration, plan);

    const now = new Date();
    const ts = now.toISOString().replace('T', ' ').substring(0, 19);

    const newId = store.calls.length > 0 ? Math.max(...store.calls.map(c => c.id)) + 1 : 1;
    store.calls.unshift({
        id: newId,
        customerId: custId,
        timestamp: ts,
        destination: dest,
        callType: type,
        durationSeconds: duration,
        cost: cost
    });

    store.save();
    closeModal('logCallModal');
    renderAll();
}

function deleteCallRecord(id) {
    if (confirm(`Delete CDR call record #${id}?`)) {
        store.calls = store.calls.filter(c => c.id !== id);
        store.save();
        renderAll();
    }
}

function handleCsvImport(e) {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(evt) {
        const text = evt.target.result;
        const lines = text.split(/\r\n|\n/).map(l => l.trim()).filter(l => l.length > 0);
        if (lines.length <= 1) {
            alert('CSV file appears empty.');
            return;
        }

        let importedCount = 0;
        let startId = store.calls.length > 0 ? Math.max(...store.calls.map(c => c.id)) + 1 : 1;

        // Skip header
        for (let i = 1; i < lines.length; i++) {
            const parts = lines[i].split(',').map(s => s.trim().replace(/^["']|["']$/g, ''));
            if (parts.length >= 3) {
                // Expected format: customerId, destination, durationSeconds, [optional timestamp]
                const custId = parseInt(parts[0], 10);
                const dest = parts[1];
                const dur = parseInt(parts[2], 10) || 60;
                const ts = parts[3] || new Date().toISOString().replace('T', ' ').substring(0, 19);

                const cust = store.customers.find(c => c.id === custId);
                const plan = cust ? (store.plans.find(p => p.id === cust.planId) || store.plans[0]) : store.plans[0];
                const type = detectCallType(dest);
                const cost = BillingEngine.computeCallCost(type, dur, plan);

                store.calls.push({
                    id: startId++,
                    customerId: custId,
                    timestamp: ts,
                    destination: dest,
                    callType: type,
                    durationSeconds: dur,
                    cost: cost
                });
                importedCount++;
            }
        }

        store.save();
        renderAll();
        alert(`Successfully imported ${importedCount} call detail records!`);
        e.target.value = '';
    };
    reader.readAsText(file);
}

// ==================== 3. BILLS & INVOICING ====================
function renderBillsTable(list = store.bills) {
    const tbody = document.getElementById('billsTableBody');
    if (!tbody) return;

    if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="10" class="p-8 text-center text-slate-400">No bills generated matching query.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(bill => {
        const cust = store.customers.find(c => c.id === bill.customerId) || { name: 'Unknown' };
        const cycleStr = `${bill.billingMonth.toString().padStart(2, '0')}/${bill.billingYear}`;

        let statusBadge = '';
        if (bill.paymentStatus === 'PAID') {
            statusBadge = `<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400">PAID</span>`;
        } else if (bill.paymentStatus === 'OVERDUE') {
            statusBadge = `<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-400">OVERDUE</span>`;
        } else {
            statusBadge = `<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400">UNPAID</span>`;
        }

        return `
            <tr class="hover:bg-slate-50/60 dark:hover:bg-slate-800/30 transition">
                <td class="p-4 font-mono font-bold text-blue-600 dark:text-blue-400">${bill.billNumber}</td>
                <td class="p-4 font-bold text-slate-900 dark:text-white">${cust.name}</td>
                <td class="p-4 font-medium text-slate-500">${cycleStr}</td>
                <td class="p-4 text-slate-700 dark:text-slate-300">₹${bill.rentalCharges.toFixed(2)}</td>
                <td class="p-4 text-slate-700 dark:text-slate-300">₹${bill.callCharges.toFixed(2)}</td>
                <td class="p-4 text-slate-500">₹${bill.taxAmount.toFixed(2)}</td>
                <td class="p-4 font-black text-slate-900 dark:text-white text-sm">₹${bill.totalAmount.toFixed(2)}</td>
                <td class="p-4 font-mono text-[11px] text-slate-500">${bill.dueDate}</td>
                <td class="p-4">${statusBadge}</td>
                <td class="p-4 text-right space-x-1 whitespace-nowrap">
                    <button onclick="previewInvoice(${bill.id})" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-blue-50 hover:bg-blue-100 text-blue-600 dark:bg-blue-950 dark:hover:bg-blue-900 dark:text-blue-300 transition" title="View Airtel/Jio Tax Invoice">
                        Invoice
                    </button>
                    ${bill.paymentStatus !== 'PAID' ? `
                    <button onclick="openPaymentModal(${bill.id})" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-50 hover:bg-emerald-100 text-emerald-600 dark:bg-emerald-950 dark:hover:bg-emerald-900 dark:text-emerald-300 transition" title="Record Payment">
                        Pay
                    </button>` : ''}
                    <button onclick="deleteBill(${bill.id})" class="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition" title="Delete Invoice">
                        <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');

    lucide.createIcons();
}

function filterBills() {
    const query = (document.getElementById('billSearch')?.value || '').toLowerCase();
    const status = document.getElementById('billStatusFilter')?.value || 'ALL';

    const filtered = store.bills.filter(b => {
        const cust = store.customers.find(c => c.id === b.customerId) || { name: '' };
        const matchesQuery = b.billNumber.toLowerCase().includes(query) || cust.name.toLowerCase().includes(query);
        const matchesStatus = status === 'ALL' || b.paymentStatus === status;
        return matchesQuery && matchesStatus;
    });

    renderBillsTable(filtered);
}

function openGenerateBillModal() {
    updateLiveBillCalculation();
    openModal('genBillModal');
}

function updateLiveBillCalculation() {
    const custId = parseInt(document.getElementById('genBillCustomer').value, 10);
    const month = parseInt(document.getElementById('genBillMonth').value, 10);
    const year = parseInt(document.getElementById('genBillYear').value, 10);
    const discount = parseFloat(document.getElementById('genBillDiscount').value) || 0;
    const lateFee = parseFloat(document.getElementById('genBillLateFee').value) || 0;

    const cust = store.customers.find(c => c.id === custId);
    if (!cust) return;
    const plan = store.plans.find(p => p.id === cust.planId) || store.plans[0];

    // Filter calls for this customer in month/year
    const relevantCalls = store.calls.filter(c => {
        if (c.customerId !== custId) return false;
        const d = new Date(c.timestamp);
        return (d.getMonth() + 1) === month && d.getFullYear() === year;
    });

    const b = BillingEngine.computeBillBreakdown(plan, relevantCalls, discount, lateFee);

    const summaryEl = document.getElementById('genBillPreviewSummary');
    if (summaryEl) {
        summaryEl.innerHTML = `
            Plan: <strong>${plan.name}</strong> (Rental: ₹${b.rentalCharges}) | CDR Calls: <strong>${relevantCalls.length}</strong> (${b.totalCallMinutes} mins)<br>
            Free Mins Applied: <strong>${b.freeMinutesUsed}</strong> | Call Charges: <strong>₹${b.totalCallCharges}</strong> | 18% GST: <strong>₹${b.taxAmount}</strong><br>
            <span class="text-blue-700 dark:text-blue-300 font-extrabold text-sm">Calculated Payable: ₹${b.totalPayable.toFixed(2)}</span>
        `;
    }
}

function handleGenerateBillSubmit(e) {
    e.preventDefault();
    const custId = parseInt(document.getElementById('genBillCustomer').value, 10);
    const month = parseInt(document.getElementById('genBillMonth').value, 10);
    const year = parseInt(document.getElementById('genBillYear').value, 10);
    const discount = parseFloat(document.getElementById('genBillDiscount').value) || 0;
    const lateFee = parseFloat(document.getElementById('genBillLateFee').value) || 0;

    const cust = store.customers.find(c => c.id === custId);
    if (!cust) return;
    const plan = store.plans.find(p => p.id === cust.planId) || store.plans[0];

    const relevantCalls = store.calls.filter(c => {
        if (c.customerId !== custId) return false;
        const d = new Date(c.timestamp);
        return (d.getMonth() + 1) === month && d.getFullYear() === year;
    });

    const b = BillingEngine.computeBillBreakdown(plan, relevantCalls, discount, lateFee);

    const billNum = `INV-${year}${month.toString().padStart(2, '0')}-${cust.id.toString().padStart(3, '0')}`;
    const genDate = new Date().toISOString().split('T')[0];
    const due = new Date();
    due.setDate(due.getDate() + 15);
    const dueDate = due.toISOString().split('T')[0];

    const newId = store.bills.length > 0 ? Math.max(...store.bills.map(x => x.id)) + 1 : 1;

    store.bills.unshift({
        id: newId,
        billNumber: billNum,
        customerId: cust.id,
        billingMonth: month,
        billingYear: year,
        callCharges: b.totalCallCharges,
        rentalCharges: b.rentalCharges,
        taxPercentage: 18.0,
        taxAmount: b.taxAmount,
        discountAmount: b.discountAmount,
        lateFee: b.lateFee,
        totalAmount: b.totalPayable,
        dueDate: dueDate,
        paymentStatus: 'UNPAID',
        generatedDate: genDate
    });

    store.save();
    closeModal('genBillModal');
    renderAll();
    switchTab('bills');
    previewInvoice(newId);
}

function deleteBill(id) {
    if (confirm(`Delete invoice #${id}?`)) {
        store.bills = store.bills.filter(b => b.id !== id);
        store.save();
        renderAll();
    }
}

function exportBillsCsv() {
    let csv = 'Invoice Number,Subscriber,Month,Year,Rental (INR),Calls (INR),GST (INR),Total (INR),Due Date,Status\n';
    store.bills.forEach(b => {
        const cust = store.customers.find(c => c.id === b.customerId) || { name: 'Unknown' };
        csv += `"${b.billNumber}","${cust.name}",${b.billingMonth},${b.billingYear},${b.rentalCharges},${b.callCharges},${b.taxAmount},${b.totalAmount},"${b.dueDate}","${b.paymentStatus}"\n`;
    });
    downloadBlob(csv, 'ApexTelecom_Invoices.csv', 'text/csv');
}

// ==================== 4. AUTHENTIC INVOICE PREVIEW & PDF ====================
let currentViewingBill = null;

function previewInvoice(billId) {
    const bill = store.bills.find(b => b.id === billId);
    if (!bill) return;
    currentViewingBill = bill;

    const cust = store.customers.find(c => c.id === bill.customerId) || { name: 'Unknown', phone: '—', address: '—', id: 0, planId: 1 };
    const plan = store.plans.find(p => p.id === cust.planId) || store.plans[0];

    // Populate sheet
    document.getElementById('invPaperNum').innerText = bill.billNumber;
    document.getElementById('invPaperDate').innerText = `Bill Date: ${bill.generatedDate}`;
    document.getElementById('invPaperDueDate').innerText = `Due Date: ${bill.dueDate}`;

    const stamp = document.getElementById('invPaperStatusStamp');
    stamp.innerText = bill.paymentStatus;
    if (bill.paymentStatus === 'PAID') {
        stamp.className = 'inline-block mt-2 px-3 py-0.5 text-xs font-black uppercase border-2 rounded border-emerald-500 text-emerald-600 bg-emerald-50';
    } else if (bill.paymentStatus === 'OVERDUE') {
        stamp.className = 'inline-block mt-2 px-3 py-0.5 text-xs font-black uppercase border-2 rounded border-rose-500 text-rose-600 bg-rose-50';
    } else {
        stamp.className = 'inline-block mt-2 px-3 py-0.5 text-xs font-black uppercase border-2 rounded border-amber-500 text-amber-600 bg-amber-50';
    }

    document.getElementById('invPaperCustomer').innerText = cust.name;
    document.getElementById('invPaperPhone').innerText = `Phone: ${cust.phone}`;
    document.getElementById('invPaperAddress').innerText = cust.address;
    document.getElementById('invPaperCustId').innerText = `Account ID: TELCO-${cust.id.toString().padStart(5, '0')}`;
    document.getElementById('invPaperPlan').innerText = plan.name;
    document.getElementById('invPaperCycle').innerText = `Billing Cycle: ${bill.billingMonth.toString().padStart(2, '0')}/${bill.billingYear}`;

    // Breakdown table
    document.getElementById('invPaperRental').innerText = `₹${bill.rentalCharges.toFixed(2)}`;
    document.getElementById('invPaperCalls').innerText = `₹${bill.callCharges.toFixed(2)}`;
    const subtotal = bill.rentalCharges + bill.callCharges;
    document.getElementById('invPaperSubtotal').innerText = `₹${subtotal.toFixed(2)}`;

    // CGST and SGST (half of 18% = 9% each)
    const halfTax = bill.taxAmount / 2;
    document.getElementById('invPaperCgst').innerText = `₹${halfTax.toFixed(2)}`;
    document.getElementById('invPaperSgst').innerText = `₹${halfTax.toFixed(2)}`;

    const lateFeeRow = document.getElementById('invPaperLateFeeRow');
    if (bill.lateFee > 0) {
        lateFeeRow.style.display = 'table-row';
        document.getElementById('invPaperLateFee').innerText = `₹${bill.lateFee.toFixed(2)}`;
    } else {
        lateFeeRow.style.display = 'none';
    }

    document.getElementById('invPaperTotal').innerText = `₹${bill.totalAmount.toFixed(2)}`;

    // Itemized CDR
    const relevantCalls = store.calls.filter(c => {
        if (c.customerId !== cust.id) return false;
        const d = new Date(c.timestamp);
        return (d.getMonth() + 1) === bill.billingMonth && d.getFullYear() === bill.billingYear;
    });

    const cdrBody = document.getElementById('invPaperCdrBody');
    if (relevantCalls.length === 0) {
        cdrBody.innerHTML = `<tr><td colspan="5" class="p-3 text-center text-slate-400 italic">No CDR calls logged in this billing period. Free allowance applies.</td></tr>`;
    } else {
        cdrBody.innerHTML = relevantCalls.map(c => `
            <tr>
                <td class="p-2 font-mono">${c.timestamp}</td>
                <td class="p-2 font-mono">${c.destination}</td>
                <td class="p-2 font-semibold">${c.callType}</td>
                <td class="p-2 text-center">${Math.floor(c.durationSeconds / 60)}m ${c.durationSeconds % 60}s</td>
                <td class="p-2 text-right font-medium">₹${c.cost.toFixed(2)}</td>
            </tr>
        `).join('');
    }

    openModal('invoiceModal');
}

function downloadInvoicePdf() {
    if (!currentViewingBill) return;
    const element = document.getElementById('invoicePaperSheet');
    const opt = {
        margin: 10,
        filename: `ApexTelecom_${currentViewingBill.billNumber}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };
    html2pdf().set(opt).from(element).save();
}

function printInvoice() {
    window.print();
}

// ==================== 5. PAYMENT RECORDING ====================
function openPaymentModal(billId) {
    const bill = store.bills.find(b => b.id === billId);
    if (!bill) return;

    const cust = store.customers.find(c => c.id === bill.customerId) || { name: 'Unknown' };

    document.getElementById('payBillId').value = bill.id;
    document.getElementById('payBillNum').innerText = `Invoice #${bill.billNumber}`;
    document.getElementById('payBillCustomer').innerText = `Subscriber: ${cust.name}`;
    document.getElementById('payBillAmount').innerText = `Payable Amount: ₹${bill.totalAmount.toFixed(2)}`;
    document.getElementById('payAmount').value = bill.totalAmount.toFixed(2);
    document.getElementById('payRef').value = `UPI/${new Date().getFullYear()}${(new Date().getMonth() + 1).toString().padStart(2, '0')}/${Math.floor(10000 + Math.random() * 90000)}`;

    openModal('paymentModal');
}

function handlePaymentSubmit(e) {
    e.preventDefault();
    const billId = parseInt(document.getElementById('payBillId').value, 10);
    const amount = parseFloat(document.getElementById('payAmount').value) || 0;
    const mode = document.getElementById('payMode').value;
    const ref = document.getElementById('payRef').value.trim();

    const bill = store.bills.find(b => b.id === billId);
    if (!bill) return;

    bill.paymentStatus = 'PAID';

    const payId = store.payments.length > 0 ? Math.max(...store.payments.map(p => p.id)) + 1 : 1;
    store.payments.push({
        id: payId,
        billId: billId,
        amount: amount,
        paymentDate: new Date().toISOString().split('T')[0],
        paymentMode: mode,
        reference: ref,
        notes: `Recorded via Web Portal (${mode})`
    });

    store.save();
    closeModal('paymentModal');
    renderAll();
    alert(`Payment of ₹${amount.toFixed(2)} recorded successfully! Invoice marked as PAID.`);
}

// ==================== 6. TARIFF PLANS ====================
function renderPlansGrid() {
    const container = document.getElementById('plansContainer');
    if (!container) return;

    container.innerHTML = store.plans.map(p => {
        const subsCount = store.customers.filter(c => c.planId === p.id).length;
        return `
            <div class="p-6 rounded-2xl bg-white dark:bg-darkCard border border-slate-200 dark:border-darkBorder shadow-sm space-y-4 relative flex flex-col justify-between">
                <div class="space-y-2">
                    <div class="flex items-center justify-between">
                        <span class="px-2.5 py-0.5 rounded text-[10px] font-bold bg-blue-50 text-blue-700 dark:bg-blue-950 dark:text-blue-400 uppercase tracking-wider">Plan #${p.id}</span>
                        <span class="text-xs text-slate-400 font-medium">${subsCount} Subscribers</span>
                    </div>
                    <h3 class="font-extrabold text-lg text-slate-900 dark:text-white">${p.name}</h3>
                    <p class="text-xs text-slate-500 leading-relaxed">${p.description}</p>
                </div>

                <div class="py-3 border-y border-slate-100 dark:border-darkBorder space-y-2 text-xs">
                    <div class="flex justify-between items-center">
                        <span class="text-slate-500">Monthly Rental:</span>
                        <span class="font-black text-slate-900 dark:text-white text-base">₹${p.monthlyRental}</span>
                    </div>
                    <div class="flex justify-between items-center">
                        <span class="text-slate-500">Free Minutes:</span>
                        <span class="font-bold text-emerald-600 dark:text-emerald-400">${p.freeMinutes} mins</span>
                    </div>
                    <div class="flex justify-between items-center">
                        <span class="text-slate-500">Local Pulse:</span>
                        <span class="font-semibold text-slate-700 dark:text-slate-300">₹${p.rateLocal.toFixed(2)} / min</span>
                    </div>
                    <div class="flex justify-between items-center">
                        <span class="text-slate-500">STD National:</span>
                        <span class="font-semibold text-slate-700 dark:text-slate-300">₹${p.rateStd.toFixed(2)} / min</span>
                    </div>
                    <div class="flex justify-between items-center">
                        <span class="text-slate-500">ISD International:</span>
                        <span class="font-semibold text-slate-700 dark:text-slate-300">₹${p.rateIsd.toFixed(2)} / min</span>
                    </div>
                </div>

                <button onclick="filterCustomersByPlan(${p.id})" class="w-full py-2 rounded-xl text-xs font-bold bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 transition">
                    View Enrolled Subscribers
                </button>
            </div>
        `;
    }).join('');
}

function filterCustomersByPlan(planId) {
    switchTab('customers');
    const filtered = store.customers.filter(c => c.planId === planId);
    renderCustomersTable(filtered);
}

function openAddPlanModal() {
    const planName = prompt('Enter New Tariff Plan Name (e.g. Ultra 5G Unlimited):');
    if (!planName) return;
    const rental = parseFloat(prompt('Monthly Rental (INR):', '399.00')) || 399.00;
    const freeMins = parseInt(prompt('Free Minutes Allowance:', '250'), 10) || 250;
    const localRate = parseFloat(prompt('Rate per min Local (INR):', '0.30')) || 0.30;
    const stdRate = parseFloat(prompt('Rate per min STD (INR):', '0.80')) || 0.80;
    const isdRate = parseFloat(prompt('Rate per min ISD (INR):', '5.50')) || 5.50;

    const newId = store.plans.length > 0 ? Math.max(...store.plans.map(p => p.id)) + 1 : 1;
    store.plans.push({
        id: newId,
        name: planName,
        rateLocal: localRate,
        rateStd: stdRate,
        rateIsd: isdRate,
        monthlyRental: rental,
        freeMinutes: freeMins,
        smsRate: 0.20,
        dataRate: 10.00,
        description: 'Custom created tariff tier.'
    });

    store.save();
    populateSelectOptions();
    renderAll();
    alert(`Tariff plan "${planName}" added!`);
}

// ==================== 7. REPORTS & ANALYTICS ====================
function renderReports() {
    const totalBilled = store.bills.reduce((sum, b) => sum + b.totalAmount, 0);
    const totalCollected = store.bills
        .filter(b => b.paymentStatus === 'PAID')
        .reduce((sum, b) => sum + b.totalAmount, 0);
    const outstanding = totalBilled - totalCollected;

    document.getElementById('reportTotalBilled').innerText = `₹${totalBilled.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById('reportTotalCollected').innerText = `₹${totalCollected.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById('reportOutstanding').innerText = `₹${outstanding.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

    // Top Callers Leaderboard
    const callerMap = {};
    store.customers.forEach(c => {
        callerMap[c.id] = {
            customer: c,
            callsCount: 0,
            totalSeconds: 0,
            totalSpend: 0
        };
    });

    store.calls.forEach(call => {
        if (callerMap[call.customerId]) {
            callerMap[call.customerId].callsCount++;
            callerMap[call.customerId].totalSeconds += call.durationSeconds;
            callerMap[call.customerId].totalSpend += call.cost;
        }
    });

    // Also include monthly rentals from bills in spend
    store.bills.forEach(b => {
        if (callerMap[b.customerId]) {
            callerMap[b.customerId].totalSpend += b.rentalCharges;
        }
    });

    const callers = Object.values(callerMap).sort((a, b) => b.totalSeconds - a.totalSeconds);

    const tbody = document.getElementById('topCallersTableBody');
    if (!tbody) return;

    tbody.innerHTML = callers.map((item, idx) => {
        const plan = store.plans.find(p => p.id === item.customer.planId) || { name: '—' };
        const mins = Math.round(item.totalSeconds / 60);

        let rankBadge = `<span class="font-bold text-slate-500">#${idx + 1}</span>`;
        if (idx === 0) rankBadge = `<span class="px-2 py-0.5 rounded text-xs font-black bg-amber-100 text-amber-800 dark:bg-amber-900/60 dark:text-amber-300">🥇 1st</span>`;
        else if (idx === 1) rankBadge = `<span class="px-2 py-0.5 rounded text-xs font-black bg-slate-200 text-slate-800 dark:bg-slate-700 dark:text-slate-300">🥈 2nd</span>`;
        else if (idx === 2) rankBadge = `<span class="px-2 py-0.5 rounded text-xs font-black bg-amber-700/20 text-amber-900 dark:text-amber-400">🥉 3rd</span>`;

        return `
            <tr class="hover:bg-slate-50/60 dark:hover:bg-slate-800/30 transition">
                <td class="p-3">${rankBadge}</td>
                <td class="p-3 font-bold text-slate-900 dark:text-white">${item.customer.name}</td>
                <td class="p-3 font-mono text-slate-600 dark:text-slate-400">${item.customer.phone}</td>
                <td class="p-3 text-slate-500">${plan.name}</td>
                <td class="p-3 font-semibold">${item.callsCount} calls</td>
                <td class="p-3 font-mono">${mins} mins (${item.totalSeconds}s)</td>
                <td class="p-3 text-right font-black text-emerald-600 dark:text-emerald-400">₹${item.totalSpend.toFixed(2)}</td>
            </tr>
        `;
    }).join('');
}

function exportTopCallersCsv() {
    let csv = 'Rank,Subscriber Name,Phone,Plan,Total Calls,Total Minutes,Total Spend (INR)\n';
    const callerMap = {};
    store.customers.forEach(c => {
        callerMap[c.id] = { customer: c, callsCount: 0, totalSeconds: 0, totalSpend: 0 };
    });
    store.calls.forEach(call => {
        if (callerMap[call.customerId]) {
            callerMap[call.customerId].callsCount++;
            callerMap[call.customerId].totalSeconds += call.durationSeconds;
            callerMap[call.customerId].totalSpend += call.cost;
        }
    });
    const callers = Object.values(callerMap).sort((a, b) => b.totalSeconds - a.totalSeconds);

    callers.forEach((item, idx) => {
        const plan = store.plans.find(p => p.id === item.customer.planId) || { name: '—' };
        const mins = Math.round(item.totalSeconds / 60);
        csv += `${idx + 1},"${item.customer.name}","${item.customer.phone}","${plan.name}",${item.callsCount},${mins},${item.totalSpend.toFixed(2)}\n`;
    });

    downloadBlob(csv, 'ApexTelecom_TopCallers.csv', 'text/csv');
}

// ==================== HELPERS ====================
function openModal(id) {
    const el = document.getElementById(id);
    if (el) {
        el.classList.remove('hidden');
        lucide.createIcons();
    }
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) {
        el.classList.add('hidden');
    }
}

function downloadBlob(content, filename, contentType) {
    const blob = new Blob([content], { type: contentType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}
