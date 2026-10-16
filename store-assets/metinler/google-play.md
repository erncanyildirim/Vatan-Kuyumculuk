# Google Play Console: mağaza kaydı ve formlar

Aşağıdaki metinler Play Console'daki alanlara olduğu gibi kopyalanabilir. Karakter sınırları
`tools/store/check_lengths.py` ile kontrol edilir.

## Uygulama bilgileri

| Alan | Değer |
| --- | --- |
| Uygulama adı (en fazla 30) | Vatan Kuyumculuk Osmaniye |
| Varsayılan dil | Türkçe – tr-TR |
| Uygulama mı, oyun mu | Uygulama |
| Ücretli mi, ücretsiz mi | Ücretsiz |
| Paket adı | com.suer.vatankuyumculuk |
| Kategori | Finans |
| Etiketler | Altın, Döviz, Finans |
| Ülkeler | Yalnızca Türkiye |
| İletişim telefonu | +90 505 434 85 92 |
| İletişim e-postası | erncanyildirim@gmail.com (geliştirici hesabının e-postası) |
| Web sitesi | https://erncanyildirim.github.io/Vatan-Kuyumculuk/ |
| Gizlilik politikası | https://erncanyildirim.github.io/Vatan-Kuyumculuk/gizlilik/ |

## Kısa açıklama (en fazla 80)

<!-- short -->
Osmaniye Vatan Kuyumculuk'tan canlı has altın, çeyrek ve döviz fiyatları.
<!-- /short -->

## Tam açıklama (en fazla 4000)

<!-- full -->
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
Büyük ve okunaklı rakamlar, koyu ve açık tema, internet kesildiğinde son fiyatları gösterme.

Vatan Kuyumculuk – Osmaniye
Telefon ve WhatsApp: 0505 434 85 92
Instagram: @vatankuyumcu.80

Fiyatlar Harem Altın piyasa verilerinden alınır ve bilgi amaçlıdır; yatırım tavsiyesi değildir. Mağazadaki satış fiyatı işçilik ve stok durumuna göre farklılık gösterebilir. Uygulama üzerinden alım satım yapılmaz.
<!-- /full -->

## Grafik öğeler

| Öğe | Dosya |
| --- | --- |
| Uygulama simgesi 512×512 | `store-assets/google-play/icon-512.png` |
| Öne çıkan grafik 1024×500 | `store-assets/google-play/feature-graphic.png` |
| Telefon ekran görüntüleri 1080×1920 | `store-assets/google-play/screenshots/` |

## Uygulama içeriği formları

**Gizlilik politikası:** https://erncanyildirim.github.io/Vatan-Kuyumculuk/gizlilik/

**Uygulama erişimi:** "Tüm işlevler özel erişim olmadan kullanılabilir."

**Reklamlar:** "Hayır, uygulamamda reklam yok."

**İçerik derecelendirmesi (IARC):**
- E-posta: erncanyildirim@gmail.com
- Kategori: "Diğer tüm uygulama türleri"
- Şiddet, cinsellik, küfür, uyuşturucu, kumar: hepsi **Hayır**
- Kullanıcılar arası etkileşim veya içerik paylaşımı: **Hayır**
- Kullanıcının konumunu paylaşma: **Hayır**
- Dijital ürün satın alma: **Hayır**
- Beklenen sonuç: 3+ / Herkes

**Hedef kitle:** Yalnızca **18 yaş ve üzeri**. (Çocuklara yönelik seçilirse Aileler politikası devreye girer; gerek yok.)

**Veri güvenliği:**
- Uygulamanız gerekli kullanıcı veri türlerinden herhangi birini topluyor veya paylaşıyor mu? **Hayır**
- (Bu cevapla form "Veri toplanmıyor, veri paylaşılmıyor" olarak yayınlanır. Aktarım şifreli olduğu
  halde "hayır" denince şifreleme sorusu sorulmaz.)

**Devlet uygulamaları:** Hayır.

**Finansal özellikler:** "Uygulamam finansal özellik sunmuyor" seçeneği. Uygulama yalnızca fiyat
gösterir; ödeme, kredi, yatırım hesabı veya kripto işlemi yoktur.

**Sağlık uygulamaları:** Hayır.

**Haber uygulaması:** Hayır.

## Yayın akışı (kişisel hesap)

1. Dahili test: imzalı AAB'yi yükleyip kendi cihazında dene.
2. Kapalı test: en az **12 test kullanıcısı**, kesintisiz **14 gün** testte kalmalı.
3. 14 gün dolunca Play Console'dan "Üretim erişimi için başvur" formu doldurulur; ardından üretim sürümü.
