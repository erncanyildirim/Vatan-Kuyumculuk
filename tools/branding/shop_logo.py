"""Builds the website and in-app logo files from the shop's own round logo.

usage: python3 tools/branding/shop_logo.py            (run from the project root)
needs: pip install pillow

Source: store-assets/brand/shop-logo-source.png, the shop's logo as Eren supplied it (a screenshot
of the round Instagram picture, with background in the corners). The gold ring is found by hand:
centre (428.5, 424.5), outer radius 425.5 px. Everything outside the ring is made transparent.
Small icons (favicon) show only the crown emblem, since the lettering is unreadable below 64 px.
"""
import os

from PIL import Image, ImageDraw

ROOT = os.getcwd()
BRAND = f"{ROOT}/store-assets/brand"
CREAM = (250, 248, 240)
GOLD = (196, 158, 74)
RING = (428.5, 424.5, 425.5)  # centre x, centre y, outer radius in the source
CROWN = (255, 140, 600, 445)  # crown emblem box in the cropped logo


def circle_mask(size, scale=4):
    m = Image.new("L", (size * scale, size * scale), 0)
    ImageDraw.Draw(m).ellipse((0, 0, size * scale - 1, size * scale - 1), fill=255)
    return m.resize((size, size), Image.LANCZOS)


def round_logo():
    src = Image.open(f"{BRAND}/shop-logo-source.png").convert("RGBA")
    cx, cy, r = RING
    size = int(2 * r) + 1
    out = Image.new("RGBA", (size, size), CREAM + (255,))
    out.paste(src, (int(round(r - cx)), int(round(r - cy))))
    out.putalpha(circle_mask(size))
    return out


def crown_badge(px, logo):
    """Crown emblem on a cream disc with a gold ring, for favicons."""
    s = px * 8
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse((0, 0, s - 1, s - 1), fill=GOLD)
    ring = max(1, s // 14)
    d.ellipse((ring, ring, s - 1 - ring, s - 1 - ring), fill=CREAM)
    crown = logo.crop(CROWN).convert("RGB")
    w = int(s * 0.74)
    crown = crown.resize((w, int(w * crown.height / crown.width)), Image.LANCZOS)
    im.paste(crown, ((s - crown.width) // 2, (s - crown.height) // 2))
    im.putalpha(circle_mask(s))
    return im.resize((px, px), Image.LANCZOS)


def on_square(px, logo, scale, bg=CREAM):
    im = Image.new("RGBA", (px, px), bg + (255,))
    n = int(px * scale)
    im.alpha_composite(logo.resize((n, n), Image.LANCZOS), ((px - n) // 2, (px - n) // 2))
    return im


def save(im, path, **kw):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path, **kw)
    print("wrote", os.path.relpath(path, ROOT))


def main():
    logo = round_logo()
    save(logo, f"{BRAND}/shop-logo-round.png")
    save(logo.resize((512, 512), Image.LANCZOS), f"{ROOT}/shared/src/commonMain/composeResources/drawable/logo_shop.png")

    web = f"{ROOT}/webApp/src/webMain/resources"
    for px in (192, 512):
        save(logo.resize((px, px), Image.LANCZOS), f"{web}/icons/icon-{px}.png")
    save(on_square(512, logo, 0.8), f"{web}/icons/icon-maskable-512.png")  # inside the 80% safe circle
    save(on_square(180, logo, 1.0).convert("RGB"), f"{web}/apple-touch-icon.png")
    fav = [crown_badge(s, logo) for s in (16, 32, 48)]
    save(fav[2], f"{web}/favicon-48.png")
    save(fav[2], f"{web}/favicon.ico", format="ICO", sizes=[(16, 16), (32, 32), (48, 48)], append_images=fav[:2])


main()
