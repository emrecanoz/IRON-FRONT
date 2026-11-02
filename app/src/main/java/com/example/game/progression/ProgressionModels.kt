package com.example.game.progression

import androidx.compose.ui.graphics.Color
import com.example.data.local.PlayerSaveEntity
import com.example.game.economy.EconomyCalculator
import com.example.ui.theme.DiamondBlue
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.PrestigePurple
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TacticalGreen

enum class FacilityType(
    val titleTr: String,
    val subtitleTr: String,
    val baseCost: Double,
    val baseProduction: Double,
    val accentColor: Color
) {
    BARRACKS(
        titleTr = "Askeri Kışla",
        subtitleTr = "Otomatik piyade mangası eğitimi ve saha konuşlandırması",
        baseCost = 65.0,
        baseProduction = 8.0,
        accentColor = TacticalGreen
    ),
    ROCKET_SILO(
        titleTr = "Roket Silosu",
        subtitleTr = "Zırhlı tank ve boss hedeflerine güdümlü füze atışı",
        baseCost = 140.0,
        baseProduction = 14.0,
        accentColor = EconomyGold
    ),
    AIR_FLEET(
        titleTr = "Uçak Filosu",
        subtitleTr = "Hava devriyesi, önleme jetleri ve otomatik bombardıman",
        baseCost = 260.0,
        baseProduction = 22.0,
        accentColor = DiamondBlue
    ),
    BORDER_WALL(
        titleTr = "Sınır Duvarı",
        subtitleTr = "Komuta merkezi zırhı, taret hattı ve altıgen enerji kalkanı",
        baseCost = 95.0,
        baseProduction = 10.0,
        accentColor = ShieldCyan
    )
}

enum class SubUpgradeId(
    val facility: FacilityType,
    val titleTr: String,
    val descriptionTr: String,
    val baseCost: Double
) {
    // 4.1 Askeri Kışla
    BARRACKS_SPAWN_RATE(FacilityType.BARRACKS, "Üretim Hızı", "Yeni piyade eğitim süresini kısaltır", 45.0),
    BARRACKS_DAMAGE(FacilityType.BARRACKS, "Piyade Hasarı", "Tüfek ve ağır silah mermi gücünü artırır", 55.0),
    BARRACKS_HEALTH(FacilityType.BARRACKS, "Piyade Canı", "Saha askerlerinin zırh ve dayanıklılığını artırır", 50.0),
    BARRACKS_MAX_UNITS(FacilityType.BARRACKS, "Saha Kapasitesi", "Aynı anda cephede çarpışan maksimum asker sayısı", 120.0),

    // 4.2 Roket Silosu
    ROCKET_DAMAGE(FacilityType.ROCKET_SILO, "Roket Hasarı", "Zırh delici harp başlığı patlama hasarı", 90.0),
    ROCKET_FIRE_RATE(FacilityType.ROCKET_SILO, "Atış Sıklığı", "Silo yeniden dolum ve fırlatma hızı", 85.0),
    ROCKET_BLAST_RADIUS(FacilityType.ROCKET_SILO, "Patlama Alanı", "Çevredeki düşmanları etkileyen şarapnel yarıçapı", 110.0),
    ROCKET_MULTI_TARGET(FacilityType.ROCKET_SILO, "Çoklu Hedef", "Aynı yaylım ateşinde fırlatılan füze sayısı", 240.0),

    // 4.3 Uçak Filosu
    AIR_DAMAGE(FacilityType.AIR_FLEET, "Saldırı Hasarı", "Hava-karaya füze ve ağır makineli top hasarı", 150.0),
    AIR_FREQUENCY(FacilityType.AIR_FLEET, "Saldırı Sıklığı", "Filo sorti ve pist kalkış hazırlık hızı", 140.0),
    AIR_TARGET_COUNT(FacilityType.AIR_FLEET, "Hedef Sayısı", "Sorti başına bırakılan güdümlü mühimmat adedi", 220.0),
    AIR_BOMB_AREA(FacilityType.AIR_FLEET, "Bombardıman Alanı", "Halı bombardımanı ateş koridoru genişliği", 180.0),

    // 4.4 Sınır Duvarı
    WALL_MAX_HP(FacilityType.BORDER_WALL, "Maksimum Üs Canı", "Komuta duvarı toplam yapısal bütünlüğü", 70.0),
    WALL_ARMOR(FacilityType.BORDER_WALL, "Zırh Kalınlığı", "Düşman mermilerine karşı doğrudan zırh bloğu", 80.0),
    WALL_DAMAGE_REDUCTION(FacilityType.BORDER_WALL, "Hasar Azaltma", "Gelen tüm patlama ve lazer hasarını sönümler", 110.0),
    WALL_SHIELD_CAPACITY(FacilityType.BORDER_WALL, "Enerji Kalkanı", "Altıgen plazma kalkanı kapasitesi ve yenilenmesi", 130.0)
}

enum class PrestigeUpgradeId(
    val titleTr: String,
    val descriptionTr: String,
    val basePointCost: Int,
    val bonusPerLevelTr: String
) {
    PERM_DAMAGE(
        "Taktiksel Ateş Doktrini",
        "Tüm kara, roket ve hava birimlerinin kalıcı hasarını artırır",
        basePointCost = 1,
        bonusPerLevelTr = "+%25 Tüm Hasar"
    ),
    PERM_GOLD(
        "Askeri Endüstri Kompleksi",
        "Pasif altın üretimini ve düşman imha gelirlerini kalıcı artırır",
        basePointCost = 1,
        bonusPerLevelTr = "+%25 Altın Üretimi"
    ),
    PERM_BASE_HP(
        "Titanyum Tahkimat",
        "Sınır duvarı maksimum canını ve enerji kalkanını kalıcı güçlendirir",
        basePointCost = 1,
        bonusPerLevelTr = "+%30 Üs Canı & Kalkan"
    ),
    PERM_SUPPORT_SKILL(
        "Hava Komuta Üstünlüğü",
        "Taktiksel Hava Desteği hasarını artırır ve bekleme süresini düşürür",
        basePointCost = 2,
        bonusPerLevelTr = "+%30 Yetenek Gücü"
    ),
    PERM_AUTOMATION(
        "Otonom Lojistik Ağı",
        "Fabrika verimliliğini ve çevrimdışı üretim hızını kalıcı artırır",
        basePointCost = 2,
        bonusPerLevelTr = "+%15 Lojistik Hızı"
    )
}

data class MilitaryRank(
    val titleTr: String,
    val minScore: Int,
    val badgeCode: String
)

data class MissionItem(
    val id: String,
    val titleTr: String,
    val descriptionTr: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardGold: Double,
    val rewardDiamonds: Int,
    val isClaimed: Boolean
) {
    val isCompleted: Boolean get() = currentProgress >= targetProgress
}

data class BaseSkinOption(
    val id: String,
    val nameTr: String,
    val descriptionTr: String,
    val diamondPrice: Int,
    val primaryWallColor: Color,
    val accentColor: Color,
    val shieldColor: Color
)

object ProgressionCatalog {
    val ranks = listOf(
        MilitaryRank("Teğmen", 0, "LT"),
        MilitaryRank("Yüzbaşı", 5, "CPT"),
        MilitaryRank("Binbaşı", 10, "MAJ"),
        MilitaryRank("Yarbay", 18, "LTC"),
        MilitaryRank("Albay", 28, "COL"),
        MilitaryRank("Tuğgeneral", 42, "BG"),
        MilitaryRank("Tümgeneral", 60, "MG"),
        MilitaryRank("Korgeneral", 85, "LTG"),
        MilitaryRank("Orgeneral", 120, "GEN"),
        MilitaryRank("Mareşal", 170, "MAR")
    )

    val baseSkins = listOf(
        BaseSkinOption(
            id = "STANDARD",
            nameTr = "Zümrüt Komuta (Standart)",
            descriptionTr = "Klasik NATO taktik yeşil karargâh ve cyan enerji kalkanı",
            diamondPrice = 0,
            primaryWallColor = Color(0xFF334155),
            accentColor = TacticalGreen,
            shieldColor = ShieldCyan
        ),
        BaseSkinOption(
            id = "DESERT_STORM",
            nameTr = "Çöl Fırtınası Karargâhı",
            descriptionTr = "Altın kum kamuflajlı kuleler, kehribar kalkan (+%10 Üretim)",
            diamondPrice = 60,
            primaryWallColor = Color(0xFF78350F),
            accentColor = EconomyGold,
            shieldColor = Color(0xFFFBBF24)
        ),
        BaseSkinOption(
            id = "ARCTIC_AEGIS",
            nameTr = "Kutup Kalkanı Üssü",
            descriptionTr = "Buzul zırh kaplaması ve yüksek yoğunluklu mavi plazma (+%10 Üretim)",
            diamondPrice = 90,
            primaryWallColor = Color(0xFF1E3A8A),
            accentColor = DiamondBlue,
            shieldColor = Color(0xFF38BDF8)
        ),
        BaseSkinOption(
            id = "CYBER_VANGUARD",
            nameTr = "Siber Muhafız Kompleksi",
            descriptionTr = "Karanlık kuantum reaktör mimarisi ve mor iyon kalkanı (+%10 Üretim)",
            diamondPrice = 140,
            primaryWallColor = Color(0xFF3B0764),
            accentColor = PrestigePurple,
            shieldColor = Color(0xFFC084FC)
        )
    )

    fun getPlayerRank(save: PlayerSaveEntity): MilitaryRank {
        val score = save.highestWaveReached + (save.prestigeCount * 12) +
            ((save.barracksLevel + save.rocketSiloLevel + save.airFleetLevel + save.borderWallLevel) / 4)
        return ranks.lastOrNull { score >= it.minScore } ?: ranks.first()
    }

    fun getVisualTierText(level: Int): String = when {
        level >= 10 -> "Kademe 3: Ağır Askeri Kompleks"
        level >= 5 -> "Kademe 2: Gelişmiş Taktik Tesis"
        else -> "Kademe 1: Standart Saha Tesisi"
    }

    fun buildMissions(save: PlayerSaveEntity): List<MissionItem> {
        val claimedSet = save.claimedMissionIdsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        return listOf(
            MissionItem(
                id = "WAVE_5",
                titleTr = "İlk Hat Savunması",
                descriptionTr = "5. Dalga'ya ulaş ve üssü koru.",
                currentProgress = save.highestWaveReached,
                targetProgress = 5,
                rewardGold = 1500.0,
                rewardDiamonds = 15,
                isClaimed = "WAVE_5" in claimedSet
            ),
            MissionItem(
                id = "WAVE_10",
                titleTr = "Demir Cephe Komutanı",
                descriptionTr = "10. Dalga'ya ulaşarak General Terfisi sistemini aç.",
                currentProgress = save.highestWaveReached,
                targetProgress = 10,
                rewardGold = 6500.0,
                rewardDiamonds = 30,
                isClaimed = "WAVE_10" in claimedSet
            ),
            MissionItem(
                id = "WAVE_20",
                titleTr = "Stratejik Üstünlük",
                descriptionTr = "20. Dalga'ya ulaş.",
                currentProgress = save.highestWaveReached,
                targetProgress = 20,
                rewardGold = 45000.0,
                rewardDiamonds = 50,
                isClaimed = "WAVE_20" in claimedSet
            ),
            MissionItem(
                id = "ROCKET_LV_5",
                titleTr = "Füze Kalkanı Ağı",
                descriptionTr = "Roket Silosunu 5. seviyeye çıkar.",
                currentProgress = save.rocketSiloLevel,
                targetProgress = 5,
                rewardGold = 2500.0,
                rewardDiamonds = 15,
                isClaimed = "ROCKET_LV_5" in claimedSet
            ),
            MissionItem(
                id = "ROCKET_LV_10",
                titleTr = "Balistik Caydırıcılık",
                descriptionTr = "Roket Silosunu 10. seviyeye çıkar.",
                currentProgress = save.rocketSiloLevel,
                targetProgress = 10,
                rewardGold = 18000.0,
                rewardDiamonds = 35,
                isClaimed = "ROCKET_LV_10" in claimedSet
            ),
            MissionItem(
                id = "TANKS_25",
                titleTr = "Zırh Avcısı",
                descriptionTr = "Toplam 25 düşman zırhlı tankı imha et.",
                currentProgress = save.totalTanksKilled,
                targetProgress = 25,
                rewardGold = 4000.0,
                rewardDiamonds = 20,
                isClaimed = "TANKS_25" in claimedSet
            ),
            MissionItem(
                id = "TANKS_100",
                titleTr = "Çelik Mezarlığı",
                descriptionTr = "Toplam 100 düşman zırhlı tankı imha et.",
                currentProgress = save.totalTanksKilled,
                targetProgress = 100,
                rewardGold = 25000.0,
                rewardDiamonds = 45,
                isClaimed = "TANKS_100" in claimedSet
            ),
            MissionItem(
                id = "PRESTIGE_1",
                titleTr = "İlk General Terfisi",
                descriptionTr = "En az 1 kez General Terfisi (Prestij) gerçekleştir.",
                currentProgress = save.prestigeCount,
                targetProgress = 1,
                rewardGold = 12000.0,
                rewardDiamonds = 35,
                isClaimed = "PRESTIGE_1" in claimedSet
            ),
            MissionItem(
                id = "PRESTIGE_5",
                titleTr = "Kurmay Konseyi",
                descriptionTr = "Toplam 5 kez General Terfisi gerçekleştir.",
                currentProgress = save.prestigeCount,
                targetProgress = 5,
                rewardGold = 80000.0,
                rewardDiamonds = 80,
                isClaimed = "PRESTIGE_5" in claimedSet
            ),
            MissionItem(
                id = "GOLD_1M",
                titleTr = "Savaş Ekonomisi",
                descriptionTr = "Toplam 1M altın kazan.",
                currentProgress = (save.totalGoldEarned / 1000.0).toInt().coerceAtMost(1000),
                targetProgress = 1000,
                rewardGold = 150000.0,
                rewardDiamonds = 60,
                isClaimed = "GOLD_1M" in claimedSet
            )
        )
    }

    fun getFacilityLevel(save: PlayerSaveEntity, type: FacilityType): Int = when (type) {
        FacilityType.BARRACKS -> save.barracksLevel
        FacilityType.ROCKET_SILO -> save.rocketSiloLevel
        FacilityType.AIR_FLEET -> save.airFleetLevel
        FacilityType.BORDER_WALL -> save.borderWallLevel
    }

    fun getSubUpgradeLevel(save: PlayerSaveEntity, id: SubUpgradeId): Int = when (id) {
        SubUpgradeId.BARRACKS_SPAWN_RATE -> save.barracksSpawnRateLv
        SubUpgradeId.BARRACKS_DAMAGE -> save.barracksDamageLv
        SubUpgradeId.BARRACKS_HEALTH -> save.barracksHealthLv
        SubUpgradeId.BARRACKS_MAX_UNITS -> save.barracksMaxUnitsLv
        SubUpgradeId.ROCKET_DAMAGE -> save.rocketDamageLv
        SubUpgradeId.ROCKET_FIRE_RATE -> save.rocketFireRateLv
        SubUpgradeId.ROCKET_BLAST_RADIUS -> save.rocketBlastRadiusLv
        SubUpgradeId.ROCKET_MULTI_TARGET -> save.rocketMultiTargetLv
        SubUpgradeId.AIR_DAMAGE -> save.airDamageLv
        SubUpgradeId.AIR_FREQUENCY -> save.airFrequencyLv
        SubUpgradeId.AIR_TARGET_COUNT -> save.airTargetCountLv
        SubUpgradeId.AIR_BOMB_AREA -> save.airBombAreaLv
        SubUpgradeId.WALL_MAX_HP -> save.wallMaxHpLv
        SubUpgradeId.WALL_ARMOR -> save.wallArmorLv
        SubUpgradeId.WALL_DAMAGE_REDUCTION -> save.wallDamageReductionLv
        SubUpgradeId.WALL_SHIELD_CAPACITY -> save.wallShieldCapacityLv
    }

    fun getPrestigeUpgradeLevel(save: PlayerSaveEntity, id: PrestigeUpgradeId): Int = when (id) {
        PrestigeUpgradeId.PERM_DAMAGE -> save.permDamageLv
        PrestigeUpgradeId.PERM_GOLD -> save.permGoldProductionLv
        PrestigeUpgradeId.PERM_BASE_HP -> save.permBaseHealthLv
        PrestigeUpgradeId.PERM_SUPPORT_SKILL -> save.permSupportSkillLv
        PrestigeUpgradeId.PERM_AUTOMATION -> save.permAutomationLv
    }

    fun getPrestigeUpgradeCost(save: PlayerSaveEntity, id: PrestigeUpgradeId): Int {
        val currentLv = getPrestigeUpgradeLevel(save, id)
        return id.basePointCost + (currentLv * id.basePointCost)
    }
}
