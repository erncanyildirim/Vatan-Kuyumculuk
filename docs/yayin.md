# Web sitesi yayını

## Şimdi: GitHub Pages (ücretsiz, alan adı gerekmez)

Site https://erncanyildirim.github.io/Vatan-Kuyumculuk/ adresindedir:

- `/` web uygulaması (canlı fiyatlar), `/gizlilik/` gizlilik politikası, `/destek/` destek sayfası
- `/api/config` uygulamaların okuduğu mağaza ayarları (kaynak: `site/api/config`)

`main` dalına her gönderimde `.github/workflows/pages.yml` web uygulamasını derler ve siteyi yayınlar.
Depo ayarlarında **Settings → Pages → Source: GitHub Actions** seçili olmalıdır (bir kez).

Mağaza bilgisi, duyuru veya fiyat ayarı değiştirmek için `site/api/config` dosyasını düzenleyin, `version`
değerini bir artırın ve `main` dalına gönderin. Uygulamalar değişikliği birkaç dakika içinde alır
(GitHub Pages yanıtları 10 dakikaya kadar önbellekte kalabilir).

Admin paneli sunucu tarafı kod (Cloudflare Pages Functions) gerektirdiği için bu sitede yoktur.

## Alan adı alınınca

1. Aşağıdaki Cloudflare kurulumunu yapın, siteyi yeni alan adında yayınlayın.
2. `AppLinks` içinde `SITE_URL`'i yeni alan adı yapın ve eski GitHub Pages adresini `configEndpoints`
   listesinde ikinci sıraya ekleyin; yeni sürümü mağazalara gönderin.
3. Mağaza kayıtlarındaki destek, pazarlama ve gizlilik adreslerini güncelleyin.
4. Eski sürümü kullananlar için GitHub Pages sitesini yayında tutun ve `site/api/config` dosyasını
   admin panelindeki ayarlarla aynı tutun (ya da GitHub Pages'e aynı alan adını bağlayıp yönlendirin).

---

# Cloudflare Pages ile site ve admin paneli (alan adı sonrası)

Site, admin paneli ve `/api/config` tek bir Cloudflare Pages projesinde çalışır. Ücretsiz plan yeterlidir:
sabit sayfalarda trafik sınırı yoktur; `/api/config` ve admin istekleri günlük 100.000 ücretsiz istek
hakkından düşer (uygulama başına oturumda birkaç istek; yerel bir mağaza için bu sınırın çok altındadır).
Sınır bir gün aşılsa bile uygulamalar son kayıtlı ayarlarla ve canlı fiyatlarla çalışmaya devam eder.

## İlk kurulum (bir kez)

1. https://dash.cloudflare.com/sign-up adresinden ücretsiz hesap açın (kart gerekmez).
2. Bilgisayarda:
   ```bash
   cd cloudflare
   npm install
   npx wrangler login                      # tarayıcıda Cloudflare onayı açılır
   npx wrangler pages project create vatankuyumculuk --production-branch main
   npx wrangler kv namespace create CONFIG # çıkan id'yi wrangler.toml içine yazın
   npx wrangler pages secret put ADMIN_PASSWORD --project-name vatankuyumculuk
   npx wrangler pages secret put SESSION_SECRET --project-name vatankuyumculuk   # uzun rastgele bir metin
   ```
3. Siteyi derleyip yayınlayın:
   ```bash
   ./gradlew :webApp:composeCompatibilityBrowserDistribution
   cd cloudflare && npx wrangler pages deploy --branch main
   ```
   Site `https://vatankuyumculuk.pages.dev` adresinde açılır; admin paneli `/admin/` altındadır.

## Alan adını bağlama (Cloudflare)

1. Cloudflare panelinde **Add a domain** → `<alan-adi>` → Free plan.
2. Cloudflare'in verdiği iki ad sunucusunu (nameserver) alan adını aldığınız firmanın panelinde
   "DNS / Nameserver" bölümüne yazın. Etkinleşmesi birkaç saat sürebilir.
3. Pages projesinde **Custom domains** → `<alan-adi>` ve `www.<alan-adi>`
   ekleyin. SSL otomatik ve ücretsizdir. Alan adı eklenirken Cloudflare'in bulduğu eski A kayıtları silinmelidir.

Not: `ADMIN_PASSWORD` veya `SESSION_SECRET` değiştirilirse yeni değer bir sonraki yayında (deploy) geçerli olur;
şifre değişince açık tüm admin oturumları kapanır.

## Panelden şifre değiştirme

Panelin **Şifre** sekmesinde mevcut şifre ve yeni şifre yazılarak şifre değiştirilir. Yeni şifre KV'de
PBKDF2 özeti olarak saklanır ve `ADMIN_PASSWORD`'ün önüne geçer. Şifre unutulursa Cloudflare'de KV'deki
`admin-password` anahtarı silinir; o zaman yeniden `ADMIN_PASSWORD` ile girilir:
`npx wrangler kv key delete admin-password --binding CONFIG --remote`

## Otomatik yayın (GitHub)

Cloudflare'e geçilince `.github/workflows/ci.yml` içine web derlemesinden sonra `cloudflare` klasöründe
`npm ci && npx wrangler pages deploy --branch main` çalıştıran bir iş eklenir. Bunun için GitHub deposunda **Settings → Secrets and variables → Actions** altına ekleyin:

- `CLOUDFLARE_API_TOKEN`: Cloudflare → My Profile → API Tokens → "Edit Cloudflare Workers" şablonu
  (Account / Cloudflare Pages: Edit izni yeterli).
- `CLOUDFLARE_ACCOUNT_ID`: Cloudflare panelinde sağ altta yazar.

## Güvenlik notları

- Admin şifresi en az 12 karakter olmalı. 8 hatalı denemede giriş 15 dakika kilitlenir (en iyi çaba);
  ücretsiz plandaki günlük 100.000 istek sınırı tahmin denemelerini de sınırlar.
- İstenirse Cloudflare panelinde **Security → WAF → Rate limiting rules** altında `/api/admin/login` için
  ücretsiz bir hız sınırı kuralı eklenebilir (ör. 10 saniyede 5 istek).

## Yerelde deneme

```bash
cd cloudflare
printf 'ADMIN_PASSWORD=yerel-sifre\nSESSION_SECRET=yerel\n' > .dev.vars
npx wrangler pages dev --port 8788
```
`http://localhost:8788/admin/` adresinden giriş yapılabilir. `.dev.vars` git'e eklenmez.
