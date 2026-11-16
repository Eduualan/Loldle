package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface ChampionDao {
    @Query("SELECT * FROM champions ORDER BY name") fun observeAll(): Flow<List<ChampionEntity>>
    @Query("SELECT * FROM champions ORDER BY name") suspend fun getAll(): List<ChampionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(champion: ChampionEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertRemote(champions: List<ChampionEntity>)
    @Update suspend fun update(champion: ChampionEntity): Int
    @Delete suspend fun delete(champion: ChampionEntity): Int
}

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY playedAt DESC") fun observeAll(): Flow<List<GameEntity>>
    @Query("SELECT * FROM games ORDER BY playedAt DESC") suspend fun getAll(): List<GameEntity>
    @Insert suspend fun insert(game: GameEntity): Long
    @Update suspend fun update(game: GameEntity): Int
    @Delete suspend fun delete(game: GameEntity): Int
}

@Database(entities = [ChampionEntity::class, GameEntity::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun champions(): ChampionDao
    abstract fun games(): GameDao

    companion object {
        fun create(context: Context) = Room.databaseBuilder(
            context.applicationContext, GameDatabase::class.java, "loldle.db"
        ).build()
    }
}
