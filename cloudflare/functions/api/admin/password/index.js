// POST /api/admin/password {code, newPassword} -> {token}
// Sets a new password with an e-mailed code (see code.js). Every other session is signed out.
import { clearFailures, isJson, issueToken } from "../../../_lib/auth.js";
import { json } from "../../../_lib/config.js";
import { consumeCode, passwordProblem, setPassword } from "../../../_lib/password.js";

export async function onRequestPost({ request, env }) {
  if (!isJson(request)) return json({ error: "Geçersiz istek" }, { status: 415 });
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "Geçersiz istek" }, { status: 400 });
  }
  const newPassword = body?.newPassword;
  const problem = passwordProblem(newPassword);
  if (problem) return json({ error: problem }, { status: 422 });
  if (!(await consumeCode(env, String(body.code ?? "").trim()))) {
    return json({ error: "Kod hatalı veya süresi dolmuş. Yeni kod isteyin." }, { status: 401 });
  }
  await setPassword(env, newPassword);
  await clearFailures(request, env);
  return json({ token: await issueToken(env) }, { headers: { "Cache-Control": "no-store" } });
}
