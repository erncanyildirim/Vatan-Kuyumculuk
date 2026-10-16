// POST /api/admin/login {password} -> {token}
import { clearFailures, isJson, isLocked, issueToken, recordFailure, safeEqual } from "../../_lib/auth.js";
import { json } from "../../_lib/config.js";

export async function onRequestPost({ request, env }) {
  if (!env.ADMIN_PASSWORD) return json({ error: "ADMIN_PASSWORD tanımlı değil" }, { status: 503 });
  if (!isJson(request)) return json({ error: "Geçersiz istek" }, { status: 415 });
  if (await isLocked(request, env)) {
    return json({ error: "Çok fazla hatalı deneme. 15 dakika sonra tekrar deneyin." }, { status: 429 });
  }
  let password = "";
  try {
    password = String((await request.json()).password ?? "");
  } catch {
    return json({ error: "Geçersiz istek" }, { status: 400 });
  }
  if (!(await safeEqual(password, env.ADMIN_PASSWORD))) {
    await recordFailure(request, env);
    return json({ error: "Şifre hatalı" }, { status: 401 });
  }
  await clearFailures(request, env);
  return json({ token: await issueToken(env) }, { headers: { "Cache-Control": "no-store" } });
}
