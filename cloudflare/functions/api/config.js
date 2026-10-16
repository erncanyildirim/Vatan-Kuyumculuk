// GET /api/config: the public configuration read by the Android, iOS and web apps.
import { json, readConfig } from "../_lib/config.js";

export async function onRequestGet({ request, env }) {
  const doc = (await readConfig(env)) ?? { schema: 1, version: 0 };
  const etag = `"v${doc.version ?? 0}"`;
  const headers = {
    ETag: etag,
    "Cache-Control": "no-cache",
    "Access-Control-Allow-Origin": "*",
    "X-Robots-Tag": "noindex",
  };
  // Cloudflare may weaken the ETag (W/"v3") when it compresses the response; compare the bare value.
  const tags = (request.headers.get("If-None-Match") || "").split(",").map((t) => t.trim().replace(/^W\//, ""));
  if (tags.includes(etag)) {
    return new Response(null, { status: 304, headers });
  }
  return json(doc, { headers });
}

export function onRequestOptions() {
  return new Response(null, {
    status: 204,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, OPTIONS",
      "Access-Control-Allow-Headers": "If-None-Match",
      "Access-Control-Max-Age": "86400",
    },
  });
}
