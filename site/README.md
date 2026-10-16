# GitHub Pages ek dosyaları

`.github/workflows/pages.yml` web uygulamasını derler, bu klasördeki dosyaları üstüne kopyalar ve
https://erncanyildirim.github.io/Vatan-Kuyumculuk/ adresinde yayınlar.

- `api/config`: uygulamaların okuduğu mağaza ayarları (sözleşme: `docs/api-contract.md`).
  Adres, çalışma saatleri, duyuru veya fiyat ayarı değişince bu dosyayı düzenleyip `version`
  değerini bir artırın; `main` dalına gönderince birkaç dakika içinde uygulamalara yansır.

Admin paneli Cloudflare gerektirdiği için bu sitede yayınlanmaz (alan adı alınınca bkz. `docs/yayin.md`).
