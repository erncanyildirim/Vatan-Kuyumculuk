# App Store Connect: mağaza kaydı ve formlar

Karakter sınırları `tools/store/check_lengths.py` ile kontrol edilir.

## Uygulama bilgileri

| Alan | Değer |
| --- | --- |
| Ad (en fazla 30) | Vatan Kuyumculuk Osmaniye |
| Birincil dil | Türkçe |
| Paket kimliği (Bundle ID) | com.suer.vatankuyumculuk |
| SKU | VATAN-OSMANIYE-001 |
| Birincil kategori | Finans |
| İkincil kategori | Alışveriş |
| Fiyat | Ücretsiz |
| Satış bölgesi | Yalnızca Türkiye |
| Telif hakkı | 2026 Vatan Kuyumculuk |
| Destek URL'si | https://erncanyildirim.github.io/Vatan-Kuyumculuk/destek/ |
| Pazarlama URL'si | https://erncanyildirim.github.io/Vatan-Kuyumculuk/ |
| Gizlilik politikası URL'si | https://erncanyildirim.github.io/Vatan-Kuyumculuk/gizlilik/ |

Alan adı alındığında bu üç adres yeni alan adıyla güncellenir (bkz. `docs/yayin.md`).

## Alt başlık (en fazla 30)

<!-- subtitle -->
Canlı altın ve döviz fiyatları
<!-- /subtitle -->

## Tanıtım metni (en fazla 170)

<!-- promo -->
Has altın, çeyrek ve döviz fiyatları anlık olarak cebinizde. Üyelik yok, reklam yok. Osmaniye Vatan Kuyumculuk'a tek dokunuşla ulaşın.
<!-- /promo -->

## Anahtar kelimeler (en fazla 100, virgülle, boşluksuz)

<!-- keywords -->
altın,has,gram,çeyrek,döviz,dolar,euro,kuyumcu,osmaniye,bilezik,ata,cumhuriyet,fiyat,22 ayar,kur
<!-- /keywords -->

## Açıklama (en fazla 4000)

<!-- description -->
Vatan Kuyumculuk Osmaniye uygulamasıyla altın ve döviz fiyatlarını anlık olarak takip edin. Üyelik yok, reklam yok, kişisel veri toplanmaz.

CANLI FİYATLAR
• Has altın, gram altın, 22 ayar bilezik ve 14 ayar altın
• Çeyrek, yarım, tam, ata lira, gremse ve ata beşli (yeni ve eski)
• Dolar, euro, sterlin ve diğer dövizler
• Gümüş, platin, paladyum ve ons fiyatları
• Fiyat değiştiği anda güncellenir, yükselişte yeşil, düşüşte kırmızı yanar
• Günlük değişim yüzdesi ile gün içi en düşük ve en yüksek fiyat

FAVORİLER
Sık baktığınız ürünleri yıldızlayın, açılış ekranında ilk onları görün.

HESAPLAYICI
Elinizdeki altının veya dövizin değerini saniyeler içinde öğrenin. Gram ya da adet girin; "bize satarsanız" ve "bizden alırsanız" tutarlarını birlikte görün.

MAĞAZAMIZ
Tek dokunuşla arayın, WhatsApp'tan yazın veya Instagram hesabımıza göz atın.

SADE VE KULLANIŞLI
Büyük ve okunaklı rakamlar, koyu ve açık tema, internet kesildiğinde son fiyatları gösterme. iPhone ve iPad'de çalışır.

Vatan Kuyumculuk – Osmaniye
Telefon ve WhatsApp: 0505 434 85 92
Instagram: @vatankuyumcu.80

Fiyatlar Harem Altın piyasa verilerinden alınır ve bilgi amaçlıdır; yatırım tavsiyesi değildir. Mağazadaki satış fiyatı işçilik ve stok durumuna göre farklılık gösterebilir. Uygulama üzerinden alım satım yapılmaz.
<!-- /description -->

## Ekran görüntüleri

| Cihaz | Boyut | Klasör |
| --- | --- | --- |
| iPhone 6,3" | 1206×2622 | `store-assets/app-store/screenshots/iphone-6.3/` |
| iPad 13" | 2064×2752 | `store-assets/app-store/screenshots/ipad-13/` |

## Uygulama gizliliği (App Privacy)

- "Bu uygulamadan veri topluyor musunuz?" → **Hayır, bu uygulamadan veri toplamıyoruz.**
- Mağazada "Veri Toplanmıyor" etiketi görünür.
- Uygulamada üçüncü taraf SDK (reklam, analiz, çökme raporu) yoktur; `PrivacyInfo.xcprivacy`
  yalnızca kullanılan API'leri (UserDefaults) bildirir.

## Yaş derecelendirmesi

Tüm sorulara **Yok / Hayır**. Kumar, sınırsız web erişimi, kullanıcı içeriği yok. Beklenen sonuç: **4+**.

## İhracat uyumluluğu (şifreleme)

Info.plist'te `ITSAppUsesNonExemptEncryption = NO` tanımlı; uygulama yalnızca işletim sisteminin
standart HTTPS/WSS şifrelemesini kullandığı için her yüklemede soru sorulmaz.

## İnceleme notu (App Review Information → Notes)

<!-- review -->
Bu uygulama, Osmaniye'deki Vatan Kuyumculuk mağazasının fiyat uygulamasıdır ve mağaza sahibinin yazılı izniyle, mağaza adına geliştirici Eren Can Yıldırım tarafından yayımlanmaktadır (izin belgesi ektedir).

Uygulama giriş gerektirmez; tüm özellikler açılışta kullanılabilir. Canlı altın ve döviz fiyatları, Türkiye'de kuyumcuların referans aldığı Harem Altın'ın herkese açık fiyat yayınından alınır ve ekranda kaynak olarak belirtilir. Uygulama alım satım, ödeme veya yatırım hizmeti sunmaz; yalnızca bilgi amaçlı fiyat gösterir, hesaplayıcı ve mağaza iletişim bilgilerini içerir.

Hafta sonu piyasalar kapalı olduğunda son kapanış fiyatları gösterilir ve ekranda "Piyasa kapalı" yazar.

This is the price app of Vatan Kuyumculuk, a jewelry store in Osmaniye, Türkiye. It is published on the store's behalf by developer Eren Can Yildirim with the store owner's written authorization (attached). No login is required. Gold and currency prices come from Harem Altın's public price feed, the reference most Turkish jewelers use, and the source is shown in the app. The app does not offer trading, payments or investment services.
<!-- /review -->

İletişim kişisi: Eren Can Yıldırım, erncanyildirim@gmail.com, telefon: hesapta kayıtlı numara.
Demo hesap: Gerekmiyor (giriş yok).
Ek: imzalı `yetki-belgesi.md` çıktısının fotoğrafı veya PDF'i (App Review Information → Attachment).
