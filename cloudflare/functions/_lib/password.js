// The admin password.
// Until the first change the password is the ADMIN_PASSWORD secret; after that it is a PBKDF2 hash
// in KV, which takes precedence. Deleting the KV key falls back to ADMIN_PASSWORD again.

const PASSWORD_KEY = "admin-password";
const ITERATIONS = 100_000; // the most PBKDF2 iterations Workers allow
export const MIN_PASSWORD_LENGTH = 8;

const enc = new TextEncoder();

const toHex = (bytes) => [...new Uint8Array(bytes)].map((b) => b.toString(16).padStart(2, "0")).join("");
const fromHex = (hex) => Uint8Array.from(hex.match(/../g) ?? [], (h) => parseInt(h, 16));

/** Constant-time comparison of two strings via their SHA-256 digests. */
export async function safeEqual(a, b) {
  const [da, db] = await Promise.all([
    crypto.subtle.digest("SHA-256", enc.encode(a)),
    crypto.subtle.digest("SHA-256", enc.encode(b)),
  ]);
  const x = new Uint8Array(da);
  const y = new Uint8Array(db);
  let diff = 0;
  for (let i = 0; i < x.length; i++) diff |= x[i] ^ y[i];
  return diff === 0;
}

async function pbkdf2(password, salt, iterations) {
  const key = await crypto.subtle.importKey("raw", enc.encode(password), "PBKDF2", false, ["deriveBits"]);
  const bits = await crypto.subtle.deriveBits({ name: "PBKDF2", hash: "SHA-256", salt, iterations }, key, 256);
  return toHex(bits);
}

// KV errors are not swallowed here: falling back to ADMIN_PASSWORD while KV is down would let the
// old password back in.
async function readJson(env, key) {
  const text = await env.CONFIG.get(key);
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

/** Changes whenever the password changes; the session key includes it, so a change signs out every session. */
export async function passwordStamp(env) {
  return (await readJson(env, PASSWORD_KEY))?.hash ?? env.ADMIN_PASSWORD ?? "";
}

export async function checkPassword(env, password) {
  const record = await readJson(env, PASSWORD_KEY);
  if (!record) return Boolean(env.ADMIN_PASSWORD) && safeEqual(password, env.ADMIN_PASSWORD);
  return safeEqual(await pbkdf2(password, fromHex(record.salt), record.iterations), record.hash);
}

/** Returns a Turkish error message, or null when [password] is acceptable. */
export function passwordProblem(password) {
  if (typeof password !== "string" || password.length < MIN_PASSWORD_LENGTH) {
    return `Yeni şifre en az ${MIN_PASSWORD_LENGTH} karakter olmalı`;
  }
  if (password.length > 128) return "Yeni şifre en fazla 128 karakter olabilir";
  if (password.trim() !== password) return "Şifre boşlukla başlayıp bitemez";
  return null;
}

export async function setPassword(env, password, now = Date.now()) {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const hash = await pbkdf2(password, salt, ITERATIONS);
  const record = { salt: toHex(salt), iterations: ITERATIONS, hash, changedAt: new Date(now).toISOString() };
  await env.CONFIG.put(PASSWORD_KEY, JSON.stringify(record));
}
