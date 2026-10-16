# Yayın öncesi kontrol listesi ve reddedilme riskleri

Durum işaretleri: ✅ hazır · ⏳ bekliyor · ⚠️ dikkat

## Önce yapılması gerekenler (yükleme engelleri)

- ✅ **Web sitesi yayında:** https://erncanyildirim.github.io/Vatan-Kuyumculuk/ (gizlilik `/gizlilik/`,
  destek `/destek/`, uygulama ayarları `/api/config`). GitHub Pages, `.github/workflows/pages.yml` ile yayınlanır.
- ⏳ **Alan adı:** Henüz alınmadı. Alınınca site oraya taşınır, `AppLinks.SITE_URL` ve mağaza
  kayıtlarındaki üç adres güncellenir (bkz. `docs/yayin.md`). Eski adres de çalışmaya devam etmeli,
  çünkü yüklü uygulamalar ayarları oradan okur.
- ⏳ **Mağaza bilgileri:** Adres, çalışma saatleri ve konum bilinmiyor; uygulamada bu satırlar gizli.
  Öğrenilince `site/api/config` dosyasına yazılır, uygulama güncellemesi gerekmez.
- ⏳ **Yetki belgesi** imzalanıp taranmalı (`yetki-belgesi.md`).
- ⏳ **Android imza anahtarı:** `~/.vatan-signing/` altında; kaybedilirse güncelleme yayınlanamaz.
  Yedeği güvenli bir yerde saklanmalı. Play App Signing açık tutulmalı.
- ✅ **Apple Team ID:** 4BQQTXY4NF (Eren Can YILDIRIM, bireysel hesap). Bundle ID com.suer.vatankuyumculuk.

## Apple App Store

- ⚠️ **5.2.1 / 4.1 – İşletme adı kişisel hesapta.** En yüksek risk bu. Apple, bir işletmenin adını ve
  logosunu taşıyan uygulamanın o işletmenin hesabından yayınlanmasını tercih eder. Seçenekler:
  1. Kişisel hesap + imzalı yetki belgesi (inceleme notuna ek). Çoğu zaman kabul edilir, bazen ret gelir.
  2. Mağaza sahibi adına **kuruluş hesabı**: şahıs şirketi D-U-N-S numarasını ücretsiz alabilir,
     yıllık ücret aynıdır (99 USD). Reddedilme riski en düşük yol budur.
- ⚠️ **4.2.6 / 4.3 – Şablon uygulama.** Aynı kod başka bir kuyumcu için de yayında. Apple, şablondan
  üretilen uygulamaların içerik sahibinin hesabından gönderilmesini ister. Ret gelirse yanıt: uygulama
  tek bir mağazaya özel (logo, renkler, iletişim) ve mağaza sahibinin yazılı izniyle yayımlanıyor;
  kalıcı çözüm yine mağaza sahibinin kendi hesabıdır.
- ✅ **4.2 – Asgari işlevsellik.** Uygulama bir web sayfası kopyası değil: canlı fiyat, favoriler,
  hesaplayıcı, mağaza iletişimi, koyu/açık tema, çevrimdışı son fiyat.
- ✅ **5.1.1 – Veri toplama.** Giriş yok, veri toplanmıyor; App Privacy "Veri Toplanmıyor".
- ✅ **Gizlilik bildirimi (PrivacyInfo.xcprivacy)** eklendi: UserDefaults (CA92.1), dosya zaman damgası
  (C617.1), sistem açılış süresi (35F9.1).
- ✅ **İhracat uyumluluğu:** `ITSAppUsesNonExemptEncryption = NO`.
- ✅ **2.3.7 – Meta veri:** Anahtar kelimelerde başka marka adı yok.
- ✅ **2.1 – Çalışan uygulama:** Hafta sonu "Piyasa kapalı" durumu ve son fiyatlar düzgün gösteriliyor,
  inceleme notunda da açıklandı.
- ✅ **iPad:** Uygulama iPad'i destekliyor (geniş ekranda yan menü); iPad ekran görüntüleri gerekli.
- ✅ **Minimum iOS:** 16.0.
- ✅ **Satış bölgesi:** Yalnızca Türkiye. Böylece AB Dijital Hizmetler Yasası'nın "tacir" beyanı istenmez.

## Google Play

- ⚠️ **Kişisel hesap kuralı:** 12 test kullanıcısı, 14 gün kesintisiz kapalı test, sonra üretim erişimi başvurusu.
- ⚠️ **Taklit / yetki (Impersonation):** İşletme adı kullanıldığı için Google da yetki sorabilir;
  aynı yetki belgesi kullanılır.
- ✅ **Veri güvenliği:** "Veri toplanmıyor ve paylaşılmıyor".
- ✅ **Finansal özellikler beyanı:** "Finansal özellik sunmuyor" (yalnızca fiyat gösterimi).
- ✅ **Hedef kitle:** 18+ (Aileler politikası dışı).
- ✅ **Hedef API seviyesi:** targetSdk 37.
- ✅ **İzinler:** Yalnızca `INTERNET`.
- ✅ **Grafikler:** 512 simge, 1024×500 öne çıkan grafik, telefon ekran görüntüleri (1080×1920).

## Uygulama içi zorunlu metinler

- ✅ Her fiyat ekranında "Fiyat kaynağı: Harem Altın" ve "bilgi amaçlıdır, yatırım tavsiyesi değildir".
- ✅ Ayarlar → Gizlilik politikası bağlantısı (GitHub Pages adresi).
- ✅ Uygulama içi satın alma, reklam veya üyelik yok.

## Marka

- ⚠️ "Vatan" adı yaygın bir kelime ve başka sektörlerde tescilli markalar var. TÜRKPATENT'te
  (https://www.turkpatent.gov.tr) "Vatan Kuyumculuk" için sınıf 14 (kuyumculuk) araması yapılması önerilir.
