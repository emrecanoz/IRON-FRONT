package com.example.data.local

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val playerSaveFlow: Flow<PlayerSaveEntity?> = gameDao.observePlayerSave()

    suspend fun getOrCreateSave(): PlayerSaveEntity {
        val existing = gameDao.getPlayerSaveOnce()
        if (existing != null) return existing
        val initial = PlayerSaveEntity(
            lastSavedWallTimeMs = System.currentTimeMillis()
        )
        gameDao.upsertPlayerSave(initial)
        return initial
    }

    suspend fun saveState(entity: PlayerSaveEntity) {
        gameDao.upsertPlayerSave(entity)
    }
}
