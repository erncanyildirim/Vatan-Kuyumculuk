// POST /api/admin/password {currentPassword, newPassword} -> {token}
// Changes the admin password from the panel. Every other session is signed out.
import { isJson, isLocked, issueToken, recordFailure, requireAdmin } from "../../_lib/auth.js";
import { json } from "../../_lib/config.js";
import { checkPassword, passwordProblem, setPassword } from "../../_lib/password.js";

export async function onRequestPost({ request, env }) {
  const denied = await requireAdmin(request, env);
  if (denied) return denied;
  if (!isJson(request)) return json({ error: "Geçersiz istek" }, { status: 415 });
  if (await isLocked(request, env)) {
    return json({ error: "Çok fazla hatalı deneme. 15 dakika sonra tekrar deneyin." }, { status: 429 });
  }
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "Geçersiz istek" }, { status: 400 });
  }
  if (!(await checkPassword(env, String(body?.currentPassword ?? "")))) {
    await recordFailure(request, env);
    return json({ error: "Mevcut şifre hatalı" }, { status: 403 });
  }
  const problem = passwordProblem(body.newPassword);
  if (problem) return json({ error: problem }, { status: 422 });
  await setPassword(env, body.newPassword);
  return json({ token: await issueToken(env) }, { headers: { "Cache-Control": "no-store" } });
}
