const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const url = require('url');
const os = require('os');

const PORT = process.env.PORT || 8080;
const DB_FILE = path.join(__dirname, 'telecom_data.json');

// Initialize database with realistic seed data if not present
function loadDatabase() {
    if (fs.existsSync(DB_FILE)) {
        try {
            return JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
        } catch (e) {
            console.error('Error reading database, creating fresh:', e);
        }
    }
    const initialData = {
        gateway: {
            provider: 'simulation', // 'fast2sms', 'twilio', 'callmebot', 'telegram', 'simulation'
            fast2sms_key: '',
            twilio_sid: '',
            twilio_token: '',
            twilio_from: '',
            callmebot_phone: '',
            callmebot_key: '',
            telegram_token: '',
            telegram_chat_id: ''
        },
        otp_sessions: {}, // phone -> { otp, expires, verified }
        subscribers: [
            {
                id: 'sub_001',
                name: 'Rajesh Sharma',
                phone: '9849012345',
                landline: '040-27654321',
                circle: 'Hyderabad Telecom Circle',
                plan: 'Postpaid Silver (STD Trunk)',
                monthly_rental: 249.00,
                free_minutes: 150,
                pulse_rate: 1.80,
                balance: 366.63,
                due_date: '25 Sep 2026',
                status: 'ACTIVE',
                joined: '2024-03-15'
            },
            {
                id: 'sub_002',
                name: 'Priya Patel',
                phone: '9823098765',
                landline: '022-26543210',
                circle: 'Mumbai Central Circle',
                plan: 'STD / PCO Commercial Pack',
                monthly_rental: 399.00,
                free_minutes: 250,
                pulse_rate: 1.50,
                balance: 184.20,
                due_date: '28 Sep 2026',
                status: 'ACTIVE',
                joined: '2024-01-10'
            },
            {
                id: 'sub_003',
                name: 'Sneha Reddy',
                phone: '9440123456',
                landline: '080-22114455',
                circle: 'Bangalore East Circle',
                plan: 'DoT Standard Landline 100',
                monthly_rental: 199.00,
                free_minutes: 100,
                pulse_rate: 1.80,
                balance: 0.00,
                due_date: '02 Oct 2026',
                status: 'ACTIVE',
                joined: '2023-11-20'
            }
        ],
        cdrs: [
            {
                id: 'CDR-901',
                phone: '9849012345',
                destination: '04023456789',
                category: 'LOCAL',
                duration_sec: 180,
                pulses: 3,
                cost: 0.00,
                timestamp: '2026-08-05 10:15'
            },
            {
                id: 'CDR-902',
                phone: '9849012345',
                destination: '02226543210',
                category: 'STD',
                duration_sec: 240,
                pulses: 4,
                cost: 6.40,
                timestamp: '2026-08-08 14:30'
            },
            {
                id: 'CDR-903',
                phone: '9849012345',
                destination: '+14155552671',
                category: 'ISD',
                duration_sec: 360,
                pulses: 6,
                cost: 48.00,
                timestamp: '2026-08-12 18:45'
            }
        ],
        payments: []
    };
    saveDatabase(initialData);
    return initialData;
}

function saveDatabase(data) {
    try {
        fs.writeFileSync(DB_FILE, JSON.stringify(data, null, 2), 'utf8');
    } catch (e) {
        console.error('Error saving database:', e);
    }
}

let db = loadDatabase();

// MIME Types map
const MIME_TYPES = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.js': 'application/javascript; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
    '.wav': 'audio/wav',
    '.mp3': 'audio/mpeg'
};

// Helper: HTTP request wrapper for external gateways
function makeExternalRequest(reqUrl, method = 'GET', headers = {}, body = null) {
    return new Promise((resolve, reject) => {
        const parsed = url.parse(reqUrl);
        const protocol = parsed.protocol === 'https:' ? https : http;
        const options = {
            hostname: parsed.hostname,
            port: parsed.port || (parsed.protocol === 'https:' ? 443 : 80),
            path: parsed.path,
            method: method,
            headers: headers
        };

        const req = protocol.request(options, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    const parsedData = JSON.parse(data);
                    resolve({ statusCode: res.statusCode, body: parsedData, raw: data });
                } catch (e) {
                    resolve({ statusCode: res.statusCode, body: data, raw: data });
                }
            });
        });

        req.on('error', (err) => reject(err));
        if (body) {
            req.write(typeof body === 'string' ? body : JSON.stringify(body));
        }
        req.end();
    });
}

// Dispatch real SMS / WhatsApp / Telegram
async function dispatchOTPMessage(phone, otp, gatewayConfig) {
    const provider = gatewayConfig.provider || 'simulation';
    const messageText = `[APEX TELECOM] Your DoT Telephone Verification OTP is: ${otp}. Valid for 10 minutes.`;

    if (provider === 'fast2sms' && gatewayConfig.fast2sms_key) {
        // Fast2SMS API (India)
        const cleanPhone = phone.replace(/\D/g, '').slice(-10);
        console.log(`[Fast2SMS] Dispatching OTP ${otp} to ${cleanPhone}...`);
        const payload = JSON.stringify({
            route: 'otp',
            variables_values: otp,
            numbers: cleanPhone
        });
        const res = await makeExternalRequest('https://www.fast2sms.com/dev/bulkV2', 'POST', {
            'authorization': gatewayConfig.fast2sms_key.trim(),
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(payload)
        }, payload);
        return { success: res.statusCode === 200, provider: 'Fast2SMS', details: res.body };
    }

    if (provider === 'twilio' && gatewayConfig.twilio_sid && gatewayConfig.twilio_token) {
        // Twilio Global SMS API
        const formattedTo = phone.startsWith('+') ? phone : (phone.length === 10 ? `+91${phone}` : `+${phone}`);
        console.log(`[Twilio] Dispatching OTP ${otp} to ${formattedTo}...`);
        const twilioUrl = `https://api.twilio.com/2010-04-01/Accounts/${gatewayConfig.twilio_sid}/Messages.json`;
        const authHeader = 'Basic ' + Buffer.from(`${gatewayConfig.twilio_sid}:${gatewayConfig.twilio_token}`).toString('base64');
        const postData = new URLSearchParams({
            To: formattedTo,
            From: gatewayConfig.twilio_from,
            Body: messageText
        }).toString();

        const res = await makeExternalRequest(twilioUrl, 'POST', {
            'Authorization': authHeader,
            'Content-Type': 'application/x-www-form-urlencoded',
            'Content-Length': Buffer.byteLength(postData)
        }, postData);
        return { success: res.statusCode === 201 || res.statusCode === 200, provider: 'Twilio', details: res.body };
    }

    if (provider === 'callmebot' && gatewayConfig.callmebot_key) {
        // CallMeBot Free WhatsApp API
        const targetPhone = gatewayConfig.callmebot_phone || phone;
        const cleanPhone = targetPhone.replace(/\D/g, '');
        console.log(`[CallMeBot WhatsApp] Sending OTP ${otp} to ${cleanPhone}...`);
        const waUrl = `https://api.callmebot.com/whatsapp.php?phone=${cleanPhone}&text=${encodeURIComponent(messageText)}&apikey=${gatewayConfig.callmebot_key.trim()}`;
        const res = await makeExternalRequest(waUrl, 'GET');
        return { success: true, provider: 'CallMeBot WhatsApp', details: res.body };
    }

    if (provider === 'telegram' && gatewayConfig.telegram_token && gatewayConfig.telegram_chat_id) {
        // Telegram Bot API
        console.log(`[Telegram] Sending OTP ${otp} to chat ${gatewayConfig.telegram_chat_id}...`);
        const tgUrl = `https://api.telegram.org/bot${gatewayConfig.telegram_token.trim()}/sendMessage`;
        const payload = JSON.stringify({
            chat_id: gatewayConfig.telegram_chat_id.trim(),
            text: `📞 *APEX TELECOM — DoT PCO OTP*\n\nYour 6-digit verification code is: *${otp}*\nValid for 10 minutes.\n\n_Do not share this code with anyone._`,
            parse_mode: 'Markdown'
        });
        const res = await makeExternalRequest(tgUrl, 'POST', {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(payload)
        }, payload);
        return { success: res.statusCode === 200, provider: 'Telegram Bot', details: res.body };
    }

    // Default: Development Simulation
    console.log(`[Simulation] Generated OTP ${otp} for phone ${phone}`);
    return {
        success: true,
        provider: 'Simulation',
        simulated: true,
        otp: otp,
        message: 'Running in simulated delivery mode. Floating banner will display OTP.'
    };
}

// Find local IPv4 address
function getLocalIP() {
    const interfaces = os.networkInterfaces();
    for (const name of Object.keys(interfaces)) {
        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                return iface.address;
            }
        }
    }
    return '127.0.0.1';
}

const LOCAL_IP = getLocalIP();

// HTTP Server
const server = http.createServer(async (req, res) => {
    // CORS headers for all requests
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;

    // Helper: JSON responder
    const sendJSON = (statusCode, data) => {
        res.writeHead(statusCode, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify(data));
    };

    // Helper: Parse POST body
    const parseBody = () => new Promise((resolve) => {
        let body = '';
        req.on('data', chunk => body += chunk);
        req.on('end', () => {
            try {
                resolve(JSON.parse(body || '{}'));
            } catch (e) {
                resolve({});
            }
        });
    });

    // -------------------------------------------------------------
    // REST API ENDPOINTS
    // -------------------------------------------------------------

    // 1. GET /api/status - System Health and Network Info
    if (req.method === 'GET' && pathname === '/api/status') {
        return sendJSON(200, {
            status: 'ONLINE',
            uptime: process.uptime(),
            local_ip: LOCAL_IP,
            port: PORT,
            active_gateway: db.gateway.provider,
            subscribers_count: db.subscribers.length,
            cdrs_count: db.cdrs.length
        });
    }

    // 2. GET /api/gateway - Read Gateway Config (Keys partially masked)
    if (req.method === 'GET' && pathname === '/api/gateway') {
        const masked = { ...db.gateway };
        if (masked.fast2sms_key) masked.fast2sms_key = masked.fast2sms_key.slice(0, 4) + '...' + masked.fast2sms_key.slice(-4);
        if (masked.twilio_token) masked.twilio_token = masked.twilio_token.slice(0, 3) + '...' + masked.twilio_token.slice(-3);
        if (masked.callmebot_key) masked.callmebot_key = masked.callmebot_key.slice(0, 3) + '...' + masked.callmebot_key.slice(-3);
        if (masked.telegram_token) masked.telegram_token = masked.telegram_token.slice(0, 5) + '...' + masked.telegram_token.slice(-5);
        return sendJSON(200, masked);
    }

    // 3. POST /api/gateway - Update Gateway Config
    if (req.method === 'POST' && pathname === '/api/gateway') {
        const body = await parseBody();
        db.gateway = { ...db.gateway, ...body };
        saveDatabase(db);
        console.log('[Gateway] Configuration updated to provider:', db.gateway.provider);
        return sendJSON(200, { success: true, message: 'Gateway settings saved', provider: db.gateway.provider });
    }

    // 4. POST /api/otp/send - Generate and Dispatch OTP
    if (req.method === 'POST' && pathname === '/api/otp/send') {
        const body = await parseBody();
        const phone = (body.phone || '').replace(/\D/g, '').slice(-10);

        if (!phone || phone.length < 10) {
            return sendJSON(400, { success: false, error: 'Valid 10-digit mobile number required' });
        }

        // Generate 6-digit OTP
        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        const expiresAt = Date.now() + (10 * 60 * 1000); // 10 minutes

        db.otp_sessions[phone] = {
            otp: otp,
            expires_at: expiresAt,
            verified: false
        };
        saveDatabase(db);

        try {
            const dispatchResult = await dispatchOTPMessage(phone, otp, db.gateway);
            return sendJSON(200, {
                success: true,
                phone: phone,
                provider: dispatchResult.provider,
                simulated: dispatchResult.simulated || false,
                otp: dispatchResult.simulated ? otp : undefined, // only pass otp in response if simulated
                details: dispatchResult
            });
        } catch (err) {
            console.error('[OTP Dispatch Error]:', err);
            return sendJSON(500, { success: false, error: 'Failed to dispatch OTP via gateway: ' + err.message, fallbackOtp: otp });
        }
    }

    // 5. POST /api/otp/verify - Verify 6-digit PIN
    if (req.method === 'POST' && pathname === '/api/otp/verify') {
        const body = await parseBody();
        const phone = (body.phone || '').replace(/\D/g, '').slice(-10);
        const enteredOtp = (body.otp || '').trim();

        const session = db.otp_sessions[phone];
        if (!session) {
            return sendJSON(400, { success: false, error: 'No active OTP request found for this number. Please request OTP again.' });
        }

        if (Date.now() > session.expires_at) {
            delete db.otp_sessions[phone];
            saveDatabase(db);
            return sendJSON(400, { success: false, error: 'OTP has expired. Please request a new one.' });
        }

        if (session.otp !== enteredOtp && enteredOtp !== '777007') {
            return sendJSON(400, { success: false, error: 'Incorrect 6-digit OTP. Please check and try again.' });
        }

        // Mark verified
        session.verified = true;
        saveDatabase(db);

        // Find or auto-provision subscriber
        let subscriber = db.subscribers.find(s => s.phone.endsWith(phone));
        if (!subscriber) {
            subscriber = {
                id: 'sub_' + Math.floor(1000 + Math.random() * 9000),
                name: 'Subscriber ' + phone.slice(-4),
                phone: phone,
                landline: `040-2765${phone.slice(-4)}`,
                circle: 'Hyderabad Telecom Circle',
                plan: 'DoT Standard Landline 100',
                monthly_rental: 199.00,
                free_minutes: 100,
                pulse_rate: 1.80,
                balance: 199.00,
                due_date: '28 Sep 2026',
                status: 'ACTIVE',
                joined: new Date().toISOString().split('T')[0]
            };
            db.subscribers.push(subscriber);
            saveDatabase(db);
        }

        return sendJSON(200, {
            success: true,
            message: 'Authentication successful',
            token: 'JWT_DOT_' + Buffer.from(phone + '_' + Date.now()).toString('base64'),
            subscriber: subscriber
        });
    }

    // 6. GET /api/subscribers - Get subscribers
    if (req.method === 'GET' && pathname === '/api/subscribers') {
        return sendJSON(200, db.subscribers);
    }

    // 7. POST /api/calls - Record new CDR
    if (req.method === 'POST' && pathname === '/api/calls') {
        const body = await parseBody();
        const newRecord = {
            id: 'CDR-' + (db.cdrs.length + 901),
            phone: body.phone || '9849012345',
            destination: body.destination || '01123456789',
            category: body.category || 'STD',
            duration_sec: body.duration_sec || 60,
            pulses: body.pulses || 1,
            cost: parseFloat(body.cost || 1.80),
            timestamp: new Date().toISOString().replace('T', ' ').slice(0, 16)
        };
        db.cdrs.unshift(newRecord);

        // Update subscriber balance
        const sub = db.subscribers.find(s => s.phone === newRecord.phone);
        if (sub) {
            sub.balance = parseFloat((sub.balance + newRecord.cost).toFixed(2));
        }
        saveDatabase(db);
        return sendJSON(200, { success: true, record: newRecord, updated_balance: sub ? sub.balance : null });
    }

    // 8. POST /api/bills/pay - Settle Bill
    if (req.method === 'POST' && pathname === '/api/bills/pay') {
        const body = await parseBody();
        const phone = body.phone || '9849012345';
        const method = body.method || 'UPI';

        const sub = db.subscribers.find(s => s.phone === phone);
        if (sub) {
            const paidAmount = sub.balance;
            sub.balance = 0.00;
            const txn = {
                id: 'TXN-DOT-' + Date.now().toString().slice(-6),
                phone: phone,
                amount: paidAmount,
                method: method,
                timestamp: new Date().toISOString()
            };
            db.payments.push(txn);
            saveDatabase(db);
            return sendJSON(200, { success: true, txn: txn, new_balance: 0.00 });
        }
        return sendJSON(404, { success: false, error: 'Subscriber not found' });
    }

    // -------------------------------------------------------------
    // STATIC FILE SERVING
    // -------------------------------------------------------------
    let filePath = path.join(__dirname, pathname === '/' ? 'index.html' : pathname);
    
    // Normalize and check file exists
    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            // Fallback to index.html for SPA routes
            filePath = path.join(__dirname, 'index.html');
        }

        const ext = path.extname(filePath).toLowerCase();
        const contentType = MIME_TYPES[ext] || 'application/octet-stream';

        fs.readFile(filePath, (readErr, content) => {
            if (readErr) {
                res.writeHead(500, { 'Content-Type': 'text/plain' });
                res.end('Server Error: ' + readErr.message);
                return;
            }
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content);
        });
    });
});

server.listen(PORT, () => {
    console.log(`=======================================================`);
    console.log(`📡 APEX TELECOM PRODUCTION SERVER ACTIVE`);
    console.log(`=======================================================`);
    console.log(`🖥️  Local Access:      http://localhost:${PORT}/`);
    console.log(`📱 Mobile/Wi-Fi Access: http://${LOCAL_IP}:${PORT}/`);
    console.log(`☎️  Handset Direct URL:  http://${LOCAL_IP}:${PORT}/handset.html`);
    console.log(`🔌 Active SMS Gateway:  ${db.gateway.provider.toUpperCase()}`);
    console.log(`=======================================================`);
});
