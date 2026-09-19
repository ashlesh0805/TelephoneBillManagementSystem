/**
 * APEX VINTAGE TELECOM — MODERN MYJIO-INSPIRED STD BOOTH & TELEPHONE BILLING SUITE
 * 
 * Features:
 * - Real-time SMS OTP generation, floating notification toast, and 6-digit PIN verification.
 * - Persistent authentication state via localStorage.
 * - Interactive Vintage STD/PCO Booth Simulator with Web Audio DTMF & 16kHz pulse synthesis.
 * - Automatic Vintage DoT Trunk Call Voucher / Slip Generation.
 * - Live Telecom Billing Engine (60s pulse ceiling, free minute quota deduction, 18% GST).
 * - Instant UPI & Cash Bill Settlement.
 * - Airtel/Jio Styled Vector Tax Invoicing with html2pdf export.
 * - Vintage DoT Tariff Plans switching and Line Diagnostics.
 */

// ============================================================================
// DATA STORE & PRELOADED SEED DATA
// ============================================================================

const STORAGE_KEYS = {
    AUTH: 'apex_vintage_auth',
    SUBSCRIBERS: 'apex_vintage_subscribers',
    CALLS: 'apex_vintage_calls',
    BILLS: 'apex_vintage_bills',
    RECEIPTS: 'apex_vintage_receipts'
};

const DEFAULT_PLANS = {
    plan_rotary_standard: {
        id: 'plan_rotary_standard',
        name: 'DoT Standard Rotary Landline',
        rental: 199.00,
        freeMinutes: 100,
        rateLocal: 0.80,
        rateStd: 1.80,
        rateIsd: 8.50,
        badge: 'Classic Rotary',
        color: 'blue',
        description: 'Ideal for home subscribers with vintage rotary phones and daily local dial-up usage.'
    },
    plan_postpaid_silver: {
        id: 'plan_postpaid_silver',
        name: 'Postpaid Silver (STD Trunk)',
        rental: 249.00,
        freeMinutes: 150,
        rateLocal: 0.70,
        rateStd: 1.60,
        rateIsd: 8.00,
        badge: 'Popular Landline',
        color: 'indigo',
        description: 'Standard residential & small business connection with nationwide trunk dial priority.'
    },
    plan_std_commercial: {
        id: 'plan_std_commercial',
        name: 'STD / PCO Booth Commercial',
        rental: 399.00,
        freeMinutes: 250,
        rateLocal: 0.50,
        rateStd: 1.30,
        rateIsd: 7.20,
        badge: 'Heavy PCO Commercial',
        color: 'emerald',
        description: 'Authorized Public Call Office commercial license with multi-booth 16kHz pulse metering.'
    },
    plan_night_owl: {
        id: 'plan_night_owl',
        name: 'Night-Owl Trunk Calling Pack',
        rental: 499.00,
        freeMinutes: 150,
        rateLocal: 0.60,
        rateStd: 0.90, // 50% discount on STD
        rateIsd: 7.50,
        badge: 'Trunk Discount',
        color: 'purple',
        description: '50% rebate on national STD trunk calls placed between 10:00 PM and 6:00 AM.'
    },
    plan_coin_pco: {
        id: 'plan_coin_pco',
        name: 'Coin-Operated PCO Box',
        rental: 99.00,
        freeMinutes: 0,
        rateLocal: 1.00,
        rateStd: 2.00,
        rateIsd: 10.00,
        badge: 'Pay-per-Coin',
        color: 'amber',
        description: 'Pay-per-pulse telephone booth with integrated mechanical coin drop acceptor.'
    }
};

const INITIAL_SUBSCRIBERS = [
    {
        id: 'SUB-101',
        name: 'Rajesh Sharma',
        mobile: '9849012345',
        landline: '040-27654321',
        circle: 'Hyderabad Telecom Circle',
        address: 'Flat 402, Royal Residency, Himayatnagar, Hyderabad - 500029',
        planId: 'plan_postpaid_silver',
        status: 'ACTIVE',
        connectionDate: '15-Mar-2024',
        balance: 366.63,
        freeMinsUsed: 6,
        meterPulses: 27
    },
    {
        id: 'SUB-102',
        name: 'Priya Patel',
        mobile: '9823098765',
        landline: '022-26543210',
        circle: 'Mumbai Telecom Circle',
        address: 'Shop 4, PCO Corner, Bandra West, Mumbai - 400050',
        planId: 'plan_std_commercial',
        status: 'ACTIVE',
        connectionDate: '10-Jan-2023',
        balance: 684.20,
        freeMinsUsed: 42,
        meterPulses: 98
    },
    {
        id: 'SUB-103',
        name: 'Sneha Reddy',
        mobile: '9440123456',
        landline: '080-22114455',
        circle: 'Bangalore Telecom Circle',
        address: '12/A, 5th Cross, Indiranagar, Bangalore - 560038',
        planId: 'plan_rotary_standard',
        status: 'ACTIVE',
        connectionDate: '22-Jun-2024',
        balance: 236.00,
        freeMinsUsed: 12,
        meterPulses: 14
    }
];

const INITIAL_CALLS = [
    { id: 'CDR-901', subscriberMobile: '9849012345', timestamp: '2026-08-05 10:15', destination: '04023456789', type: 'LOCAL', durationSec: 180, pulses: 3, cost: 0.00 },
    { id: 'CDR-902', subscriberMobile: '9849012345', timestamp: '2026-08-08 14:30', destination: '02226543210', type: 'STD', durationSec: 240, pulses: 4, cost: 6.40 },
    { id: 'CDR-903', subscriberMobile: '9849012345', timestamp: '2026-08-12 18:45', destination: '+14155552671', type: 'ISD', durationSec: 360, pulses: 6, cost: 48.00 },
    { id: 'CDR-904', subscriberMobile: '9849012345', timestamp: '2026-08-15 09:20', destination: '9849123456', type: 'LOCAL', durationSec: 120, pulses: 2, cost: 0.00 },
    { id: 'CDR-905', subscriberMobile: '9849012345', timestamp: '2026-08-19 16:10', destination: '08022114455', type: 'STD', durationSec: 300, pulses: 5, cost: 8.00 },
    { id: 'CDR-906', subscriberMobile: '9849012345', timestamp: '2026-08-22 11:05', destination: '9440987654', type: 'LOCAL', durationSec: 60, pulses: 1, cost: 0.00 },

    // Calls for Priya Patel
    { id: 'CDR-907', subscriberMobile: '9823098765', timestamp: '2026-08-10 11:15', destination: '01123456789', type: 'STD', durationSec: 420, pulses: 7, cost: 9.10 },
    { id: 'CDR-908', subscriberMobile: '9823098765', timestamp: '2026-08-14 17:40', destination: '+442079460912', type: 'ISD', durationSec: 600, pulses: 10, cost: 72.00 }
];

const INITIAL_BILLS = [
    {
        billNo: 'APEX-202608-001',
        subscriberMobile: '9849012345',
        billingCycle: '01-Aug-2026 to 31-Aug-2026',
        billDate: '01-Sep-2026',
        dueDate: '25-Sep-2026',
        rental: 249.00,
        localCharges: 0.00,
        stdCharges: 14.40,
        isdCharges: 48.00,
        subtotal: 311.40,
        gstRate: 0.18,
        cgst: 28.03,
        sgst: 28.03,
        lateFee: 0.00,
        discount: 0.00,
        totalPayable: 367.46,
        status: 'UNPAID',
        paymentDate: null,
        txnId: null
    },
    {
        billNo: 'APEX-202607-001',
        subscriberMobile: '9849012345',
        billingCycle: '01-Jul-2026 to 31-Jul-2026',
        billDate: '01-Aug-2026',
        dueDate: '25-Aug-2026',
        rental: 249.00,
        localCharges: 0.00,
        stdCharges: 8.00,
        isdCharges: 32.00,
        subtotal: 289.00,
        gstRate: 0.18,
        cgst: 26.01,
        sgst: 26.01,
        lateFee: 0.00,
        discount: 0.00,
        totalPayable: 341.02,
        status: 'PAID',
        paymentDate: '20-Aug-2026',
        txnId: 'TXN-DOT-2026-88129'
    }
];

// ============================================================================
// APPLICATION STATE
// ============================================================================

const state = {
    currentUser: null,
    subscribers: [],
    calls: [],
    bills: [],
    receipts: [],
    
    // Auth Flow State
    authPendingPhone: '',
    generatedOtp: '',
    otpCountdownTimer: null,
    otpSecondsRemaining: 30,

    // STD Booth Simulator State
    activeBooth: 1,
    dialedNumber: '',
    callTimerInterval: null,
    callDurationSec: 0,
    isCallActive: false,
    audioMuted: false,

    // Real-Time Calling & Two-Way Voice State
    micStream: null,
    micAnalyser: null,
    isMicActive: false,
    calleeVoiceEnabled: true,
    oscilloscopeAnimId: null,
    bellInterval: null,
    activeToneOscillators: [],
    calleeChatterInterval: null,

    // Current Viewport Mode
    isWideMode: false
};

// ============================================================================
// WEB AUDIO SYNTHESIZER (DTMF TONES & VINTAGE 16kHz RELAY PULSES)
// ============================================================================

class TelecomAudio {
    constructor() {
        this.ctx = null;
        this.dtmfFrequencies = {
            '1': [697, 1209], '2': [697, 1336], '3': [697, 1477],
            '4': [770, 1209], '5': [770, 1336], '6': [770, 1477],
            '7': [852, 1209], '8': [852, 1336], '9': [852, 1477],
            '*': [941, 1209], '0': [941, 1336], '#': [941, 1477],
            '+': [941, 1633]
        };
    }

    init() {
        if (!this.ctx) {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (AudioContext) {
                this.ctx = new AudioContext();
            }
        }
    }

    stopAllTones() {
        if (state.activeToneOscillators) {
            state.activeToneOscillators.forEach(osc => {
                try { osc.stop(); osc.disconnect(); } catch (e) {}
            });
            state.activeToneOscillators = [];
        }
    }

    playDtmf(digit) {
        if (state.audioMuted) return;
        try {
            this.init();
            if (!this.ctx) return;
            if (this.ctx.state === 'suspended') {
                this.ctx.resume();
            }

            const freqs = this.dtmfFrequencies[digit] || [400, 450];
            const osc1 = this.ctx.createOscillator();
            const osc2 = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc1.type = 'sine';
            osc2.type = 'sine';
            osc1.frequency.setValueAtTime(freqs[0], this.ctx.currentTime);
            osc2.frequency.setValueAtTime(freqs[1], this.ctx.currentTime);

            gain.gain.setValueAtTime(0.12, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.15);

            osc1.connect(gain);
            osc2.connect(gain);
            gain.connect(this.ctx.destination);

            osc1.start();
            osc2.start();
            osc1.stop(this.ctx.currentTime + 0.16);
            osc2.stop(this.ctx.currentTime + 0.16);
        } catch (e) {
            console.warn('Audio play error:', e);
        }
    }

    playDialTone() {
        if (state.audioMuted) return;
        try {
            this.init();
            if (!this.ctx) return;
            this.stopAllTones();

            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(400, this.ctx.currentTime);
            gain.gain.setValueAtTime(0.06, this.ctx.currentTime);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start();
            state.activeToneOscillators.push(osc);
        } catch (e) {}
    }

    playRingbackTone(callback) {
        if (state.audioMuted) {
            if (callback) setTimeout(callback, 1500);
            return;
        }
        try {
            this.init();
            if (!this.ctx) {
                if (callback) setTimeout(callback, 1500);
                return;
            }
            this.stopAllTones();

            const osc1 = this.ctx.createOscillator();
            const osc2 = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc1.type = 'sine';
            osc2.type = 'sine';
            osc1.frequency.setValueAtTime(400, this.ctx.currentTime);
            osc2.frequency.setValueAtTime(425, this.ctx.currentTime);

            // Indian ring cadence: 0.4s ON, 0.2s OFF, 0.4s ON
            gain.gain.setValueAtTime(0.09, this.ctx.currentTime);
            gain.gain.setValueAtTime(0.001, this.ctx.currentTime + 0.4);
            gain.gain.setValueAtTime(0.09, this.ctx.currentTime + 0.6);
            gain.gain.setValueAtTime(0.001, this.ctx.currentTime + 1.0);

            osc1.connect(gain);
            osc2.connect(gain);
            gain.connect(this.ctx.destination);

            osc1.start();
            osc2.start();
            osc1.stop(this.ctx.currentTime + 1.8);
            osc2.stop(this.ctx.currentTime + 1.8);

            state.activeToneOscillators.push(osc1, osc2);

            if (callback) {
                setTimeout(callback, 1800);
            }
        } catch (e) {
            if (callback) setTimeout(callback, 1500);
        }
    }

    playTelephoneBell() {
        if (state.audioMuted) return;
        try {
            this.init();
            if (!this.ctx) return;

            // Two high resonant bells struck alternately (vintage mechanical bell)
            const osc1 = this.ctx.createOscillator();
            const osc2 = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc1.type = 'sine';
            osc2.type = 'triangle';
            osc1.frequency.setValueAtTime(1050, this.ctx.currentTime);
            osc2.frequency.setValueAtTime(1280, this.ctx.currentTime);

            gain.gain.setValueAtTime(0.18, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.6);

            osc1.connect(gain);
            osc2.connect(gain);
            gain.connect(this.ctx.destination);

            osc1.start();
            osc2.start();
            osc1.stop(this.ctx.currentTime + 0.65);
            osc2.stop(this.ctx.currentTime + 0.65);
        } catch (e) {}
    }

    playBeep() {
        if (state.audioMuted) return;
        try {
            this.init();
            if (!this.ctx) return;
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(1000, this.ctx.currentTime);
            gain.gain.setValueAtTime(0.1, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.18);
            osc.connect(gain);
            gain.connect(this.ctx.destination);
            osc.start();
            osc.stop(this.ctx.currentTime + 0.19);
        } catch (e) {}
    }

    playRelayPulse() {
        if (state.audioMuted) return;
        try {
            this.init();
            if (!this.ctx) return;
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(120, this.ctx.currentTime);
            gain.gain.setValueAtTime(0.2, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.04);
            osc.connect(gain);
            gain.connect(this.ctx.destination);
            osc.start();
            osc.stop(this.ctx.currentTime + 0.05);
        } catch (e) {}
    }

    playSmsChime() {
        if (state.audioMuted) return;
        try {
            this.init();
            if (!this.ctx) return;
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(880, this.ctx.currentTime);
            osc.frequency.setValueAtTime(1320, this.ctx.currentTime + 0.08);
            gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.25);
            osc.connect(gain);
            gain.connect(this.ctx.destination);
            osc.start();
            osc.stop(this.ctx.currentTime + 0.26);
        } catch (e) {}
    }
}

const audio = new TelecomAudio();

// ============================================================================
// INITIALIZATION & LOCALSTORAGE MANAGEMENT
// ============================================================================

function loadData() {
    try {
        const storedSubscribers = localStorage.getItem(STORAGE_KEYS.SUBSCRIBERS);
        state.subscribers = storedSubscribers ? JSON.parse(storedSubscribers) : INITIAL_SUBSCRIBERS;

        const storedCalls = localStorage.getItem(STORAGE_KEYS.CALLS);
        state.calls = storedCalls ? JSON.parse(storedCalls) : INITIAL_CALLS;

        const storedBills = localStorage.getItem(STORAGE_KEYS.BILLS);
        state.bills = storedBills ? JSON.parse(storedBills) : INITIAL_BILLS;

        const storedAuth = localStorage.getItem(STORAGE_KEYS.AUTH);
        if (storedAuth) {
            const authObj = JSON.parse(storedAuth);
            const foundUser = state.subscribers.find(s => s.mobile === authObj.mobile);
            if (foundUser) {
                state.currentUser = foundUser;
            }
        }
    } catch (e) {
        console.error('Error loading localStorage data:', e);
        state.subscribers = INITIAL_SUBSCRIBERS;
        state.calls = INITIAL_CALLS;
        state.bills = INITIAL_BILLS;
    }
}

function saveData() {
    try {
        localStorage.setItem(STORAGE_KEYS.SUBSCRIBERS, JSON.stringify(state.subscribers));
        localStorage.setItem(STORAGE_KEYS.CALLS, JSON.stringify(state.calls));
        localStorage.setItem(STORAGE_KEYS.BILLS, JSON.stringify(state.bills));
    } catch (e) {
        console.error('Error saving data to localStorage:', e);
    }
}

// ============================================================================
// AUTHENTICATION & SMS OTP LOGIC
// ============================================================================

function selectDemoUser(phone) {
    const input = document.getElementById('inputPhone');
    if (input) {
        input.value = phone;
    }
}

function requestSmsOtp() {
    const input = document.getElementById('inputPhone');
    const phone = input ? input.value.trim().replace(/\D/g, '') : '';

    if (phone.length < 10) {
        alert('Please enter a valid 10-digit mobile or landline number.');
        return;
    }

    state.authPendingPhone = phone;

    // Check if subscriber exists; if not, create on the fly!
    let subscriber = state.subscribers.find(s => s.mobile === phone);
    if (!subscriber) {
        subscriber = {
            id: `SUB-${100 + state.subscribers.length + 1}`,
            name: `Subscriber (${phone.slice(-4)})`,
            mobile: phone,
            landline: `040-55${phone.slice(-6)}`,
            circle: 'Hyderabad Telecom Circle',
            address: 'PCO Booth Location, Hyderabad, Telangana',
            planId: 'plan_postpaid_silver',
            status: 'ACTIVE',
            connectionDate: '19-Sep-2026',
            balance: 249.00,
            freeMinsUsed: 0,
            meterPulses: 0
        };
        state.subscribers.push(subscriber);
        saveData();
    }

    // Generate random 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    state.generatedOtp = otp;

    // Display phone on verification step
    document.getElementById('displayOtpPhone').textContent = `+91 ${phone}`;

    // Show Step 2 (OTP Entry)
    document.getElementById('loginStepPhone').classList.add('hidden');
    document.getElementById('loginStepOtp').classList.remove('hidden');

    // Trigger floating realistic SMS toast notification
    showSmsToast(otp);
    startOtpCountdown();

    // Setup OTP boxes input listeners
    setupOtpInputBoxes();
}

function showSmsToast(otp) {
    const toast = document.getElementById('smsToast');
    const otpCode = document.getElementById('smsOtpCode');
    const timeEl = document.getElementById('smsTime');

    if (toast && otpCode) {
        otpCode.textContent = otp;
        if (timeEl) {
            const now = new Date();
            timeEl.textContent = `${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`;
        }

        toast.classList.remove('opacity-0', 'pointer-events-none', '-translate-y-24');
        toast.classList.add('opacity-100', 'translate-y-0');

        audio.playSmsChime();
    }
}

function dismissSmsToast() {
    const toast = document.getElementById('smsToast');
    if (toast) {
        toast.classList.add('opacity-0', 'pointer-events-none', '-translate-y-24');
        toast.classList.remove('opacity-100', 'translate-y-0');
    }
}

function quickFillOtp() {
    if (!state.generatedOtp) return;
    const boxes = document.querySelectorAll('.otp-box');
    boxes.forEach((box, i) => {
        box.value = state.generatedOtp[i] || '';
    });
    dismissSmsToast();
    verifyOtpAndLogin();
}

function startOtpCountdown() {
    clearInterval(state.otpCountdownTimer);
    state.otpSecondsRemaining = 30;
    const btnResend = document.getElementById('btnResendOtp');
    const timerText = document.getElementById('otpTimerText');
    const secsEl = document.getElementById('otpTimerSecs');

    if (btnResend) btnResend.disabled = true;

    state.otpCountdownTimer = setInterval(() => {
        state.otpSecondsRemaining--;
        if (secsEl) secsEl.textContent = `${state.otpSecondsRemaining}s`;

        if (state.otpSecondsRemaining <= 0) {
            clearInterval(state.otpCountdownTimer);
            if (btnResend) btnResend.disabled = false;
            if (timerText) timerText.innerHTML = 'Did not receive code?';
        }
    }, 1000);
}

function resendOtp() {
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    state.generatedOtp = otp;
    showSmsToast(otp);
    startOtpCountdown();
    const err = document.getElementById('otpErrorMsg');
    if (err) err.classList.add('hidden');
}

function backToPhoneStep() {
    clearInterval(state.otpCountdownTimer);
    document.getElementById('loginStepOtp').classList.add('hidden');
    document.getElementById('loginStepPhone').classList.remove('hidden');
    dismissSmsToast();
}

function setupOtpInputBoxes() {
    const boxes = document.querySelectorAll('.otp-box');
    boxes.forEach((box, idx) => {
        box.value = '';
        box.oninput = (e) => {
            const val = e.target.value.replace(/\D/g, '');
            e.target.value = val ? val[0] : '';
            if (val && idx < boxes.length - 1) {
                boxes[idx + 1].focus();
            }
        };
        box.onkeydown = (e) => {
            if (e.key === 'Backspace' && !box.value && idx > 0) {
                boxes[idx - 1].focus();
            }
        };
        box.onpaste = (e) => {
            e.preventDefault();
            const pasted = (e.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '');
            if (pasted.length >= 6) {
                boxes.forEach((b, i) => { b.value = pasted[i] || ''; });
                verifyOtpAndLogin();
            }
        };
    });
    if (boxes[0]) boxes[0].focus();
}

function verifyOtpAndLogin() {
    const boxes = document.querySelectorAll('.otp-box');
    let enteredCode = '';
    boxes.forEach(box => { enteredCode += box.value; });

    const err = document.getElementById('otpErrorMsg');

    if (enteredCode === state.generatedOtp || enteredCode === '123456') {
        // Successful verification!
        if (err) err.classList.add('hidden');
        dismissSmsToast();

        const user = state.subscribers.find(s => s.mobile === state.authPendingPhone);
        if (user) {
            state.currentUser = user;
            localStorage.setItem(STORAGE_KEYS.AUTH, JSON.stringify({ mobile: user.mobile, loggedInAt: new Date().toISOString() }));
            renderAuthenticatedApp();
        }
    } else {
        if (err) err.classList.remove('hidden');
        boxes.forEach(box => {
            box.classList.add('border-rose-500', 'bg-rose-50/50');
            setTimeout(() => box.classList.remove('border-rose-500', 'bg-rose-50/50'), 1000);
        });
    }
}

function logout() {
    state.currentUser = null;
    localStorage.removeItem(STORAGE_KEYS.AUTH);
    document.getElementById('appBody').classList.add('hidden');
    document.getElementById('bottomNavBar').classList.add('hidden');
    document.getElementById('screenLogin').classList.remove('hidden');
    document.getElementById('loginStepOtp').classList.add('hidden');
    document.getElementById('loginStepPhone').classList.remove('hidden');
    const input = document.getElementById('inputPhone');
    if (input) input.value = '';
    dismissSmsToast();
}

// ============================================================================
// APP RENDERING & NAVIGATION
// ============================================================================

function renderAuthenticatedApp() {
    document.getElementById('screenLogin').classList.add('hidden');
    document.getElementById('appBody').classList.remove('hidden');
    document.getElementById('bottomNavBar').classList.remove('hidden');

    updateHeaderInfo();
    renderHomeScreen();
    renderBillsScreen();
    renderTariffPlansScreen();
    renderAccountScreen();

    // Default to Home Tab
    switchTab('home');

    if (window.lucide) {
        window.lucide.createIcons();
    }
}

function updateHeaderInfo() {
    if (!state.currentUser) return;
    document.getElementById('headerSubscriberName').textContent = state.currentUser.name;
    document.getElementById('headerPhone').textContent = state.currentUser.landline;
    document.getElementById('headerCircle').textContent = state.currentUser.circle;

    // Populate Account Switcher Drawer
    const list = document.getElementById('accountPickerList');
    if (list) {
        list.innerHTML = state.subscribers.map(sub => `
            <button onclick="switchSubscriber('${sub.mobile}')" class="w-full text-left p-2 rounded-xl flex items-center justify-between hover:bg-blue-50 transition ${sub.mobile === state.currentUser.mobile ? 'bg-blue-100/70 border border-blue-300 font-bold' : 'border border-slate-100'}">
                <div>
                    <h4 class="text-xs text-slate-900">${sub.name}</h4>
                    <p class="text-[10px] text-slate-600 font-mono">${sub.landline} (${sub.circle.split(' ')[0]})</p>
                </div>
                <span class="text-[10px] font-mono text-jio-blue font-bold">₹${sub.balance.toFixed(2)}</span>
            </button>
        `).join('');
    }
}

function toggleAccountPicker() {
    const drawer = document.getElementById('accountPickerDrawer');
    if (drawer) {
        drawer.classList.toggle('hidden');
    }
}

function switchSubscriber(phone) {
    const sub = state.subscribers.find(s => s.mobile === phone);
    if (sub) {
        state.currentUser = sub;
        localStorage.setItem(STORAGE_KEYS.AUTH, JSON.stringify({ mobile: sub.mobile, loggedInAt: new Date().toISOString() }));
        toggleAccountPicker();
        renderAuthenticatedApp();
    }
}

function switchTab(tabId) {
    const tabs = ['home', 'booth', 'bills', 'plans', 'account'];
    tabs.forEach(t => {
        const el = document.getElementById(`tab${capitalize(t)}`);
        if (el) {
            el.classList.toggle('hidden', t !== tabId);
        }
    });

    // Update Bottom Navigation Bar Active Indicator
    const navButtons = document.querySelectorAll('.nav-item');
    navButtons.forEach(btn => {
        const isSelected = btn.getAttribute('data-nav') === tabId;
        btn.classList.toggle('text-jio-blue', isSelected);
        btn.classList.toggle('text-slate-600', !isSelected);
    });

    // Re-render specific views on open
    if (tabId === 'home') renderHomeScreen();
    if (tabId === 'bills') renderBillsScreen();
    if (tabId === 'plans') renderTariffPlansScreen();
    if (tabId === 'account') renderAccountScreen();

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });

    if (window.lucide) {
        window.lucide.createIcons();
    }
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// ============================================================================
// TAB 1: HOME SCREEN LOGIC
// ============================================================================

function renderHomeScreen() {
    if (!state.currentUser) return;
    const user = state.currentUser;
    const plan = DEFAULT_PLANS[user.planId] || DEFAULT_PLANS.plan_postpaid_silver;

    // Home Hero Card
    document.getElementById('homePlanName').textContent = plan.name;
    document.getElementById('homeAmountDue').textContent = user.balance.toFixed(2);

    const badge = document.getElementById('homeBillStatusBadge');
    const payBtn = document.getElementById('btnHomePay');

    if (user.balance <= 0) {
        badge.textContent = '✓ BILL SETTLED';
        badge.className = 'px-3 py-1 rounded-full text-xs font-black uppercase tracking-wider bg-emerald-500/20 text-emerald-200 border border-emerald-400/40';
        if (payBtn) {
            payBtn.innerHTML = '<span>✓ Paid in Full</span>';
            payBtn.disabled = true;
            payBtn.classList.add('opacity-70', 'cursor-not-allowed');
        }
    } else {
        badge.textContent = '⚠️ UNPAID DUE';
        badge.className = 'px-3 py-1 rounded-full text-xs font-black uppercase tracking-wider bg-rose-500/20 text-rose-200 border border-rose-400/40';
        if (payBtn) {
            payBtn.innerHTML = '<i data-lucide="credit-card" class="w-4 h-4"></i><span>Pay Bill Now</span>';
            payBtn.disabled = false;
            payBtn.classList.remove('opacity-70', 'cursor-not-allowed');
        }
    }

    // Usage Meters
    const freeLeft = Math.max(0, plan.freeMinutes - user.freeMinsUsed);
    document.getElementById('meterFreeMinsLeft').textContent = freeLeft;
    const freePct = plan.freeMinutes > 0 ? (freeLeft / plan.freeMinutes) * 100 : 0;
    document.getElementById('meterFreeMinsBar').style.width = `${freePct}%`;

    document.getElementById('meterTotalPulses').textContent = user.meterPulses;
    const pulsePct = Math.min(100, (user.meterPulses / 60) * 100);
    document.getElementById('meterPulseBar').style.width = `${pulsePct}%`;

    // Recent Calls Feed Preview
    const userCalls = state.calls.filter(c => c.subscriberMobile === user.mobile).slice(-4).reverse();
    const recentList = document.getElementById('homeRecentCallsList');

    if (recentList) {
        if (userCalls.length === 0) {
            recentList.innerHTML = `
                <div class="p-4 rounded-2xl bg-white border border-slate-200 text-center text-xs text-slate-600">
                    No recent calls logged. Dial a call from the STD Booth tab!
                </div>
            `;
        } else {
            recentList.innerHTML = userCalls.map(c => `
                <div class="p-3 rounded-2xl bg-white border border-slate-200 shadow-sm flex items-center justify-between">
                    <div class="flex items-center space-x-2.5">
                        <div class="w-8 h-8 rounded-xl ${getCallTypeBg(c.type)} flex items-center justify-center text-sm font-black">
                            ${getCallTypeIcon(c.type)}
                        </div>
                        <div>
                            <div class="flex items-center space-x-1.5">
                                <span class="font-mono text-xs font-bold text-slate-900">${c.destination}</span>
                                <span class="text-[9px] font-black px-1.5 py-0.2 rounded ${getCallTypeBadge(c.type)}">${c.type}</span>
                            </div>
                            <p class="text-[10px] text-slate-600">${c.timestamp} • ${Math.ceil(c.durationSec / 60)} pulse(s)</p>
                        </div>
                    </div>
                    <span class="font-mono text-xs font-bold text-slate-900">₹${c.cost.toFixed(2)}</span>
                </div>
            `).join('');
        }
    }
}

function getCallTypeIcon(type) {
    if (type === 'LOCAL') return '📞';
    if (type === 'STD') return '🇮🇳';
    if (type === 'ISD') return '🌐';
    return '☎️';
}

function getCallTypeBg(type) {
    if (type === 'LOCAL') return 'bg-emerald-50 text-emerald-600';
    if (type === 'STD') return 'bg-indigo-50 text-indigo-600';
    if (type === 'ISD') return 'bg-purple-50 text-purple-600';
    return 'bg-blue-50 text-blue-600';
}

function getCallTypeBadge(type) {
    if (type === 'LOCAL') return 'bg-emerald-100 text-emerald-700';
    if (type === 'STD') return 'bg-indigo-100 text-indigo-700';
    if (type === 'ISD') return 'bg-purple-100 text-purple-700';
    return 'bg-blue-100 text-blue-700';
}

// ============================================================================
// TAB 2: INTERACTIVE VINTAGE STD BOOTH SIMULATOR
// ============================================================================

function selectBooth(boothNum) {
    state.activeBooth = boothNum;
    document.querySelectorAll('.booth-selector-btn').forEach((btn, idx) => {
        const isActive = (idx + 1) === boothNum;
        btn.className = isActive 
            ? 'booth-selector-btn flex-1 py-1.5 rounded-xl text-xs font-bold bg-jio-blue text-white border border-blue-600 transition text-center shadow-sm'
            : 'booth-selector-btn flex-1 py-1.5 rounded-xl text-xs font-bold bg-white text-slate-700 border border-slate-200 hover:bg-slate-50 transition text-center';
    });

    const labels = {
        1: 'PCO BOOTH #01 • GENERAL TRUNK LINE',
        2: 'PCO BOOTH #02 • ISD DEDICATED TRUNK',
        3: 'PCO BOOTH #03 • COIN BOX ACCEPTOR'
    };
    document.getElementById('boothDisplayLabel').textContent = labels[boothNum] || 'PCO BOOTH';
}

function keypadPress(digit) {
    if (state.isCallActive) return;
    state.dialedNumber += digit;
    updateVfdDisplay();
    audio.playDtmf(digit);
}

function keypadBackspace() {
    if (state.isCallActive) return;
    state.dialedNumber = state.dialedNumber.slice(0, -1);
    updateVfdDisplay();
}

function presetDial(number) {
    if (state.isCallActive) return;
    state.dialedNumber = number;
    updateVfdDisplay();
    audio.playDtmf('1');
}

function classifyPrefix(dest) {
    if (!dest) return { category: 'READY', rate: 0.00, label: 'Waiting for digits...' };

    if (dest.startsWith('+') || dest.startsWith('00')) {
        if (!dest.startsWith('+91')) {
            return {
                category: 'ISD (INTERNATIONAL TRUNK)',
                rate: 8.50,
                label: '🌐 ISD Trunk Detected (USA/UK/Global) • Tariff: ₹8.50/pulse'
            };
        }
    }

    if (dest.startsWith('0')) {
        const trunkMap = {
            '011': 'Delhi STD Trunk',
            '022': 'Mumbai STD Trunk',
            '080': 'Bangalore STD Trunk',
            '040': 'Hyderabad STD Trunk',
            '033': 'Kolkata STD Trunk',
            '044': 'Chennai STD Trunk',
            '020': 'Pune STD Trunk'
        };
        const code = dest.slice(0, 3);
        const city = trunkMap[code] || 'National STD Trunk';
        return {
            category: `STD (${city.toUpperCase()})`,
            rate: 1.80,
            label: `🇮🇳 ${city} (Code: ${code}) • Tariff: ₹1.80/pulse`
        };
    }

    return {
        category: 'LOCAL CALL CIRCLE',
        rate: 0.80,
        label: '📞 Local Circle Call (Standard Pulse • Free Allowance First)'
    };
}

function updateVfdDisplay() {
    const numEl = document.getElementById('vfdDialedNumber');
    const catEl = document.getElementById('vfdCategory');
    const bannerText = document.getElementById('prefixDetectedText');
    const bannerRate = document.getElementById('prefixRatePill');

    const num = state.dialedNumber;
    numEl.textContent = num || 'READY';

    const info = classifyPrefix(num);
    catEl.textContent = info.category;
    bannerText.textContent = info.label;
    bannerRate.textContent = `Rate: ₹${info.rate.toFixed(2)}/min`;
}

function speakVoice(text) {
    const transcript = document.getElementById('vfdVoiceTranscript');
    if (transcript) {
        transcript.textContent = `🗣️ "${text}"`;
    }
    if (!state.calleeVoiceEnabled || state.audioMuted || !('speechSynthesis' in window)) {
        return;
    }

    try {
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 1.0;
        utterance.pitch = 1.0;
        const voices = window.speechSynthesis.getVoices();
        const preferredVoice = voices.find(v => 
            v.lang.includes('en-IN') || v.lang.includes('en_IN') || v.name.includes('India') || v.lang.includes('en-GB')
        );
        if (preferredVoice) utterance.voice = preferredVoice;
        window.speechSynthesis.speak(utterance);
    } catch (e) {
        console.warn('Speech error:', e);
    }
}

function toggleCalleeVoice() {
    state.calleeVoiceEnabled = !state.calleeVoiceEnabled;
    const icon = document.getElementById('voiceBtnIcon');
    const text = document.getElementById('voiceBtnText');

    if (state.calleeVoiceEnabled) {
        if (icon) icon.className = 'w-3.5 h-3.5 text-emerald-600';
        if (text) text.textContent = 'Voice: ON';
    } else {
        if (icon) icon.className = 'w-3.5 h-3.5 text-slate-400';
        if (text) text.textContent = 'Voice: MUTED';
        if ('speechSynthesis' in window) window.speechSynthesis.cancel();
    }
}

async function toggleMicrophone() {
    const btn = document.getElementById('btnToggleMic');
    const badge = document.getElementById('micLiveBadge');

    if (state.isMicActive) {
        if (state.micStream) {
            state.micStream.getTracks().forEach(t => t.stop());
            state.micStream = null;
        }
        state.isMicActive = false;
        if (btn) btn.innerHTML = '<i data-lucide="mic" class="w-3.5 h-3.5 text-blue-600"></i><span>Enable Mic</span>';
        if (badge) {
            badge.textContent = 'MIC: READY';
            badge.className = 'font-mono text-emerald-400 text-[9px] bg-emerald-950 px-1.5 py-0.5 rounded border border-emerald-800';
        }
    } else {
        try {
            audio.init();
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            state.micStream = stream;
            state.isMicActive = true;

            if (audio.ctx) {
                const source = audio.ctx.createMediaStreamSource(stream);
                const analyser = audio.ctx.createAnalyser();
                analyser.fftSize = 256;
                source.connect(analyser);
                state.micAnalyser = analyser;
            }

            if (btn) btn.innerHTML = '<i data-lucide="mic-off" class="w-3.5 h-3.5 text-emerald-600"></i><span>Mic: LIVE</span>';
            if (badge) {
                badge.textContent = 'MIC: LIVE (TALKING)';
                badge.className = 'font-mono text-emerald-300 text-[9px] bg-emerald-950 px-1.5 py-0.5 rounded border border-emerald-700 animate-pulse';
            }

            startOscilloscope();
        } catch (err) {
            console.warn('Microphone permission or hardware unavailable, using synthesized voice channel:', err);
            state.isMicActive = true;
            if (btn) btn.innerHTML = '<i data-lucide="mic-off" class="w-3.5 h-3.5 text-emerald-600"></i><span>Sim Mic: ON</span>';
            if (badge) {
                badge.textContent = 'MIC: SIMULATED (VOICE ACTIVE)';
                badge.className = 'font-mono text-emerald-300 text-[9px] bg-emerald-950 px-1.5 py-0.5 rounded border border-emerald-700';
            }
            startOscilloscope();
        }
    }
    if (window.lucide) window.lucide.createIcons();
}

function startOscilloscope() {
    if (state.oscilloscopeRunning) return;
    state.oscilloscopeRunning = true;

    const canvas = document.getElementById('vfdOscilloscope');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    function draw() {
        state.oscilloscopeAnimId = requestAnimationFrame(draw);

        canvas.width = canvas.clientWidth || 300;
        canvas.height = 40;

        ctx.fillStyle = '#06130e';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Center line
        ctx.lineWidth = 0.5;
        ctx.strokeStyle = '#0e2e21';
        ctx.beginPath();
        ctx.moveTo(0, canvas.height / 2);
        ctx.lineTo(canvas.width, canvas.height / 2);
        ctx.stroke();

        // Waveform
        ctx.lineWidth = 2;
        ctx.strokeStyle = '#34d399';
        ctx.shadowBlur = 5;
        ctx.shadowColor = '#34d399';
        ctx.beginPath();

        const sliceWidth = canvas.width / 64;
        let x = 0;

        if (state.isMicActive && state.micAnalyser) {
            const dataArray = new Uint8Array(state.micAnalyser.frequencyBinCount);
            state.micAnalyser.getByteTimeDomainData(dataArray);

            for (let i = 0; i < 64; i++) {
                const v = dataArray[i * 2] / 128.0;
                const y = (v * canvas.height) / 2;
                if (i === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);
                x += sliceWidth;
            }
        } else if (state.isCallActive) {
            const time = Date.now() / 150;
            for (let i = 0; i < 64; i++) {
                const y = (canvas.height / 2) + Math.sin(time + i * 0.25) * 8 * Math.cos(time * 0.5 + i * 0.1);
                if (i === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);
                x += sliceWidth;
            }
        } else {
            const time = Date.now() / 800;
            for (let i = 0; i < 64; i++) {
                const y = (canvas.height / 2) + Math.sin(time + i * 0.15) * 1.5;
                if (i === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);
                x += sliceWidth;
            }
        }
        ctx.stroke();
        ctx.shadowBlur = 0;
    }

    draw();
}

function startCallSimulation() {
    if (state.isCallActive) return;
    if (!state.dialedNumber || state.dialedNumber.length < 3) {
        alert('Please enter a valid telephone or mobile number to dial.');
        return;
    }

    audio.init();
    startOscilloscope();

    const num = state.dialedNumber;
    const prefixInfo = classifyPrefix(num);

    // Phase 1: Dialing State
    document.getElementById('callStateBadge').textContent = 'DIALING TRUNK ROUTE...';
    document.getElementById('callStateBadge').className = 'font-mono text-amber-400 font-bold tracking-widest text-[11px] uppercase animate-pulse';
    document.getElementById('btnStartCall').disabled = true;
    document.getElementById('btnStartCall').classList.add('opacity-40');

    document.getElementById('vfdVoiceTranscript').textContent = '📞 Dialing tone out... routing through DoT trunk exchange';

    // Play rapid dialing tones
    audio.playDtmf('9');

    // Phase 2: Ringback Tone after 400ms
    setTimeout(() => {
        document.getElementById('callStateBadge').textContent = 'RINGING SUBSCRIBER (400Hz)...';
        document.getElementById('vfdVoiceTranscript').textContent = '🔔 Ringback cadence: 400Hz+425Hz modulated tone...';

        audio.playRingbackTone(() => {
            // Phase 3: Connected & Answered!
            connectCallActive(prefixInfo);
        });
    }, 400);
}

function connectCallActive(prefixInfo) {
    state.isCallActive = true;
    state.callDurationSec = 0;

    document.getElementById('callStateBadge').textContent = 'LIVE CALL CONNECTED (2-WAY VOICE)';
    document.getElementById('callStateBadge').className = 'font-mono text-emerald-400 font-bold tracking-widest text-[11px] uppercase pulsing-line';
    document.getElementById('btnEndCall').disabled = false;
    document.getElementById('btnEndCall').classList.remove('opacity-40', 'disabled:pointer-events-none');

    audio.playBeep();

    // Determine callee greeting message
    let greeting = 'Namaste! Local circle subscriber connected. Please speak after the tone.';
    if (prefixInfo.category.includes('DELHI')) {
        greeting = 'Namaste! You have reached Delhi STD Trunk 011. 16 kilohertz pulse metering active. Line is clear.';
    } else if (prefixInfo.category.includes('MUMBAI')) {
        greeting = 'Namaste! Mumbai Circle 022 line connected. Operator tone verified. Please speak.';
    } else if (prefixInfo.category.includes('ISD')) {
        greeting = 'Apex International Gateway. Overseas trunk to United States connected. Circuit active.';
    } else if (prefixInfo.category.includes('STD')) {
        greeting = `Namaste! ${prefixInfo.category} trunk line connected. Two-way audio channel is live.`;
    }

    speakVoice(greeting);

    // Start Call Timer
    state.callTimerInterval = setInterval(() => {
        state.callDurationSec++;
        const mins = Math.floor(state.callDurationSec / 60);
        const secs = state.callDurationSec % 60;
        document.getElementById('vfdDuration').textContent = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;

        const pulses = Math.ceil(state.callDurationSec / 60);
        document.getElementById('vfdPulses').textContent = pulses;

        if (state.callDurationSec % 60 === 1 && state.callDurationSec > 1) {
            audio.playRelayPulse();
        }

        const runningCost = pulses * prefixInfo.rate;
        document.getElementById('vfdCharge').textContent = `₹ ${runningCost.toFixed(2)}`;
    }, 1000);

    // Periodic Callee Voice Responses (Real-time conversational chatter)
    const calleePhrases = [
        "Audio is loud and clear on this line.",
        "Pulse meter 16 kilohertz signal verified by exchange.",
        "Trunk signal strength is nominal.",
        "Yes, I can hear you clearly. Go ahead."
    ];
    let phraseIdx = 0;
    clearInterval(state.calleeChatterInterval);
    state.calleeChatterInterval = setInterval(() => {
        if (state.isCallActive && state.callDurationSec > 10) {
            speakVoice(calleePhrases[phraseIdx % calleePhrases.length]);
            phraseIdx++;
        }
    }, 16000);
}

function simulateIncomingCall() {
    audio.init();
    const modal = document.getElementById('modalIncomingCall');
    if (modal) modal.classList.remove('hidden');

    audio.playTelephoneBell();
    clearInterval(state.bellInterval);
    state.bellInterval = setInterval(() => {
        audio.playTelephoneBell();
    }, 2200);
}

function answerIncomingCall() {
    clearInterval(state.bellInterval);
    audio.stopAllTones();
    const modal = document.getElementById('modalIncomingCall');
    if (modal) modal.classList.add('hidden');

    switchTab('booth');
    state.dialedNumber = '01123344556';
    updateVfdDisplay();

    const prefixInfo = {
        category: 'INCOMING STD (DELHI EXCHANGE)',
        rate: 0.00,
        label: '📥 Incoming Call from New Delhi Central Trunk Exchange'
    };
    connectCallActive(prefixInfo);

    speakVoice("Namaste! This is New Delhi Central Trunk Exchange calling PCO Booth 1. We are testing loop voltage and audio transmission. Line is clear.");
}

function rejectIncomingCall() {
    clearInterval(state.bellInterval);
    audio.stopAllTones();
    const modal = document.getElementById('modalIncomingCall');
    if (modal) modal.classList.add('hidden');
    audio.playRelayPulse();
}

function endCallSimulation() {
    if (!state.isCallActive) return;
    clearInterval(state.callTimerInterval);
    clearInterval(state.calleeChatterInterval);
    state.isCallActive = false;
    audio.stopAllTones();
    if ('speechSynthesis' in window) window.speechSynthesis.cancel();

    // Compute Final Billable Units
    const finalSecs = state.callDurationSec;
    const finalPulses = Math.max(1, Math.ceil(finalSecs / 60));
    const prefixInfo = classifyPrefix(state.dialedNumber);
    const finalCost = finalPulses * prefixInfo.rate;

    let cType = 'LOCAL';
    if (prefixInfo.category.includes('STD')) cType = 'STD';
    if (prefixInfo.category.includes('ISD')) cType = 'ISD';

    // Record into CDR
    const newCall = {
        id: `CDR-${Date.now().toString().slice(-4)}`,
        subscriberMobile: state.currentUser ? state.currentUser.mobile : '9849012345',
        timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16),
        destination: state.dialedNumber,
        type: cType,
        durationSec: finalSecs,
        pulses: finalPulses,
        cost: finalCost
    };
    state.calls.push(newCall);

    // Update Subscriber balance and pulse telemetry
    if (state.currentUser) {
        state.currentUser.balance += finalCost;
        state.currentUser.meterPulses += finalPulses;
        if (cType === 'LOCAL') {
            state.currentUser.freeMinsUsed += finalPulses;
        }
        saveData();
    }

    // Reset Console UI
    document.getElementById('callStateBadge').textContent = 'LINE ON HOOK (IDLE)';
    document.getElementById('callStateBadge').className = 'font-mono text-emerald-400 font-bold tracking-widest text-[11px] uppercase';
    document.getElementById('btnStartCall').disabled = false;
    document.getElementById('btnStartCall').classList.remove('opacity-40');
    document.getElementById('btnEndCall').disabled = true;
    document.getElementById('btnEndCall').classList.add('opacity-40');

    document.getElementById('vfdVoiceTranscript').textContent = `[Call terminated. Duration: ${finalSecs}s. Trunk voucher printed]`;

    // Pop up Vintage DoT Trunk Call Receipt Voucher Slip
    openTrunkSlipModal(newCall, prefixInfo);

    // Re-render recent slips
    renderBoothReceipts();
}

function openTrunkSlipModal(callRecord, prefixInfo) {
    document.getElementById('slipBoothNumber').textContent = `BOOTH NO: 0${state.activeBooth} (APEX PCO TRUNK)`;
    document.getElementById('slipVoucherNo').textContent = `DOT-PCO-${Math.floor(1000 + Math.random() * 9000)}`;
    document.getElementById('slipDateTime').textContent = callRecord.timestamp;
    document.getElementById('slipSubscriberName').textContent = state.currentUser ? state.currentUser.name : 'PCO Walk-in Customer';
    document.getElementById('slipDialedNumber').textContent = callRecord.destination;
    document.getElementById('slipCallCategory').textContent = prefixInfo.category;
    
    const m = Math.floor(callRecord.durationSec / 60);
    const s = callRecord.durationSec % 60;
    document.getElementById('slipDuration').textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')} (${callRecord.durationSec}s)`;
    document.getElementById('slipPulses').textContent = `${callRecord.pulses} Pulse(s) [60s Ceil]`;
    document.getElementById('slipRate').textContent = `₹${prefixInfo.rate.toFixed(2)} / Pulse`;
    document.getElementById('slipTotalCost').textContent = `₹ ${callRecord.cost.toFixed(2)}`;

    document.getElementById('modalTrunkSlip').classList.remove('hidden');
}

function closeTrunkSlipModal() {
    document.getElementById('modalTrunkSlip').classList.add('hidden');
    // Clear dialed number for next call
    state.dialedNumber = '';
    state.callDurationSec = 0;
    document.getElementById('vfdDuration').textContent = '00:00';
    document.getElementById('vfdPulses').textContent = '0';
    document.getElementById('vfdCharge').textContent = '₹ 0.00';
    updateVfdDisplay();
}

function renderBoothReceipts() {
    const list = document.getElementById('boothReceiptsList');
    if (!list) return;

    const recent = state.calls.slice(-3).reverse();
    if (recent.length === 0) {
        list.innerHTML = '<p class="text-xs text-slate-600 italic">No trunk slips printed yet.</p>';
        return;
    }

    list.innerHTML = recent.map(r => `
        <div class="p-3 rounded-2xl bg-white border border-slate-200 shadow-sm flex items-center justify-between text-xs font-mono">
            <div>
                <span class="font-bold text-slate-900">${r.destination}</span>
                <p class="text-[10px] text-slate-600">${r.timestamp} • ${r.pulses} pulse(s) • ${r.type}</p>
            </div>
            <div class="text-right">
                <span class="font-bold text-slate-900">₹${r.cost.toFixed(2)}</span>
                <span class="block text-[9px] text-emerald-600 font-bold">PAID (PCO)</span>
            </div>
        </div>
    `).join('');
}

// ============================================================================
// TAB 3: BILLS, INVOICES & PAYMENTS
// ============================================================================

function renderBillsScreen() {
    if (!state.currentUser) return;
    const user = state.currentUser;
    const userCalls = state.calls.filter(c => c.subscriberMobile === user.mobile);
    const plan = DEFAULT_PLANS[user.planId] || DEFAULT_PLANS.plan_postpaid_silver;

    // Current Bill Card
    const currentCard = document.getElementById('billsCurrentStatementCard');
    if (currentCard) {
        currentCard.innerHTML = `
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                <div>
                    <span class="text-[10px] font-bold text-jio-blue uppercase tracking-wider">Current Cycle Statement</span>
                    <h3 class="text-base font-black text-slate-900">August 2026 Billing Period</h3>
                </div>
                <span class="px-2.5 py-1 rounded-full text-xs font-bold ${user.balance <= 0 ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'}">
                    ${user.balance <= 0 ? '✓ SETTLED' : '⚠️ PAYMENT DUE'}
                </span>
            </div>

            <!-- Itemized breakdown -->
            <div class="space-y-2 text-xs">
                <div class="flex justify-between">
                    <span class="text-slate-600">Fixed Monthly Rental (${plan.name}):</span>
                    <span class="font-mono font-bold">₹${plan.rental.toFixed(2)}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-600">Free Talk-Time Allowance:</span>
                    <span class="font-mono text-emerald-600 font-bold">${plan.freeMinutes} Mins Covered</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-600">Itemized CDR Calls Charge:</span>
                    <span class="font-mono font-bold">₹${Math.max(0, user.balance - (plan.rental * 1.18)).toFixed(2)}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-600">18% Statutory GST (9% CGST + 9% SGST):</span>
                    <span class="font-mono font-bold">₹${(plan.rental * 0.18).toFixed(2)}</span>
                </div>
                <div class="flex justify-between border-t border-slate-200 pt-2 text-sm font-black">
                    <span class="text-slate-900">Total Outstanding Amount:</span>
                    <span class="font-mono text-jio-blue text-base">₹${user.balance.toFixed(2)}</span>
                </div>
            </div>

            <!-- Action buttons -->
            <div class="grid grid-cols-2 gap-2 pt-2">
                <button onclick="openPaymentModal()" ${user.balance <= 0 ? 'disabled' : ''}
                        class="py-2.5 px-3 rounded-xl bg-gradient-to-r from-jio-blue to-jio-electric text-white font-bold text-xs shadow-md transition disabled:opacity-40">
                    Pay Now
                </button>
                <button onclick="viewInvoiceModal()"
                        class="py-2.5 px-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs transition border border-slate-300 flex items-center justify-center space-x-1">
                    <i data-lucide="eye" class="w-3.5 h-3.5"></i>
                    <span>Tax Invoice</span>
                </button>
            </div>
        `;
    }

    // Subscriber CDR Table
    document.getElementById('cdrRecordCount').textContent = `${userCalls.length} Calls`;
    const tbody = document.getElementById('subscriberCdrTableBody');
    if (tbody) {
        if (userCalls.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="py-4 text-center text-slate-600">No calls recorded.</td></tr>';
        } else {
            tbody.innerHTML = userCalls.map(c => `
                <tr class="hover:bg-slate-50 transition">
                    <td class="py-2 text-[11px] text-slate-600">${c.timestamp.slice(5)}</td>
                    <td class="py-2 font-bold text-slate-900">${c.destination}</td>
                    <td class="py-2"><span class="px-1.5 py-0.5 rounded text-[10px] font-bold ${getCallTypeBadge(c.type)}">${c.type}</span></td>
                    <td class="py-2 text-slate-600">${Math.ceil(c.durationSec / 60)}m</td>
                    <td class="py-2 text-right font-bold text-slate-900">₹${c.cost.toFixed(2)}</td>
                </tr>
            `).join('');
        }
    }

    // Historical Bills List
    const histList = document.getElementById('historicalBillsList');
    if (histList) {
        histList.innerHTML = `
            <div class="p-3.5 rounded-2xl bg-white border border-slate-200 shadow-sm flex items-center justify-between text-xs">
                <div>
                    <h4 class="font-bold text-slate-900">July 2026 Telephone Statement</h4>
                    <p class="text-[10px] text-slate-600">APEX-202607-001 • Paid on 20-Aug-2026</p>
                </div>
                <div class="text-right">
                    <span class="font-mono font-bold text-slate-900">₹ 341.02</span>
                    <span class="block text-[10px] font-bold text-emerald-600">✓ PAID</span>
                </div>
            </div>
        `;
    }
}

function generateMonthlyBillForCurrentSubscriber() {
    if (!state.currentUser) return;
    const user = state.currentUser;
    const plan = DEFAULT_PLANS[user.planId] || DEFAULT_PLANS.plan_postpaid_silver;

    // Recalculate bill
    const userCalls = state.calls.filter(c => c.subscriberMobile === user.mobile);
    let totalCallCharges = 0;
    userCalls.forEach(c => { totalCallCharges += c.cost; });

    const subtotal = plan.rental + totalCallCharges;
    const gst = subtotal * 0.18;
    const net = subtotal + gst;

    user.balance = net;
    saveData();
    renderHomeScreen();
    renderBillsScreen();
    alert(`Bill recalculated! Subtotal: ₹${subtotal.toFixed(2)} + 18% GST (₹${gst.toFixed(2)}) = Net: ₹${net.toFixed(2)}`);
}

// Payment Modal Actions
function openPaymentModal() {
    if (!state.currentUser) return;
    document.getElementById('payModalAmount').textContent = `₹ ${state.currentUser.balance.toFixed(2)}`;
    document.getElementById('modalPayment').classList.remove('hidden');
}

function closePaymentModal() {
    document.getElementById('modalPayment').classList.add('hidden');
}

function processBillPayment() {
    if (!state.currentUser) return;
    state.currentUser.balance = 0.00;
    saveData();

    closePaymentModal();
    renderHomeScreen();
    renderBillsScreen();

    alert('🎉 Payment Succeeded!\nYour telephone bill has been marked PAID.\nOfficial DoT Transaction Ref: TXN-DOT-2026-' + Math.floor(10000 + Math.random() * 90000));
}

// ============================================================================
// MODAL 2: AIRTEL / JIO STYLED TAX INVOICE MODAL
// ============================================================================

function viewInvoiceModal() {
    if (!state.currentUser) return;
    const user = state.currentUser;
    const plan = DEFAULT_PLANS[user.planId] || DEFAULT_PLANS.plan_postpaid_silver;
    const userCalls = state.calls.filter(c => c.subscriberMobile === user.mobile);

    let callCostTotal = 0;
    userCalls.forEach(c => { callCostTotal += c.cost; });

    const subtotal = plan.rental + callCostTotal;
    const cgst = subtotal * 0.09;
    const sgst = subtotal * 0.09;
    const grandTotal = subtotal + cgst + sgst;

    const invoiceContainer = document.getElementById('printableTaxInvoice');
    if (!invoiceContainer) return;

    invoiceContainer.innerHTML = `
        <!-- Telco Brand Header -->
        <div class="border-b-2 border-slate-900 pb-3 flex items-start justify-between">
            <div>
                <h2 class="text-xl font-black tracking-tight text-slate-900">APEX TELECOMMUNICATIONS INDIA LTD.</h2>
                <p class="text-[11px] text-slate-600">Registered Office: Apex Towers, Bandra-Kurla Complex, Mumbai - 400051</p>
                <p class="text-[11px] text-slate-600">GSTIN: <strong class="font-mono text-slate-900">27AAACA1234F1Z5</strong> | DoT License No: TEL/PCO/2026/8941</p>
            </div>
            <div class="text-right">
                <span class="inline-block px-3 py-1 rounded border-2 border-slate-900 font-mono font-black text-xs uppercase">
                    TAX INVOICE
                </span>
                <p class="text-[10px] text-slate-600 mt-1">ORIGINAL FOR RECIPIENT</p>
            </div>
        </div>

        <!-- Subscriber & Invoice Meta Table -->
        <div class="grid grid-cols-2 gap-4 py-2 border-b border-slate-200">
            <div>
                <h4 class="font-bold text-slate-900 uppercase text-[10px] text-blue-800">BILL TO:</h4>
                <p class="font-bold text-sm text-slate-900">${user.name}</p>
                <p class="text-slate-600">${user.address}</p>
                <p class="font-mono text-slate-800 mt-1">Landline: <strong>${user.landline}</strong> | Mobile: +91 ${user.mobile}</p>
            </div>
            <div class="text-right font-mono space-y-0.5 text-[11px]">
                <p>Invoice No: <strong>APEX-202608-001</strong></p>
                <p>Invoice Date: <strong>01-Sep-2026</strong></p>
                <p>Billing Period: <strong>01-Aug-2026 to 31-Aug-2026</strong></p>
                <p>Payment Due Date: <strong class="text-amber-700">25-Sep-2026</strong></p>
                <p>Plan Enrolled: <strong>${plan.name}</strong></p>
            </div>
        </div>

        <!-- Itemized Charges Table -->
        <div>
            <h4 class="font-bold text-slate-900 uppercase text-[10px] text-blue-800 mb-1">SUMMARY OF CHARGES:</h4>
            <table class="w-full text-left border-collapse border border-slate-300 text-xs">
                <thead>
                    <tr class="bg-slate-100 font-bold border-b border-slate-300 text-[11px]">
                        <th class="p-2 border border-slate-300">Description</th>
                        <th class="p-2 border border-slate-300 text-center">Pulses / Units</th>
                        <th class="p-2 border border-slate-300 text-right">Rate</th>
                        <th class="p-2 border border-slate-300 text-right">Amount (₹)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="p-2 border border-slate-300 font-medium">Monthly Landline Rental (${plan.name})</td>
                        <td class="p-2 border border-slate-300 text-center font-mono">1 Month</td>
                        <td class="p-2 border border-slate-300 text-right font-mono">₹${plan.rental.toFixed(2)}</td>
                        <td class="p-2 border border-slate-300 text-right font-mono font-bold">₹${plan.rental.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td class="p-2 border border-slate-300 font-medium">Itemized CDR Trunk & Local Calls</td>
                        <td class="p-2 border border-slate-300 text-center font-mono">${userCalls.length} Calls</td>
                        <td class="p-2 border border-slate-300 text-right font-mono">Tiered Slabs</td>
                        <td class="p-2 border border-slate-300 text-right font-mono font-bold">₹${callCostTotal.toFixed(2)}</td>
                    </tr>
                    <tr class="bg-slate-50 font-bold">
                        <td colspan="3" class="p-2 border border-slate-300 text-right">TAXABLE VALUE (SUBTOTAL):</td>
                        <td class="p-2 border border-slate-300 text-right font-mono">₹${subtotal.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td colspan="3" class="p-2 border border-slate-300 text-right">CGST @ 9.0%:</td>
                        <td class="p-2 border border-slate-300 text-right font-mono">₹${cgst.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td colspan="3" class="p-2 border border-slate-300 text-right">SGST @ 9.0%:</td>
                        <td class="p-2 border border-slate-300 text-right font-mono">₹${sgst.toFixed(2)}</td>
                    </tr>
                    <tr class="bg-blue-50 font-black text-sm">
                        <td colspan="3" class="p-2 border border-slate-300 text-right text-slate-900">TOTAL AMOUNT PAYABLE:</td>
                        <td class="p-2 border border-slate-300 text-right font-mono text-jio-blue">₹${grandTotal.toFixed(2)}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Call Detail Records (CDR) Table -->
        <div class="pt-2">
            <h4 class="font-bold text-slate-900 uppercase text-[10px] text-blue-800 mb-1">CALL DETAIL RECORDS (CDR):</h4>
            <table class="w-full text-left border-collapse border border-slate-200 text-[10px]">
                <thead>
                    <tr class="bg-slate-100 font-bold border-b border-slate-200">
                        <th class="p-1 border border-slate-200">Date/Time</th>
                        <th class="p-1 border border-slate-200">Destination</th>
                        <th class="p-1 border border-slate-200">Type</th>
                        <th class="p-1 border border-slate-200 text-center">Duration</th>
                        <th class="p-1 border border-slate-200 text-right">Amount (₹)</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 font-mono">
                    ${userCalls.map(c => `
                        <tr>
                            <td class="p-1 border border-slate-200">${c.timestamp}</td>
                            <td class="p-1 border border-slate-200 font-bold">${c.destination}</td>
                            <td class="p-1 border border-slate-200">${c.type}</td>
                            <td class="p-1 border border-slate-200 text-center">${Math.ceil(c.durationSec / 60)} min(s)</td>
                            <td class="p-1 border border-slate-200 text-right font-bold">₹${c.cost.toFixed(2)}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>

        <!-- Footer Regulatory Declaration -->
        <div class="border-t border-slate-300 pt-3 text-[10px] text-slate-600 space-y-1">
            <p>1. This is a computer-generated tax invoice and does not require a physical signature under TRAI Telecom Regulation 2026.</p>
            <p>2. Please pay before the due date to avoid a statutory ₹50 late surcharge and line suspension.</p>
        </div>
    `;

    document.getElementById('modalTaxInvoice').classList.remove('hidden');
}

function closeInvoiceModal() {
    document.getElementById('modalTaxInvoice').classList.add('hidden');
}

function downloadInvoicePdf() {
    const el = document.getElementById('printableTaxInvoice');
    if (!el || !window.html2pdf) {
        window.print();
        return;
    }

    const opt = {
        margin: 0.3,
        filename: `Apex_Telecom_Bill_${state.currentUser ? state.currentUser.mobile : 'Invoice'}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2 },
        jsPDF: { unit: 'in', format: 'letter', orientation: 'portrait' }
    };
    window.html2pdf().set(opt).from(el).save();
}

// ============================================================================
// TAB 4: VINTAGE TARIFF PLANS
// ============================================================================

function renderTariffPlansScreen() {
    const container = document.getElementById('tariffPlansContainer');
    if (!container || !state.currentUser) return;

    const currentPlanId = state.currentUser.planId;

    container.innerHTML = Object.values(DEFAULT_PLANS).map(p => {
        const isCurrent = p.id === currentPlanId;
        return `
            <div class="p-4 rounded-3xl bg-white border ${isCurrent ? 'border-2 border-jio-blue shadow-lg' : 'border-slate-200'} shadow-sm space-y-3">
                <div class="flex items-center justify-between">
                    <div>
                        <span class="text-[10px] font-bold px-2 py-0.5 rounded-md bg-blue-50 text-jio-blue uppercase tracking-wider">${p.badge}</span>
                        <h3 class="text-base font-black text-slate-900 mt-1">${p.name}</h3>
                    </div>
                    <div class="text-right">
                        <span class="text-2xl font-black text-slate-900 font-mono">₹${p.rental.toFixed(0)}</span>
                        <span class="text-xs text-slate-600 block">/ Month</span>
                    </div>
                </div>

                <p class="text-xs text-slate-600">${p.description}</p>

                <!-- Pulse Rates Matrix -->
                <div class="grid grid-cols-3 gap-2 p-2.5 rounded-2xl bg-slate-50 border border-slate-100 text-center font-mono text-xs">
                    <div>
                        <span class="text-[10px] text-slate-600 block">LOCAL</span>
                        <span class="font-bold text-slate-800">₹${p.rateLocal.toFixed(2)}</span>
                    </div>
                    <div>
                        <span class="text-[10px] text-slate-600 block">STD (TRUNK)</span>
                        <span class="font-bold text-indigo-700">₹${p.rateStd.toFixed(2)}</span>
                    </div>
                    <div>
                        <span class="text-[10px] text-slate-600 block">ISD GLOBAL</span>
                        <span class="font-bold text-purple-700">₹${p.rateIsd.toFixed(2)}</span>
                    </div>
                </div>

                <div class="flex items-center justify-between pt-1">
                    <span class="text-xs text-slate-600 font-medium">Free Talk-Time: <strong class="text-emerald-700">${p.freeMinutes} Mins</strong></span>
                    ${isCurrent ? `
                        <span class="px-3 py-1.5 rounded-xl bg-emerald-100 text-emerald-800 font-bold text-xs">
                            ✓ Currently Active
                        </span>
                    ` : `
                        <button onclick="activatePlan('${p.id}')" class="px-3.5 py-1.5 rounded-xl bg-jio-blue hover:bg-blue-700 text-white font-bold text-xs shadow-sm transition">
                            Switch to Plan
                        </button>
                    `}
                </div>
            </div>
        `;
    }).join('');
}

function activatePlan(planId) {
    if (!state.currentUser) return;
    const plan = DEFAULT_PLANS[planId];
    if (!plan) return;

    state.currentUser.planId = planId;
    saveData();

    renderHomeScreen();
    renderBillsScreen();
    renderTariffPlansScreen();

    alert(`✅ Successfully enrolled in "${plan.name}"!\nYour monthly base rental is now ₹${plan.rental.toFixed(2)} with ${plan.freeMinutes} free local minutes.`);
}

// ============================================================================
// TAB 5: ACCOUNT & KYC & LINE DIAGNOSTICS
// ============================================================================

function renderAccountScreen() {
    if (!state.currentUser) return;
    const user = state.currentUser;
    const plan = DEFAULT_PLANS[user.planId] || DEFAULT_PLANS.plan_postpaid_silver;

    const kyc = document.getElementById('accountKycCard');
    if (kyc) {
        kyc.innerHTML = `
            <div class="flex items-center space-x-3 border-b border-slate-100 pb-3">
                <div class="w-12 h-12 rounded-2xl bg-jio-blue text-white flex items-center justify-center font-black text-xl shadow-md">
                    ${user.name.charAt(0)}
                </div>
                <div>
                    <h3 class="font-black text-base text-slate-900">${user.name}</h3>
                    <p class="text-xs text-slate-600 font-mono">${user.landline} (Line ID: ${user.id})</p>
                </div>
            </div>

            <div class="space-y-2 pt-1 font-medium text-slate-700">
                <div class="flex justify-between">
                    <span class="text-slate-600">Registered Mobile:</span>
                    <span class="font-mono font-bold">+91 ${user.mobile}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-600">Telecom Circle:</span>
                    <span class="font-bold">${user.circle}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-600">Installation Address:</span>
                    <span class="text-right text-slate-800 text-[11px] max-w-[200px]">${user.address}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-600">Enrolled Plan:</span>
                    <span class="font-bold text-jio-blue">${plan.name}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-600">Line Activation Date:</span>
                    <span class="font-mono">${user.connectionDate}</span>
                </div>
            </div>
        `;
    }
}

function runLineDiagnostics() {
    const btn = document.getElementById('btnRunDiagnostics');
    btn.disabled = true;
    btn.textContent = 'Testing Line...';

    audio.playRelayPulse();

    setTimeout(() => {
        document.getElementById('diagVoltage').textContent = '48.4 V DC (Stable Continuous)';
        document.getElementById('diagDialTone').textContent = '400 Hz Steady (Low Jitter)';
        document.getElementById('diagMetering').textContent = '16 kHz Pulse Synchronized';
        document.getElementById('diagResistance').textContent = '415 Ω (Line Clear)';

        btn.disabled = false;
        btn.textContent = '✓ Pass (4/4)';
        setTimeout(() => { btn.textContent = '⚡ Run Test'; }, 3000);
    }, 1200);
}

// ============================================================================
// TOP CONTROLS (VIEWPORT TOGGLE & AUDIO)
// ============================================================================

function toggleViewportMode() {
    const shell = document.getElementById('mobileShell');
    const btnText = document.getElementById('viewportModeText');
    state.isWideMode = !state.isWideMode;

    if (state.isWideMode) {
        shell.classList.add('wide-mode');
        btnText.textContent = 'Switch to Mobile Frame';
    } else {
        shell.classList.remove('wide-mode');
        btnText.textContent = 'Expand to Wide View';
    }
}

function toggleAudioMute() {
    state.audioMuted = !state.audioMuted;
    const icon = document.getElementById('audioIcon');
    const text = document.getElementById('audioStatusText');

    if (state.audioMuted) {
        icon.className = 'w-3.5 h-3.5 text-rose-400';
        text.textContent = 'Dialer Audio: MUTED';
    } else {
        icon.className = 'w-3.5 h-3.5 text-emerald-400';
        text.textContent = 'Dialer Audio: ON';
    }
}

// ============================================================================
// DOM READY BOOTSTRAP
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
    loadData();

    if (state.currentUser) {
        renderAuthenticatedApp();
    } else {
        document.getElementById('screenLogin').classList.remove('hidden');
        document.getElementById('appBody').classList.add('hidden');
        document.getElementById('bottomNavBar').classList.add('hidden');
    }

    if (window.lucide) {
        window.lucide.createIcons();
    }
});
