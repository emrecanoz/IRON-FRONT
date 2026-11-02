package com.example.monetization

enum class RewardedAdPlacement(
    val titleTr: String,
    val descriptionTr: String,
    val durationLabelTr: String
) {
    OFFLINE_DOUBLE(
        titleTr = "Çevrimdışı Kazancı 2x Katla",
        descriptionTr = "Biriken çevrimdışı altın üretimini anında iki katına çıkarır.",
        durationLabelTr = "Anında 2x Altın"
    ),
    PRODUCTION_BOOST_15M(
        titleTr = "15 Dakika Lojistik Hızlandırma",
        descriptionTr = "Tüm askeri tesislerin pasif altın üretimini 15 dakika boyunca 2 katına çıkarır.",
        durationLabelTr = "15 Dk x2 Üretim"
    ),
    DAMAGE_BOOST_10M(
        titleTr = "Yüksek Kalibre Mühimmat Desteği",
        descriptionTr = "10 dakika boyunca tüm piyade, taret, roket ve uçak hasarını +%50 artırır.",
        durationLabelTr = "10 Dk +%50 Hasar"
    ),
    BONUS_SUPPLY_DROP(
        titleTr = "Acil Hava İkmal Sandığı",
        descriptionTr = "Komuta merkezine anında altın ve +10 Elmas ikmali indirir.",
        durationLabelTr = "Altın + 10 Elmas"
    )
}

enum class StoreProductType {
    STARTER_PACK,
    DIAMOND_PACK,
    PERMANENT_AUTOMATION,
    AD_FREE_PACK
}

data class BillingProductConfig(
    val productId: String,
    val type: StoreProductType,
    val titleTr: String,
    val subtitleTr: String,
    val badgeTr: String,
    val formattedPrice: String, // Dynamically fetched from Play Billing SKU details in production
    val goldAmount: Double = 0.0,
    val diamondAmount: Int = 0,
    val isOneTimeNonConsumable: Boolean = false
)

object BillingCatalog {
    /**
     * Simulates Google Play Billing ProductDetails query results so prices are not hardcoded in UI components.
     */
    fun queryAvailableProducts(): List<BillingProductConfig> = listOf(
        BillingProductConfig(
            productId = "ironfront_starter_pack",
            type = StoreProductType.STARTER_PACK,
            titleTr = "Komutan Başlangıç Paketi",
            subtitleTr = "+15.000 Altın, +120 Elmas, +%20 Üretim Bonusu ve 2x Hız Kilidi",
            badgeTr = "EN POPÜLER",
            formattedPrice = "₺89,99",
            goldAmount = 15000.0,
            diamondAmount = 120,
            isOneTimeNonConsumable = true
        ),
        BillingProductConfig(
            productId = "ironfront_perm_automation",
            type = StoreProductType.PERMANENT_AUTOMATION,
            titleTr = "Kalıcı Otomasyon Modülü",
            subtitleTr = "Altın biriktikçe en uygun tesisi otomatik yükseltir + %25 Lojistik Hızı",
            badgeTr = "KALICI AVANTAJ",
            formattedPrice = "₺149,99",
            goldAmount = 5000.0,
            diamondAmount = 50,
            isOneTimeNonConsumable = true
        ),
        BillingProductConfig(
            productId = "ironfront_ad_free_bundle",
            type = StoreProductType.AD_FREE_PACK,
            titleTr = "Reklamsız VIP Komuta Paketi",
            subtitleTr = "Tüm ödüllü reklam bonuslarını beklemeden anında topla + 2x Oyun Hızı + 200 Elmas",
            badgeTr = "VIP KOMUTA",
            formattedPrice = "₺219,99",
            goldAmount = 25000.0,
            diamondAmount = 200,
            isOneTimeNonConsumable = true
        ),
        BillingProductConfig(
            productId = "ironfront_diamonds_100",
            type = StoreProductType.DIAMOND_PACK,
            titleTr = "Taktik Elmas Kasası (100)",
            subtitleTr = "Özel üs görünümleri ve anında ikmal için 100 Elmas",
            badgeTr = "İKMAL",
            formattedPrice = "₺49,99",
            diamondAmount = 100,
            isOneTimeNonConsumable = false
        ),
        BillingProductConfig(
            productId = "ironfront_diamonds_350",
            type = StoreProductType.DIAMOND_PACK,
            titleTr = "Stratejik Elmas Rezervi (350)",
            subtitleTr = "Yüksek kapasiteli 350 Elmas + 10.000 Bonus Altın",
            badgeTr = "+%25 BONUS",
            formattedPrice = "₺129,99",
            goldAmount = 10000.0,
            diamondAmount = 350,
            isOneTimeNonConsumable = false
        )
    )
}
