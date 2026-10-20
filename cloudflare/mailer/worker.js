// POST {to, subject, text} -> sends a plain-text e-mail from panel@vatankuyumculuk.com.tr.
import { EmailMessage } from "cloudflare:email";

const FROM = "panel@vatankuyumculuk.com.tr";
const FROM_NAME = "Vatan Kuyumculuk Panel";

function b64(text) {
  let s = "";
  for (const b of new TextEncoder().encode(text)) s += String.fromCharCode(b);
  return btoa(s);
}

/** A minimal UTF-8 MIME message, for runtimes where send_email only accepts EmailMessage. */
function rawMessage(to, subject, text) {
  return [
    `From: =?UTF-8?B?${b64(FROM_NAME)}?= <${FROM}>`,
    `To: <${to}>`,
    `Subject: =?UTF-8?B?${b64(subject)}?=`,
    `Message-ID: <${crypto.randomUUID()}@vatankuyumculuk.com.tr>`,
    `Date: ${new Date().toUTCString()}`,
    "MIME-Version: 1.0",
    "Content-Type: text/plain; charset=UTF-8",
    "Content-Transfer-Encoding: base64",
    "",
    b64(text).replace(/.{76}/g, "$&\r\n"),
  ].join("\r\n");
}

export default {
  async fetch(request, env) {
    if (request.method !== "POST") return new Response(null, { status: 405 });
    const { to, subject, text } = await request.json();
    if (typeof to !== "string" || !to.includes("@") || typeof subject !== "string" || typeof text !== "string") {
      return new Response("bad request", { status: 400 });
    }
    try {
      await env.EMAIL.send({ to, from: { email: FROM, name: FROM_NAME }, subject, text });
    } catch (first) {
      try {
        await env.EMAIL.send(new EmailMessage(FROM, to, rawMessage(to, subject, text)));
      } catch (second) {
        console.error("send failed", String(first), String(second));
        return new Response("send failed", { status: 502 });
      }
    }
    return new Response("ok");
  },
};
