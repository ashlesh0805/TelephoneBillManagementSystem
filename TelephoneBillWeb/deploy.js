const fs = require('fs');
const path = require('path');

const TOKEN = process.env.VERCEL_TOKEN || '';

async function deploy() {
    console.log('Reading deployment files...');
    const indexHtml = fs.readFileSync(path.join(__dirname, 'index.html'), 'utf8');
    const appJs = fs.readFileSync(path.join(__dirname, 'app.js'), 'utf8');
    const vercelJson = fs.readFileSync(path.join(__dirname, 'vercel.json'), 'utf8');

    const payload = {
        name: 'apex-telecom-billing',
        files: [
            {
                file: 'index.html',
                data: indexHtml
            },
            {
                file: 'app.js',
                data: appJs
            },
            {
                file: 'vercel.json',
                data: vercelJson
            }
        ],
        projectSettings: {
            framework: null
        }
    };

    console.log('Sending deployment payload to Vercel API...');
    const response = await fetch('https://api.vercel.com/v13/deployments', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${TOKEN}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    const result = await response.json();
    console.log('Status:', response.status);
    console.log('Deployment Result:', JSON.stringify(result, null, 2));

    if (result.url) {
        console.log('\n=============================================');
        console.log('DEPLOYMENT SUCCESSFUL!');
        console.log(`Live URL: https://${result.url}`);
        if (result.alias && result.alias.length > 0) {
            console.log(`Aliases: ${result.alias.map(a => `https://${a}`).join(', ')}`);
        }
        console.log('=============================================\n');
    }
}

deploy().catch(err => {
    console.error('Deployment error:', err);
});
