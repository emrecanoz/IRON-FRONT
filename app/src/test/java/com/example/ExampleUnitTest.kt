package com.example

import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.WavePhase
import com.example.game.economy.EconomyCalculator
import com.example.game.engine.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun economyFormulas_andOfflineCapWorkCorrectly() {
        val baseSave = PlayerSaveEntity(
            lastSavedWallTimeMs = 1_000_000L,
            lastSavedPassiveRatePerSec = 1000.0,
            lastClaimedOfflineTimestampMs = 0L
        )

        // 2 hours offline (7200s) -> 7,200,000 normal gold, 14,400,000 double ad gold
        val twoHoursLater = 1_000_000L + (2L * 3600L * 1000L)
        val report2h = EconomyCalculator.evaluateOfflineEarnings(baseSave, twoHoursLater)
        assertNotNull(report2h)
        assertEquals(7200L, report2h!!.cappedSeconds)
        assertEquals(7_200_000.0, report2h.baseRewardGold, 0.1)
        assertEquals(14_400_000.0, report2h.doubleAdRewardGold, 0.1)

        // 6 hours offline -> capped at 3 hours (10,800s)
        val sixHoursLater = 1_000_000L + (6L * 3600L * 1000L)
        val report6h = EconomyCalculator.evaluateOfflineEarnings(baseSave, sixHoursLater)
        assertNotNull(report6h)
        assertEquals(10800L, report6h!!.cappedSeconds)

        // Duplicate claim guard: cannot claim again for same lastSavedWallTimeMs
        val claimedSave = baseSave.copy(lastClaimedOfflineTimestampMs = 1_000_000L)
        val duplicateReport = EconomyCalculator.evaluateOfflineEarnings(claimedSave, twoHoursLater)
        assertNull(duplicateReport)
    }

    @Test
    fun gameEngine_transitionsFromPreparingToFighting_andGeneratesPassiveGold() {
        val engine = GameEngine()
        val save = PlayerSaveEntity(currentWave = 1)
        engine.initializeForWave(save, WavePhase.PREPARING)
        assertEquals(WavePhase.PREPARING, engine.snapshot.wavePhase)

        // Advance 5.2 seconds in fixed steps to complete the 5-second preparation countdown
        var totalGold = 0.0
        repeat(55) {
            val events = engine.step(0.1f, save)
            totalGold += events.goldEarnedDelta
        }
        assertEquals(WavePhase.FIGHTING, engine.snapshot.wavePhase)
        assertTrue(totalGold > 0.0)
    }
}
