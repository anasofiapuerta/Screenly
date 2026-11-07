// Vercel Serverless API Endpoint for Android <-> Web Real-Time Telemetry Synchronization
// Allows the Android native Screenly app to POST real UsageStatsManager, PackageManager,
// AppLimit, and AlertItem payloads and the Web interface to fetch them.

let latestAndroidSnapshot = null;

module.exports = async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method === 'POST') {
    try {
      const payload = typeof req.body === 'string' ? JSON.parse(req.body) : req.body;
      if (!payload || !Array.isArray(payload.apps)) {
        return res.status(400).json({
          ok: false,
          error: 'Invalid Android telemetry payload: missing apps array'
        });
      }

      latestAndroidSnapshot = {
        ...payload,
        receivedAtServer: new Date().toISOString()
      };

      return res.status(200).json({
        ok: true,
        message: 'Sincronización Android recibida en Vercel correctamente',
        syncedAt: latestAndroidSnapshot.receivedAtServer,
        appsCount: payload.apps.length,
        limitsCount: Array.isArray(payload.limits) ? payload.limits.length : 0
      });
    } catch (err) {
      return res.status(400).json({
        ok: false,
        error: 'Malformed JSON payload',
        details: err.message
      });
    }
  }

  if (req.method === 'GET') {
    return res.status(200).json({
      ok: true,
      hasLiveAndroidSync: latestAndroidSnapshot !== null,
      snapshot: latestAndroidSnapshot
    });
  }

  return res.status(405).json({ ok: false, error: 'Method not allowed' });
};
