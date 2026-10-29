# Vatan Kuyumculuk Osmaniye

Osmaniye'deki Vatan Kuyumculuk için canlı altın ve döviz fiyatı uygulaması: Android, iOS ve web
(Kotlin Multiplatform + Compose Multiplatform). Üyelik, reklam ve ücretli servis yoktur.

## Nasıl çalışır?

- **Canlı fiyatlar:** Her cihaz Harem Altın'ın herkese açık Socket.IO yayınına (`wss://hrmsocketonly.haremaltin.com`)
  doğrudan bağlanır. Bağlantı yalnızca uygulama açıkken tutulur, kopunca kendiliğinden yeniden kurulur, son fiyatlar
  cihazda saklanır. Kod: `shared/.../data/harem/`, `shared/.../data/market/`.
- **Mağaza TV ekranı:** `/tv/` adresi televizyonda tam ekran açılır; ürünler ve ekrana özel farklar admin panelindeki
  "Mağaza ekranı" sekmesinden seçilir.
- **Mağaza ayarları:** Ürün bazında fark, sabit fiyat, gizleme, duyuru ve iletişim bilgileri `/api/config` belgesinden
  okunur. Şimdilik bu belge `site/api/config` dosyasıdır; alan adı alınınca Cloudflare'deki `/admin/` paneline geçilir.
  Ayrıntı: `docs/api-contract.md`.
- **Web sitesi:** https://erncanyildirim.github.io/Vatan-Kuyumculuk/ (GitHub Pages, ücretsiz; gizlilik `/gizlilik/`,
  destek `/destek/`). Yayın ve alan adına geçiş: `docs/yayin.md`.
- **Mağaza yayını:** Metinler, formlar, ekran görüntüleri ve kontrol listesi: `store-assets/`.

## Klasörler

| Klasör | İçerik |
| --- | --- |
| `shared/` | Ortak kod: veri katmanı, fiyat hesaplama, tüm ekranlar |
| `androidApp/` | Android uygulaması |
| `iosApp/` | iOS uygulaması (Xcode projesi) |
| `webApp/` | Web uygulaması; `resources/` altında gizlilik, destek ve admin sayfaları |
| `site/` | GitHub Pages'e eklenen dosyalar (`api/config`) |
| `cloudflare/` | Alan adı sonrası için `/api/config` ve admin API'si (Pages Functions) |
| `store-assets/` | Logo, simgeler, ekran görüntüleri, mağaza metinleri |
| `tools/` | Logo/simge üretimi, ekran görüntüsü, metin uzunluğu ve iOS yayın araçları |

## Komutlar

```bash
./gradlew :androidApp:assembleDebug                         # Android
./gradlew :androidApp:bundleRelease                         # Play Store AAB (imza: ~/.vatan-signing/keystore.properties)
./gradlew :webApp:composeCompatibilityBrowserDistribution   # Web (wasm + js yedeği)
./gradlew :shared:testAndroidHostTest :shared:iosSimulatorArm64Test
cd cloudflare && npm test                                   # API testleri
python3 tools/store/check_lengths.py                        # mağaza metin uzunlukları
python3 tools/branding/vatan_brand.py                       # logo ve tüm simgeleri yeniden üretir
tools/ios/release.sh                                        # iOS arşivle, imzala, App Store Connect'e yükle
```

## İmza

- **Android:** `~/.vatan-signing/keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`)
  veya `VATAN_SIGNING_PROPERTIES` ortam değişkeni. Anahtar ve şifreler depoya girmez; yedeği ayrıca saklanmalıdır.
- **iOS:** Takım 4BQQTXY4NF, otomatik imzalama. Xcode'da bu takımın Apple ID'si ile oturum açık olmalıdır.
