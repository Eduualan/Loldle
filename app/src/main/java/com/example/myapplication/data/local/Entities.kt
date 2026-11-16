package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "champions")
data class ChampionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val title: String,
    val role: String,
    val resource: String,
    val rangeType: String,
    @ColumnInfo(defaultValue = "'Desconhecido'") val gender: String = "Desconhecido",
    @ColumnInfo(defaultValue = "'Desconhecida'") val species: String = "Desconhecida"
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
