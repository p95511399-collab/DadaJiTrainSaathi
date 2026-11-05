import express from 'express';
const app = express();
app.use(express.json());
const PORT = process.env.PORT || 10000;
const API_KEY = process.env.RAILRADAR_API_KEY;
const BASE = 'https://api.railradar.in/v1';

async function railradar(path) {
  if (!API_KEY) throw new Error('RAILRADAR_API_KEY is not configured on the server');
  const r = await fetch(`${BASE}${path}`, { headers: { Authorization: `Bearer ${API_KEY}`, 'Accept-Language': 'hi' } });
  const j = await r.json();
  if (!r.ok || j.success === false) throw new Error(j?.error?.message || `RailRadar returned HTTP ${r.status}`);
  return j;
}
app.get('/health', (_req, res) => res.json({ ok: true, service: 'DadaJiTrainSaathi' }));
app.get('/api/live/:number', async (req, res) => {
  try {
    if (!/^[0-9A-Za-z\s_/-]{1,20}$/.test(req.params.number)) return res.status(400).json({ error: 'Invalid train number' });
    const j = await railradar(`/trains/${encodeURIComponent(req.params.number)}/live?lang=hi`);
    const d = j.data || {};
    const c = d.currentLocation || {};
    const next = d.nextHalt || {};
    const location = c.stationName ? `${c.stationName}${c.status ? ` (${c.status})` : ''}` : (c.latitude && c.longitude ? `GPS ${c.latitude}, ${c.longitude}` : 'सटीक वर्तमान स्थान उपलब्ध नहीं');
    res.json({ trainNumber: d.trainNumber || req.params.number, trainName: d.trainName || d.train?.name || `Train ${req.params.number}`, location, nextStation: next.stationName || 'उपलब्ध नहीं', delayMinutes: Number.isFinite(d.delayMinutes) ? d.delayMinutes : null, lastUpdatedAt: d.lastUpdatedAt || j.meta?.timestamp || null, isLive: Boolean(d.isLive), source: j.meta?.source || null });
  } catch (e) { res.status(502).json({ error: e.message }); }
});
app.get('/api/search', async (req, res) => {
  try {
    const q = String(req.query.q || '').trim();
    if (q.length < 2 || q.length > 80) return res.status(400).json({ error: 'Search text must be 2 to 80 characters' });
    const j = await railradar(`/lookup/search/trains?q=${encodeURIComponent(q)}&limit=5&lang=hi`);
    const data = Array.isArray(j.data) ? j.data : [];
    res.json({ results: data.map(x => ({ number: x.trainNumber || x.number || x.train_number, name: x.trainName || x.name || x.train_name })).filter(x => x.number) });
  } catch (e) { res.status(502).json({ error: e.message }); }
});
app.listen(PORT, () => console.log(`Train Saathi backend listening on ${PORT}`));
