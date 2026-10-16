// GET/PUT /api/admin/config: read and save the configuration (admin only).
import { isJson, requireAdmin } from "../../_lib/auth.js";
import { json, readConfig, sanitizeConfig, ValidationError, writeConfig } from "../../_lib/config.js";

const NO_STORE = { "Cache-Control": "no-store", "X-Robots-Tag": "noindex" };

export async function onRequestGet({ request, env }) {
  const denied = await requireAdmin(request, env);
  if (denied) return denied;
  return json((await readConfig(env)) ?? { schema: 1, version: 0 }, { headers: NO_STORE });
}

export async function onRequestPut({ request, env }) {
  const denied = await requireAdmin(request, env);
  if (denied) return denied;
  if (!isJson(request)) return json({ error: "Geçersiz istek" }, { status: 415 });
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "Geçersiz istek" }, { status: 400 });
  }
  const previous = await readConfig(env);
  // Reject saves made from a stale copy so two open admin tabs cannot overwrite each other.
  if (typeof body.version === "number" && previous && body.version !== previous.version) {
    return json({ error: "Ayarlar başka bir yerde değiştirilmiş. Sayfayı yenileyip tekrar deneyin." }, { status: 409 });
  }
  try {
    const doc = await writeConfig(env, sanitizeConfig(body), previous);
    return json(doc, { headers: NO_STORE });
  } catch (e) {
    if (e instanceof ValidationError) return json({ error: e.message }, { status: 422 });
    throw e;
  }
}
