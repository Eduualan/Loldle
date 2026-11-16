package com.example.myapplication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "champions")
data class ChampionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val title: String,
    val role: String,
    val resource: String,
    val rangeType: String,
    @ColumnInfo(defaultValue = "'Desconhecido'")
    val gender: String = UNKNOWN_GENDER,
    @ColumnInfo(defaultValue = "'Desconhecida'")
    val species: String = UNKNOWN_SPECIES,
    @ColumnInfo(defaultValue = "'Desconhecida'")
    val region: String = UNKNOWN_REGION
) {
    companion object {
        const val UNKNOWN_GENDER = "Desconhecido"
        const val UNKNOWN_SPECIES = "Desconhecida"
        const val UNKNOWN_REGION = "Desconhecida"
    }
}

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val player: String,
    val champion: String,
    val won: Boolean,
    val attempts: Int,
    val playedAt: Long = System.currentTimeMillis()
)
