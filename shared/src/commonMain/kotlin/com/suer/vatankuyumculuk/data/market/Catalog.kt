package com.suer.vatankuyumculuk.data.market

enum class ProductGroup(val title: String) {
    GOLD("Altın"),
    COINS("Sarrafiye"),
    CURRENCY("Döviz"),
    PARITY("Parite"),
    METALS("Emtia"),
}

/** How a product is measured in the calculator. */
enum class CalcUnit(val label: String) {
    GRAM("gram"),
    PIECE("adet"),
    CURRENCY("birim"),
}

/**
 * A displayable instrument. [code] is Harem Altın's feed code.
 * Keep in sync with webApp/src/webMain/resources/catalog.json (checked by CatalogJsonTest).
 */
data class Product(
    val code: String,
    val name: String,
    val group: ProductGroup,
    val decimals: Int,
    /** Currency of the quoted price: "₺" or "$". */
    val currency: String = "₺",
    /** Short unit hint shown under the name, e.g. "gr", "kg", "ons". */
    val unitHint: String? = null,
    /** Family + variant are used to group coins ("Çeyrek" → "Yeni"/"Eski"). */
    val family: String? = null,
    val variant: String? = null,
    val calcUnit: CalcUnit? = null,
    /** Spoken name for screen readers. */
    val spokenName: String = name,
)

object Catalog {
    const val HAS_ALTIN = "ALTIN"

    val products: List<Product> = listOf(
        // Altın
        Product("ALTIN", "Has Altın", ProductGroup.GOLD, 2, unitHint = "24 ayar", calcUnit = CalcUnit.GRAM),
        Product("KULCEALTIN", "Gram Altın", ProductGroup.GOLD, 2, unitHint = "külçe", calcUnit = CalcUnit.GRAM),
        Product("AYAR22", "22 Ayar Bilezik", ProductGroup.GOLD, 2, unitHint = "gr", calcUnit = CalcUnit.GRAM),
        Product("AYAR14", "14 Ayar Altın", ProductGroup.GOLD, 2, unitHint = "gr", calcUnit = CalcUnit.GRAM),
        Product("ONS", "Ons Altın", ProductGroup.GOLD, 2, currency = "$", unitHint = "ons", spokenName = "Ons altın dolar"),
        Product("USDKG", "Kilo Altın ($)", ProductGroup.GOLD, 0, currency = "$", unitHint = "kg"),
        Product("EURKG", "Kilo Altın (€)", ProductGroup.GOLD, 0, currency = "€", unitHint = "kg"),
        // Sarrafiye
        Product("CEYREK_YENI", "Çeyrek Altın", ProductGroup.COINS, 0, family = "Çeyrek", variant = "Yeni", calcUnit = CalcUnit.PIECE, spokenName = "Yeni çeyrek altın"),
        Product("CEYREK_ESKI", "Çeyrek Altın", ProductGroup.COINS, 0, family = "Çeyrek", variant = "Eski", calcUnit = CalcUnit.PIECE, spokenName = "Eski çeyrek altın"),
        Product("YARIM_YENI", "Yarım Altın", ProductGroup.COINS, 0, family = "Yarım", variant = "Yeni", calcUnit = CalcUnit.PIECE, spokenName = "Yeni yarım altın"),
        Product("YARIM_ESKI", "Yarım Altın", ProductGroup.COINS, 0, family = "Yarım", variant = "Eski", calcUnit = CalcUnit.PIECE, spokenName = "Eski yarım altın"),
        Product("TEK_YENI", "Tam Altın", ProductGroup.COINS, 0, family = "Tam", variant = "Yeni", calcUnit = CalcUnit.PIECE, spokenName = "Yeni tam altın"),
        Product("TEK_ESKI", "Tam Altın", ProductGroup.COINS, 0, family = "Tam", variant = "Eski", calcUnit = CalcUnit.PIECE, spokenName = "Eski tam altın"),
        Product("ATA_YENI", "Ata Lira", ProductGroup.COINS, 0, family = "Ata", variant = "Yeni", calcUnit = CalcUnit.PIECE, spokenName = "Yeni ata lira"),
        Product("ATA_ESKI", "Ata Lira", ProductGroup.COINS, 0, family = "Ata", variant = "Eski", calcUnit = CalcUnit.PIECE, spokenName = "Eski ata lira"),
        Product("GREMESE_YENI", "Gremse", ProductGroup.COINS, 0, unitHint = "2,5 lira", family = "Gremse", variant = "Yeni", calcUnit = CalcUnit.PIECE, spokenName = "Yeni gremse"),
        Product("GREMESE_ESKI", "Gremse", ProductGroup.COINS, 0, unitHint = "2,5 lira", family = "Gremse", variant = "Eski", calcUnit = CalcUnit.PIECE, spokenName = "Eski gremse"),
        Product("ATA5_YENI", "Ata Beşli", ProductGroup.COINS, 0, family = "Ata Beşli", variant = "Yeni", calcUnit = CalcUnit.PIECE, spokenName = "Yeni ata beşli"),
        Product("ATA5_ESKI", "Ata Beşli", ProductGroup.COINS, 0, family = "Ata Beşli", variant = "Eski", calcUnit = CalcUnit.PIECE, spokenName = "Eski ata beşli"),
        // Döviz
        Product("USDTRY", "Amerikan Doları", ProductGroup.CURRENCY, 4, unitHint = "USD", calcUnit = CalcUnit.CURRENCY),
        Product("EURTRY", "Euro", ProductGroup.CURRENCY, 4, unitHint = "EUR", calcUnit = CalcUnit.CURRENCY),
        Product("GBPTRY", "İngiliz Sterlini", ProductGroup.CURRENCY, 4, unitHint = "GBP", calcUnit = CalcUnit.CURRENCY),
        Product("CHFTRY", "İsviçre Frangı", ProductGroup.CURRENCY, 4, unitHint = "CHF", calcUnit = CalcUnit.CURRENCY),
        Product("SARTRY", "Suudi Riyali", ProductGroup.CURRENCY, 4, unitHint = "SAR", calcUnit = CalcUnit.CURRENCY),
        Product("AUDTRY", "Avustralya Doları", ProductGroup.CURRENCY, 4, unitHint = "AUD", calcUnit = CalcUnit.CURRENCY),
        Product("CADTRY", "Kanada Doları", ProductGroup.CURRENCY, 4, unitHint = "CAD", calcUnit = CalcUnit.CURRENCY),
        Product("KWDTRY", "Kuveyt Dinarı", ProductGroup.CURRENCY, 4, unitHint = "KWD", calcUnit = CalcUnit.CURRENCY),
        Product("JODTRY", "Ürdün Dinarı", ProductGroup.CURRENCY, 4, unitHint = "JOD", calcUnit = CalcUnit.CURRENCY),
        Product("JPYTRY", "Japon Yeni", ProductGroup.CURRENCY, 4, unitHint = "JPY", calcUnit = CalcUnit.CURRENCY),
        Product("DKKTRY", "Danimarka Kronu", ProductGroup.CURRENCY, 4, unitHint = "DKK", calcUnit = CalcUnit.CURRENCY),
        Product("NOKTRY", "Norveç Kronu", ProductGroup.CURRENCY, 4, unitHint = "NOK", calcUnit = CalcUnit.CURRENCY),
        Product("SEKTRY", "İsveç Kronu", ProductGroup.CURRENCY, 4, unitHint = "SEK", calcUnit = CalcUnit.CURRENCY),
        // Parite
        Product("EURUSD", "EUR / USD", ProductGroup.PARITY, 4, currency = "", spokenName = "Euro dolar paritesi"),
        Product("GBPUSD", "GBP / USD", ProductGroup.PARITY, 4, currency = "", spokenName = "Sterlin dolar paritesi"),
        Product("USDCHF", "USD / CHF", ProductGroup.PARITY, 4, currency = "", spokenName = "Dolar frank paritesi"),
        Product("USDJPY", "USD / JPY", ProductGroup.PARITY, 3, currency = "", spokenName = "Dolar yen paritesi"),
        Product("USDCAD", "USD / CAD", ProductGroup.PARITY, 4, currency = "", spokenName = "Dolar Kanada doları paritesi"),
        Product("AUDUSD", "AUD / USD", ProductGroup.PARITY, 4, currency = "", spokenName = "Avustralya doları dolar paritesi"),
        Product("USDSAR", "USD / SAR", ProductGroup.PARITY, 4, currency = "", spokenName = "Dolar riyal paritesi"),
        // Emtia
        Product("GUMUSTRY", "Gümüş", ProductGroup.METALS, 2, unitHint = "gr", calcUnit = CalcUnit.GRAM),
        Product("XAGUSD", "Gümüş Ons", ProductGroup.METALS, 2, currency = "$", unitHint = "ons"),
        Product("GUMUSUSD", "Gümüş ($/kg)", ProductGroup.METALS, 0, currency = "$", unitHint = "kg"),
        Product("PLATIN", "Platin ($/kg)", ProductGroup.METALS, 0, currency = "$", unitHint = "kg"),
        Product("XPTUSD", "Platin Ons", ProductGroup.METALS, 2, currency = "$", unitHint = "ons"),
        Product("PALADYUM", "Paladyum ($/kg)", ProductGroup.METALS, 0, currency = "$", unitHint = "kg"),
        Product("XPDUSD", "Paladyum Ons", ProductGroup.METALS, 2, currency = "$", unitHint = "ons"),
        Product("XAUXAG", "Altın / Gümüş Oranı", ProductGroup.METALS, 2, currency = ""),
    )

    private val byCode: Map<String, Product> = products.associateBy { it.code }

    operator fun get(code: String): Product? = byCode[code]

    val defaultFavorites: List<String> = listOf("ALTIN", "KULCEALTIN", "AYAR22", "CEYREK_YENI", "USDTRY", "EURTRY")

    /** Products offered in the calculator, in display order. */
    val calculable: List<Product> = products.filter { it.calcUnit != null }

    /** Display name including the coin variant, e.g. "Çeyrek Altın (Yeni)". */
    fun Product.fullName(): String = if (variant != null) "$name ($variant)" else name
}
