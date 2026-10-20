import assert from "node:assert/strict";
import { test } from "node:test";

import { issueToken, verifyToken } from "../functions/_lib/auth.js";
import { sanitizeConfig, ValidationError } from "../functions/_lib/config.js";
import * as adminConfig from "../functions/api/admin/config.js";
import * as login from "../functions/api/admin/login.js";
import * as password from "../functions/api/admin/password/index.js";
import * as passwordCode from "../functions/api/admin/password/code.js";
import * as publicConfig from "../functions/api/config.js";

class MemoryKV {
  map = new Map();
  async get(k) { return this.map.get(k) ?? null; }
  async put(k, v) { this.map.set(k, v); }
  async delete(k) { this.map.delete(k); }
}

const env = () => ({ CONFIG: new MemoryKV(), ADMIN_PASSWORD: "dogru-sifre", SESSION_SECRET: "test-secret" });

function req(path, { method = "GET", body, token, headers = {} } = {}) {
  const h = new Headers(headers);
  if (token) h.set("Authorization", `Bearer ${token}`);
  if (body !== undefined) h.set("Content-Type", "application/json");
  return new Request(`https://example.test${path}`, { method, headers: h, body: body === undefined ? undefined : JSON.stringify(body) });
}

test("sanitize keeps valid overrides and drops no-op entries", () => {
  const clean = sanitizeConfig({
    products: {
      CEYREK_YENI: { mode: "fixed", sell: "10.900", buy: null },
      ALTIN: { mode: "adjust", buyAdj: "-38", sellAdj: 50, adjUnit: "amount" },
      USDTRY: { mode: "live", visible: true },
    },
  });
  assert.deepEqual(Object.keys(clean.products).sort(), ["ALTIN", "CEYREK_YENI"]);
  assert.equal(clean.products.CEYREK_YENI.sell, 10.9);
  assert.equal(clean.products.ALTIN.buyAdj, -38);
  assert.equal(clean.feed.url, "wss://hrmsocketonly.haremaltin.com");
});

test("sanitize rejects bad input with a Turkish message", () => {
  assert.throws(() => sanitizeConfig({ products: { "bad code": {} } }), ValidationError);
  assert.throws(() => sanitizeConfig({ products: { ALTIN: { mode: "adjust", adjUnit: "percent", sellAdj: 80 } } }), ValidationError);
  assert.throws(() => sanitizeConfig({ store: { openingHours: [{ days: [1], open: "19:00", close: "09:00" }] } }), ValidationError);
  assert.throws(() => sanitizeConfig({ store: { phone: "abc" } }), ValidationError);
  assert.throws(() => sanitizeConfig({ feed: { url: "http://evil.example" } }), ValidationError);
});

test("sanitize normalizes leftovers and empty modes", () => {
  const clean = sanitizeConfig({
    store: { name: "Vatan", city: "Osmaniye", mapsQuery: "", openingHours: [{ days: [1.5, 2, 9], open: "09:00", close: "19:00" }] },
    products: {
      ALTIN: { mode: "fixed" },
      AYAR22: { mode: "fixed", sell: 6000, buyAdj: 999, adjUnit: "percent" },
      KULCEALTIN: { mode: "adjust", buyAdj: 0, sellAdj: 0 },
    },
  });
  assert.equal(clean.products.ALTIN, undefined);
  assert.equal(clean.products.KULCEALTIN, undefined);
  assert.deepEqual(clean.products.AYAR22, { mode: "fixed", visible: true, buy: null, sell: 6000, buyAdj: 0, sellAdj: 0, adjUnit: "amount" });
  assert.deepEqual(clean.store.openingHours[0].days, [2]);
  assert.equal(clean.store.mapsQuery, "Vatan Osmaniye");
});

test("login requires JSON and weak ETags still match", async () => {
  const e = env();
  const form = await login.onRequestPost({
    request: new Request("https://example.test/api/admin/login", { method: "POST", headers: { "Content-Type": "text/plain" }, body: '{"password":"x"}' }),
    env: e,
  });
  assert.equal(form.status, 415);
  const weak = await publicConfig.onRequestGet({ request: req("/api/config", { headers: { "If-None-Match": 'W/"v0"' } }), env: e });
  assert.equal(weak.status, 304);
});

test("tokens verify and expire", async () => {
  const e = env();
  const t = await issueToken(e, 1_000);
  assert.equal(await verifyToken(e, t, 2_000), true);
  assert.equal(await verifyToken(e, t, 1_000 + 13 * 3600 * 1000), false);
  assert.equal(await verifyToken({ ...e, SESSION_SECRET: "other" }, t, 2_000), false);
  assert.equal(await verifyToken(e, t.slice(0, -2) + "xx", 2_000), false);
});

test("login, save and public read round trip", async () => {
  const e = env();
  const empty = await publicConfig.onRequestGet({ request: req("/api/config"), env: e });
  assert.equal(empty.status, 200);
  assert.equal((await empty.json()).version, 0);

  const bad = await login.onRequestPost({ request: req("/api/admin/login", { method: "POST", body: { password: "x" } }), env: e });
  assert.equal(bad.status, 401);

  const ok = await login.onRequestPost({ request: req("/api/admin/login", { method: "POST", body: { password: "dogru-sifre" } }), env: e });
  const { token } = await ok.json();
  assert.ok(token);

  const denied = await adminConfig.onRequestPut({ request: req("/api/admin/config", { method: "PUT", body: {} }), env: e });
  assert.equal(denied.status, 401);

  const put = await adminConfig.onRequestPut({
    request: req("/api/admin/config", {
      method: "PUT",
      token,
      body: { version: 0, announcement: { enabled: true, text: "Bayramda kapalıyız" } },
    }),
    env: e,
  });
  assert.equal(put.status, 200);
  const doc = await put.json();
  assert.equal(doc.version, 1);

  const stale = await adminConfig.onRequestPut({
    request: req("/api/admin/config", { method: "PUT", token, body: { version: 0 } }),
    env: e,
  });
  assert.equal(stale.status, 409);

  const pub = await publicConfig.onRequestGet({ request: req("/api/config"), env: e });
  assert.equal(pub.headers.get("ETag"), '"v1"');
  assert.equal((await pub.json()).announcement.text, "Bayramda kapalıyız");

  const notModified = await publicConfig.onRequestGet({ request: req("/api/config", { headers: { "If-None-Match": '"v1"' } }), env: e });
  assert.equal(notModified.status, 304);
});

test("login locks after repeated failures", async () => {
  const e = env();
  for (let i = 0; i < 8; i++) {
    await login.onRequestPost({ request: req("/api/admin/login", { method: "POST", body: { password: "x" } }), env: e });
  }
  const locked = await login.onRequestPost({ request: req("/api/admin/login", { method: "POST", body: { password: "dogru-sifre" } }), env: e });
  assert.equal(locked.status, 429);
});

test("password change with an e-mailed code", async () => {
  const sent = [];
  const e = {
    ...env(),
    ADMIN_EMAIL: "sahip@example.com",
    MAILER: { fetch: async (_url, init) => { sent.push(JSON.parse(init.body)); return new Response("ok"); } },
  };
  const loginWith = (password) => login.onRequestPost({ request: req("/api/admin/login", { method: "POST", body: { password } }), env: e });
  const { token } = await (await loginWith("dogru-sifre")).json();

  const noAuth = await passwordCode.onRequestPost({ request: req("/api/admin/password/code", { method: "POST", body: { purpose: "change", currentPassword: "dogru-sifre" } }), env: e });
  assert.equal(noAuth.status, 401);
  const wrong = await passwordCode.onRequestPost({ request: req("/api/admin/password/code", { method: "POST", token, body: { purpose: "change", currentPassword: "x" } }), env: e });
  assert.equal(wrong.status, 401);
  assert.equal(sent.length, 0);

  const asked = await passwordCode.onRequestPost({ request: req("/api/admin/password/code", { method: "POST", token, body: { purpose: "change", currentPassword: "dogru-sifre" } }), env: e });
  assert.equal(asked.status, 200);
  assert.equal((await asked.json()).sentTo, "sa…ip@example.com");
  assert.equal(sent[0].to, "sahip@example.com");
  const code = sent[0].subject.match(/\d{6}/)[0];

  const tooSoon = await passwordCode.onRequestPost({ request: req("/api/admin/password/code", { method: "POST", body: { purpose: "reset" } }), env: e });
  assert.equal(tooSoon.status, 429);

  const set = (body) => password.onRequestPost({ request: req("/api/admin/password", { method: "POST", body }), env: e });
  assert.equal((await set({ code, newPassword: "kisa" })).status, 422);
  assert.equal((await set({ code: code === "000000" ? "111111" : "000000", newPassword: "yeni-sifre-123" })).status, 401);
  const ok = await set({ code, newPassword: "yeni-sifre-123" });
  assert.equal(ok.status, 200);
  const { token: fresh } = await ok.json();
  assert.equal((await set({ code, newPassword: "baska-sifre-1" })).status, 401); // a code works once

  assert.equal(await verifyToken(e, token), false); // old sessions are signed out
  assert.equal(await verifyToken(e, fresh), true);
  assert.equal((await loginWith("dogru-sifre")).status, 401);
  assert.equal((await loginWith("yeni-sifre-123")).status, 200);
});

test("a code stops working after five wrong tries", async () => {
  const sent = [];
  const e = { ...env(), ADMIN_EMAIL: "a@b.co", MAILER: { fetch: async (_u, init) => { sent.push(JSON.parse(init.body)); return new Response("ok"); } } };
  await passwordCode.onRequestPost({ request: req("/api/admin/password/code", { method: "POST", body: { purpose: "reset" } }), env: e });
  const code = sent[0].subject.match(/\d{6}/)[0];
  const other = code === "000000" ? "111111" : "000000";
  for (let i = 0; i < 5; i++) {
    await password.onRequestPost({ request: req("/api/admin/password", { method: "POST", body: { code: other, newPassword: "yeni-sifre-123" } }), env: e });
  }
  const late = await password.onRequestPost({ request: req("/api/admin/password", { method: "POST", body: { code, newPassword: "yeni-sifre-123" } }), env: e });
  assert.equal(late.status, 401);
});

test("password codes need the mailer to be set up", async () => {
  const res = await passwordCode.onRequestPost({ request: req("/api/admin/password/code", { method: "POST", body: { purpose: "reset" } }), env: env() });
  assert.equal(res.status, 503);
});
