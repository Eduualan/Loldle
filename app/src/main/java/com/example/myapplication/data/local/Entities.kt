package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "champions")
data class ChampionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val title: String,
    val role: String,
    val resource: String,
    val rangeType: String
)

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val player: String,
    val champion: String,
    val won: Boolean,
    val attempts: Int,
    val playedAt: Long = System.currentTimeMillis()
)
