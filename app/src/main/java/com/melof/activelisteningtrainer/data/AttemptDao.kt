package com.melof.activelisteningtrainer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Insert
    suspend fun insert(record: AttemptRecord)

    @Query("SELECT * FROM attempts WHERE scenarioId = :id ORDER BY timestamp DESC LIMIT 3")
    suspend fun lastThree(id: String): List<AttemptRecord>

    @Query("SELECT * FROM attempts WHERE timestamp >= :todayStart")
    fun todayAttempts(todayStart: Long): Flow<List<AttemptRecord>>

    @Query("SELECT * FROM attempts ORDER BY timestamp DESC LIMIT 1")
    suspend fun lastAttempt(): AttemptRecord?

    @Query("SELECT DISTINCT scenarioId FROM attempts")
    fun allAttemptedIds(): Flow<List<String>>
}
