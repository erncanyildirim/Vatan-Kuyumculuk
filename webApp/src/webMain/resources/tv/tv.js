// Shop TV screen: live Harem Altın prices + the shop's own TV differences from /api/config (tv.items).
// Socket.IO over a plain WebSocket as in admin.js / HaremProtocol.kt.

const DEFAULT_FEED = "wss://hrmsocketonly.haremaltin.com";
const STALE_MS = 45000;

// Shown until the shop picks its own list in the admin panel (Mağaza ekranı sekmesi).
const DEFAULT_ITEMS = [
  ["ALTIN", "HAS"], ["AYAR22", "BİLEZİK"], ["CEYREK_YENI", "ÇEYREK"], ["YARIM_YENI", "YARIM"], ["TEK_YENI", "TAM"],
  ["KULCEALTIN", "GRAM"], ["AYAR14", "14 AYAR"], ["GUMUSTRY", "GÜMÜŞ"], ["USDTRY", "DOLAR"], ["EURTRY", "EURO"],
].map(([code, label]) => ({ code, label, buyAdj: 0, sellAdj: 0, adjUnit: "amount" }));

const $ = (id) => document.getElementById(id);
const catalog = {};
const live = {};
let config = { store: {}, feed: {}, tv: null };
let lastTick = 0;
let socket = null;
let retry = 0;
let signature = "";

function items() {
  const list = config.tv && Array.isArray(config.tv.items) ? config.tv.items : DEFAULT_ITEMS;
  return list.filter((it) => catalog[it.code]);
}

function adjust(value, adj, percent) {
  if (value === null || value === undefined) return null;
  const out = percent ? value * (1 + adj / 100) : value + adj;
  return out > 0 ? out : null;
}

// A TV shows at most two decimals (coins none), however many the catalog lists for the app.
const tvDecimals = (p) => Math.min(p.decimals, 2);

function fmt(value, decimals) {
  if (value === null || !Number.isFinite(value)) return "—";
  return value.toLocaleString("tr-TR", { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
}

function render() {
  const list = items();
  const grid = $("grid");
  const sig = JSON.stringify(list);
  if (sig !== signature) {
    signature = sig;
    grid.textContent = "";
    if (!list.length) {
      const p = document.createElement("p");
      p.className = "empty";
      p.textContent = "Gösterilecek ürün seçilmedi";
      grid.append(p);
    }
    const rows = list.length > 10 ? 3 : 2;
    const cols = Math.max(1, Math.ceil(list.length / rows));
    grid.style.setProperty("--cols", cols);
    grid.style.setProperty("--rows", Math.ceil(list.length / cols) || 1);
    // Smaller type when more columns share the width.
    const k = Math.min(1, 5 / cols);
    grid.style.setProperty("--title", `${(3.2 * k).toFixed(2)}vh`);
    grid.style.setProperty("--big", `${(6.4 * Math.max(k, 0.7)).toFixed(2)}vh`);
    grid.style.setProperty("--small", `${(2.3 * Math.max(k, 0.8)).toFixed(2)}vh`);
    for (const it of list) {
      const p = catalog[it.code];
      const tile = document.createElement("section");
      tile.className = "tile";
      tile.dataset.code = it.code;
      const h = document.createElement("h2");
      h.textContent = it.label || p.name;
      if (p.group === "CURRENCY") h.className = "fx";
      tile.append(h);
      tile.insertAdjacentHTML("beforeend",
        '<div class="body"><div class="sell">—</div><div class="buy"></div><div class="chg"></div></div>');
      grid.append(tile);
    }
  }
  for (const it of list) update(it);
}

function update(it) {
  const tile = document.querySelector(`.tile[data-code="${it.code}"]`);
  if (!tile) return;
  const p = catalog[it.code];
  const raw = live[it.code];
  const pct = it.adjUnit === "percent";
  const sell = raw ? adjust(raw.sell, it.sellAdj || 0, pct) : null;
  const buy = raw ? adjust(raw.buy, it.buyAdj || 0, pct) : null;
  const sellEl = tile.querySelector(".sell");
  const text = fmt(sell ?? buy, tvDecimals(p));
  if (sellEl.textContent !== text) {
    const was = Number(sellEl.dataset.v);
    const now = sell ?? buy;
    sellEl.textContent = text;
    sellEl.dataset.v = now ?? "";
    if (now !== null && was) {
      sellEl.classList.toggle("up", now > was);
      sellEl.classList.toggle("down", now < was);
      clearTimeout(sellEl._t);
      sellEl._t = setTimeout(() => sellEl.classList.remove("up", "down"), 2500);
    }
  }
  tile.querySelector(".buy").textContent = buy !== null && sell !== null ? `Alış ${fmt(buy, tvDecimals(p))}` : "";
  const chg = tile.querySelector(".chg");
  if (raw && raw.close && raw.sell) {
    const c = ((raw.sell - raw.close) / raw.close) * 100;
    const flat = Math.abs(c) < 0.005;
    chg.className = `chg ${flat ? "" : c > 0 ? "up" : "down"}`;
    chg.textContent = `${flat ? "" : c > 0 ? "▲ " : "▼ "}%${Math.abs(c).toLocaleString("tr-TR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  } else {
    chg.textContent = "";
  }
}

function applyStore() {
  const s = config.store || {};
  const name = (s.name || "Vatan Kuyumculuk").trim();
  const i = name.indexOf(" ");
  const shop = $("shop-name");
  shop.textContent = "";
  if (i > 0) {
    shop.append(name.slice(0, i) + " ");
    const b = document.createElement("b");
    b.textContent = name.slice(i + 1);
    shop.append(b);
  } else {
    shop.textContent = name;
  }
  document.title = `Mağaza Ekranı · ${name}`;
  const phone = (s.phone || "+905054348592").replace(/^\+90\s?/, "0").replace(/\s/g, "");
  $("contact").textContent = phone.length === 11 ? `Bilgi: ${phone.slice(0, 4)} ${phone.slice(4, 7)} ${phone.slice(7, 9)} ${phone.slice(9)}` : phone ? `Bilgi: ${phone}` : "";
}

function numOrNull(v) {
  const n = typeof v === "string" ? Number(v.replace(",", ".")) : v;
  return Number.isFinite(n) && n > 0 ? n : null;
}

function connect() {
  if (socket) return;
  const base = (config.feed?.url || DEFAULT_FEED).replace(/^https:/, "wss:").replace(/\/$/, "");
  let ws;
  try {
    ws = new WebSocket(`${base}/socket.io/?EIO=4&transport=websocket`);
  } catch {
    setTimeout(connect, 5000);
    return;
  }
  socket = ws;
  ws.onmessage = (e) => {
    const d = String(e.data);
    if (d[0] === "0") ws.send("40");
    else if (d === "2") ws.send("3");
    else if (d.startsWith("42")) {
      let name, payload;
      try { [name, payload] = JSON.parse(d.slice(2)); } catch { return; }
      if (name !== "price_changed") return;
      retry = 0;
      lastTick = Date.now();
      for (const [code, q] of Object.entries(payload.data || {})) {
        live[code] = { buy: numOrNull(q.alis), sell: numOrNull(q.satis), close: numOrNull(q.kapanis) };
      }
      render();
    }
  };
  ws.onclose = () => {
    socket = null;
    setTimeout(connect, Math.min(30000, 1000 * 2 ** retry++));
  };
  ws.onerror = () => ws.close();
}

async function loadConfig() {
  try {
    const res = await fetch("../api/config", { cache: "no-cache" });
    if (res.ok) {
      config = await res.json();
      applyStore();
      render();
    }
  } catch { /* keep the last known settings */ }
}

function tick() {
  const now = new Date();
  $("clock").textContent = now.toLocaleString("tr-TR", {
    timeZone: "Europe/Istanbul", day: "numeric", month: "short", weekday: "long",
    hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false,
  });
  const ok = Date.now() - lastTick < STALE_MS;
  const badge = $("status");
  badge.className = `badge ${ok ? "" : "off"}`;
  badge.lastElementChild.textContent = ok ? "CANLI" : lastTick ? "BAĞLANTI YOK" : "BAĞLANIYOR";
}

async function init() {
  try {
    const cat = await fetch("../catalog.json").then((r) => r.json());
    for (const p of cat.products) catalog[p.code] = p;
  } catch { /* retried by the reload below */ setTimeout(() => location.reload(), 10000); return; }
  await loadConfig();
  applyStore();
  render();
  connect();
  tick();
  setInterval(tick, 1000);
  setInterval(loadConfig, 60000);
  // A TV that sits on one page for days: reload once a night to drop leaked state.
  setInterval(() => { if (new Date().getHours() === 4 && Date.now() - lastTick < STALE_MS) location.reload(); }, 3600000);
  try { await navigator.wakeLock?.request("screen"); } catch { /* not supported or denied */ }
}

init();
