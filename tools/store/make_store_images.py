"""Builds store screenshots and the Play feature graphic from raw simulator captures.

usage: make_store_images.py <raw_dir> <project_root>

<raw_dir> must contain iphone-1..4.png (1206x2622, iPhone 6.3" — the size App Store Connect asks for) and ipad-1..4.png
(2064x2752, iPad 13"), captured from the Release build with a 09:41 status bar.
Outputs:
  store-assets/app-store/screenshots/iphone-6.3/  captioned, 1206x2622
  store-assets/app-store/screenshots/ipad-13/     raw, 2064x2752
  store-assets/google-play/screenshots/           captioned, 1080x1920
  store-assets/google-play/feature-graphic.png    1024x500
"""
import os
import shutil
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

RAW, ROOT = sys.argv[1], sys.argv[2]
FONT = f"{ROOT}/shared/src/commonMain/composeResources/font/manrope.ttf"
MARK = f"{ROOT}/store-assets/brand/mark-transparent-1600.png"

BG = (11, 16, 32)  # #0B1020 brand navy
GOLD = (233, 185, 73)  # #E9B949 brand gold
TEXT = (244, 241, 234)
MUTED = (163, 169, 188)
FRAME = (40, 49, 82)  # #283152 outline

CAPTIONS = [
    ("Has altın fiyatı anlık", "Fiyat değiştiği an güncellenir"),
    ("Tüm sarrafiye tek ekranda", "Çeyrekten ata liraya, yeni ve eski"),
    ("Değerini hemen hesaplayın", "Gram ya da adet girin, tutarı görün"),
    ("Mağazamız bir dokunuş uzakta", "Arayın, WhatsApp'tan yazın, Instagram'da takip edin"),
]


def font(size, weight):
    f = ImageFont.truetype(FONT, size)
    try:
        f.set_variation_by_axes([weight])
    except OSError:
        pass
    return f


def rounded(im, radius):
    mask = Image.new("L", im.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, im.width - 1, im.height - 1), radius, fill=255)
    out = Image.new("RGBA", im.size)
    out.paste(im, (0, 0), mask)
    return out


def fitted(draw, text, size, weight, max_width):
    """Largest font not above [size] whose rendering of [text] fits in [max_width]."""
    while size > 10:
        f = font(size, weight)
        if draw.textlength(text, font=f) <= max_width:
            return f
        size -= 2
    return font(size, weight)


def centered(draw, y, text, f, fill, width):
    w = draw.textlength(text, font=f)
    draw.text(((width - w) / 2, y), text, font=f, fill=fill)


def captioned(shot_path, size, title, subtitle):
    W, H = size
    canvas = Image.new("RGB", size, BG)
    draw = ImageDraw.Draw(canvas)
    # soft gold glow behind the phone
    glow = Image.new("L", size, 0)
    ImageDraw.Draw(glow).ellipse((-W * 0.3, H * 0.25, W * 1.3, H * 1.25), fill=38)
    glow = glow.filter(ImageFilter.GaussianBlur(W * 0.12))
    canvas.paste(Image.new("RGB", size, GOLD), (0, 0), glow)
    draw = ImageDraw.Draw(canvas)

    title_f = fitted(draw, title, round(W * 0.066), 800, W * 0.9)
    sub_f = fitted(draw, subtitle, round(W * 0.036), 500, W * 0.9)
    top = round(H * 0.055)
    centered(draw, top, title, title_f, TEXT, W)
    centered(draw, top + round(W * 0.095), subtitle, sub_f, GOLD, W)

    shot = Image.open(shot_path).convert("RGB")
    avail_h = H - round(H * 0.19)
    scale = min(W * 0.84 / shot.width, avail_h / shot.height)
    shot = shot.resize((round(shot.width * scale), round(shot.height * scale)), Image.LANCZOS)
    shot = rounded(shot, round(shot.width * 0.075))
    border = Image.new("RGBA", (shot.width + 8, shot.height + 8))
    ImageDraw.Draw(border).rounded_rectangle(
        (0, 0, border.width - 1, border.height - 1), round(shot.width * 0.075) + 4, fill=FRAME + (255,)
    )
    x = (W - border.width) // 2
    y = H - border.height - round(H * 0.025) + round(H * 0.06)  # phone runs off the bottom edge slightly
    canvas.paste(border, (x, y), border)
    canvas.paste(shot, (x + 4, y + 4), shot)
    return canvas


def feature_graphic(path):
    W, H = 1024, 500
    canvas = Image.new("RGB", (W, H), BG)
    glow = Image.new("L", (W, H), 0)
    ImageDraw.Draw(glow).ellipse((-200, -300, 620, 800), fill=34)
    glow = glow.filter(ImageFilter.GaussianBlur(90))
    canvas.paste(Image.new("RGB", (W, H), GOLD), (0, 0), glow)
    mark = Image.open(MARK).convert("RGBA")
    mh = 230
    mark = mark.resize((round(mark.width * mh / mark.height), mh), Image.LANCZOS)
    canvas.paste(mark, (70, (H - mh) // 2), mark)
    draw = ImageDraw.Draw(canvas)
    x = 70 + mark.width + 50
    draw.text((x, 150), "Vatan Kuyumculuk", font=font(56, 800), fill=TEXT)
    draw.text((x, 225), "Canlı altın ve döviz fiyatları", font=font(32, 600), fill=GOLD)
    draw.text((x, 275), "Osmaniye", font=font(28, 500), fill=MUTED)
    canvas.save(path)


def main():
    ios_dir = f"{ROOT}/store-assets/app-store/screenshots/iphone-6.3"
    ipad_dir = f"{ROOT}/store-assets/app-store/screenshots/ipad-13"
    play_dir = f"{ROOT}/store-assets/google-play/screenshots"
    for d in (ios_dir, ipad_dir, play_dir):
        os.makedirs(d, exist_ok=True)

    for i, (title, sub) in enumerate(CAPTIONS, start=1):
        src = f"{RAW}/iphone-{i}.png"
        assert Image.open(src).size == (1206, 2622), src
        captioned(src, (1206, 2622), title, sub).save(f"{ios_dir}/{i:02d}.png")
        captioned(src, (1080, 1920), title, sub).save(f"{play_dir}/{i:02d}.png")

        ipad = f"{RAW}/ipad-{i}.png"
        assert Image.open(ipad).size == (2064, 2752), ipad
        shutil.copyfile(ipad, f"{ipad_dir}/{i:02d}.png")

    feature_graphic(f"{ROOT}/store-assets/google-play/feature-graphic.png")


main()
