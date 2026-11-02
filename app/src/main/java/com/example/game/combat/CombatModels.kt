package com.example.game.combat

enum class WavePhase(val labelTr: String) {
    PREPARING("HAZIRLIK"),
    FIGHTING("ÇATIŞMA"),
    REWARD_SELECTION("ÖDÜL SEÇİMİ"),
    COMPLETED("TAMAMLANDI")
}

enum class EnemyType(
    val nameTr: String,
    val isFlying: Boolean,
    val isBoss: Boolean,
    val baseHp: Double,
    val baseSpeed: Float,
    val baseDamage: Double,
    val armorReduction: Double,
    val attackRangeX: Float,
    val goldMultiplier: Double
) {
    INFANTRY(
        nameTr = "Hücum Piyadesi",
        isFlying = false,
        isBoss = false,
        baseHp = 75.0,
        baseSpeed = 0.082f,
        baseDamage = 9.0,
        armorReduction = 0.0,
        attackRangeX = 0.42f,
        goldMultiplier = 1.0
    ),
    ARMORED_TANK(
        nameTr = "Zırhlı Ana Muharebe Tankı",
        isFlying = false,
        isBoss = false,
        baseHp = 320.0,
        baseSpeed = 0.044f,
        baseDamage = 26.0,
        armorReduction = 0.28,
        attackRangeX = 0.48f,
        goldMultiplier = 3.2
    ),
    STEALTH_JET(
        nameTr = "Hayalet Savaş Uçağı",
        isFlying = true,
        isBoss = false,
        baseHp = 190.0,
        baseSpeed = 0.115f,
        baseDamage = 22.0,
        armorReduction = 0.12,
        attackRangeX = 0.54f,
        goldMultiplier = 2.6
    ),
    MEGA_BOSS_MECH(
        nameTr = "Mega Boss Titan Mech",
        isFlying = false,
        isBoss = true,
        baseHp = 1650.0,
        baseSpeed = 0.028f,
        baseDamage = 75.0,
        armorReduction = 0.35,
        attackRangeX = 0.50f,
        goldMultiplier = 15.0
    )
}

data class EnemyUnit(
    val id: Long,
    val type: EnemyType,
    val x: Float, // Normalized 0.0 (left HQ) to 1.0 (right spawn edge)
    val laneY: Float, // Normalized depth 0.25 to 0.85 on battlefield
    val maxHp: Double,
    val currentHp: Double,
    val speed: Float,
    val attackDamage: Double,
    val attackCooldownSec: Float = 0f,
    val walkCyclePhase: Float = 0f,
    val hitFlashTimer: Float = 0f
)

data class AlliedSoldier(
    val id: Long,
    val x: Float, // Normalized 0.26 to 0.46
    val laneY: Float,
    val maxHp: Double,
    val currentHp: Double,
    val damage: Double,
    val fireCooldownSec: Float = 0f,
    val walkPhase: Float = 0f,
    val muzzleFlashTimer: Float = 0f
)

enum class ProjectileKind {
    ALLY_BULLET,
    TURRET_TRACER,
    HOMING_ROCKET,
    AIR_BOMB,
    ARTILLERY_SHELL,
    ENEMY_SHELL,
    BOSS_PLASMA
}

data class Projectile(
    val id: Long,
    val kind: ProjectileKind,
    val startX: Float,
    val startY: Float,
    val currentX: Float,
    val currentY: Float,
    val targetX: Float,
    val targetY: Float,
    val progress: Float,
    val speed: Float,
    val damage: Double,
    val blastRadius: Float,
    val arcHeight: Float = 0.08f
)

data class AirSortiePlane(
    val id: Long,
    val x: Float, // -0.15f to 1.15f
    val y: Float,
    val speed: Float,
    val isTacticalStrike: Boolean,
    val targetDropX: Float,
    val hasDroppedPayload: Boolean = false
)

data class ExplosionEffect(
    val id: Long,
    val x: Float,
    val y: Float,
    val radius: Float,
    val progress: Float, // 0f -> 1f
    val isBigRocketOrBomb: Boolean,
    val isShieldHit: Boolean = false
)

data class SmokeParticle(
    val x: Float,
    val y: Float,
    val alpha: Float,
    val size: Float
)

data class FloatingDamageText(
    val id: Long,
    val x: Float,
    val y: Float,
    val text: String,
    val isCriticalOrRocket: Boolean,
    val isGoldGain: Boolean = false,
    val progress: Float = 0f // 0f -> 1f
)

enum class TacticalSkillType(
    val titleTr: String,
    val unlockWave: Int,
    val baseCooldownSec: Float,
    val requiresTargetTap: Boolean
) {
    AIRSTRIKE(
        titleTr = "Hava Bombardımanı",
        unlockWave = 1,
        baseCooldownSec = 14f,
        requiresTargetTap = true
    ),
    ARTILLERY_BARRAGE(
        titleTr = "Topçu Ateşi",
        unlockWave = 4,
        baseCooldownSec = 18f,
        requiresTargetTap = true
    ),
    SHIELD_OVERCHARGE(
        titleTr = "Kalkan Yüklemesi",
        unlockWave = 7,
        baseCooldownSec = 22f,
        requiresTargetTap = false
    )
}

enum class WaveRewardType(
    val titleTr: String,
    val badgeTr: String
) {
    FIREPOWER("Ateş Gücü İkmali", "SALDIRI BONUSU"),
    ENERGY_SHIELD("Enerji Kalkanı Hücresi", "SAVUNMA BONUSU"),
    TREASURE_VAULT("Hazine Kasası", "ANINDA ALTIN")
}

data class WaveRewardOption(
    val type: WaveRewardType,
    val titleTr: String,
    val descriptionTr: String,
    val valueSummaryTr: String,
    val bonusPctOrGold: Double
)

data class CombatSnapshot(
    val waveNumber: Int = 1,
    val wavePhase: WavePhase = WavePhase.PREPARING,
    val prepareCountdownSec: Float = 5.0f,
    val baseMaxHp: Double = 1000.0,
    val baseCurrentHp: Double = 1000.0,
    val shieldMaxHp: Double = 350.0,
    val shieldCurrentHp: Double = 350.0,
    val enemiesRemainingToSpawn: Int = 0,
    val totalWaveEnemies: Int = 8,
    val enemiesDefeatedInWave: Int = 0,
    val enemies: List<EnemyUnit> = emptyList(),
    val alliedSoldiers: List<AlliedSoldier> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val airSorties: List<AirSortiePlane> = emptyList(),
    val explosions: List<ExplosionEffect> = emptyList(),
    val smokeTrail: List<SmokeParticle> = emptyList(),
    val floatingTexts: List<FloatingDamageText> = emptyList(),
    val rewardOptions: List<WaveRewardOption> = emptyList(),
    val airstrikeCooldownRemaining: Float = 0f,
    val airstrikeCharges: Int = 2,
    val maxAirstrikeCharges: Int = 2,
    val artilleryCooldownRemaining: Float = 0f,
    val shieldBoostCooldownRemaining: Float = 0f,
    val activeTargetingSkill: TacticalSkillType? = null,
    val radarAngleDeg: Float = 0f,
    val turretMuzzleFlashTimer: Float = 0f,
    val siloLaunchFlashTimer: Float = 0f,
    val lastBannerMessage: String? = null
)
