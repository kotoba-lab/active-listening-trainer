package com.melof.activelisteningtrainer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attempts")
data class AttemptRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val passed: Boolean
)
