// Sends the admin's codes through the vatan-mailer Worker (cloudflare/mailer), bound as MAILER.
// The recipient is the ADMIN_EMAIL secret; Cloudflare only delivers to addresses verified in Email Routing.

export class MailError extends Error {}

/** "erncanyildirim@gmail.com" -> "er…im@gmail.com", shown in the panel. */
export function maskEmail(address) {
  const [name, domain] = String(address).split("@");
  if (!domain) return "";
  return `${name.slice(0, 2)}…${name.length > 4 ? name.slice(-2) : ""}@${domain}`;
}

export async function sendCode(env, code, purpose) {
  if (!env.MAILER || !env.ADMIN_EMAIL) throw new MailError("E-posta gönderimi henüz ayarlanmadı");
  const action = purpose === "reset" ? "şifrenizi sıfırlamak" : "şifrenizi değiştirmek";
  const text = [
    `Vatan Kuyumculuk yönetim panelinde ${action} için doğrulama kodunuz:`,
    "",
    `    ${code}`,
    "",
    "Kod 10 dakika geçerlidir ve yalnızca bir kez kullanılabilir.",
    "Bu isteği siz yapmadıysanız bu e-postayı dikkate almayın; şifreniz değişmez.",
  ].join("\n");
  let res;
  try {
    res = await env.MAILER.fetch("https://mailer/send", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ to: env.ADMIN_EMAIL, subject: `Doğrulama kodu: ${code}`, text }),
    });
  } catch {
    res = null;
  }
  if (!res?.ok) throw new MailError("E-posta gönderilemedi, biraz sonra tekrar deneyin");
}
