// POST /api/admin/password/code: e-mails a 6-digit code for setting a new password.
//   {purpose: "change", currentPassword}  signed in, from the panel's password form
//   {purpose: "reset"}                    "Şifremi unuttum" on the login screen
import { isJson, isLocked, recordFailure, requireAdmin } from "../../../_lib/auth.js";
import { json } from "../../../_lib/config.js";
import { MailError, maskEmail, sendCode } from "../../../_lib/mail.js";
import { checkPassword, issueCode } from "../../../_lib/password.js";

const NO_STORE = { "Cache-Control": "no-store" };

export async function onRequestPost({ request, env }) {
  if (!isJson(request)) return json({ error: "Geçersiz istek" }, { status: 415 });
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "Geçersiz istek" }, { status: 400 });
  }
  if (!env.MAILER || !env.ADMIN_EMAIL) {
    return json({ error: "E-posta gönderimi henüz ayarlanmadı" }, { status: 503, headers: NO_STORE });
  }
  const purpose = body?.purpose === "reset" ? "reset" : "change";
  if (purpose === "change") {
    const denied = await requireAdmin(request, env);
    if (denied) return denied;
    if (await isLocked(request, env)) {
      return json({ error: "Çok fazla hatalı deneme. 15 dakika sonra tekrar deneyin." }, { status: 429 });
    }
    if (!(await checkPassword(env, String(body.currentPassword ?? "")))) {
      await recordFailure(request, env);
      return json({ error: "Mevcut şifre hatalı" }, { status: 401 });
    }
  }
  const issued = await issueCode(env);
  if (issued.waitSeconds) {
    const wait = issued.waitSeconds > 3600 ? "yarın" : `${issued.waitSeconds} saniye sonra`;
    return json({ error: `Yeni kod için ${wait} tekrar deneyin` }, { status: 429, headers: NO_STORE });
  }
  try {
    await sendCode(env, issued.code, purpose);
  } catch (e) {
    if (e instanceof MailError) return json({ error: e.message }, { status: 503, headers: NO_STORE });
    throw e;
  }
  return json({ sentTo: maskEmail(env.ADMIN_EMAIL) }, { headers: NO_STORE });
}
