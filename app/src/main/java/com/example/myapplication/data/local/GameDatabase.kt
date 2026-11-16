package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

private const val DATABASE_VERSION = 3

@Dao
interface ChampionDao {
    @Query("SELECT * FROM champions ORDER BY name") fun observeAll(): Flow<List<ChampionEntity>>
    @Query("SELECT * FROM champions ORDER BY name") suspend fun getAll(): List<ChampionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(champion: ChampionEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertRemote(champions: List<ChampionEntity>)
    @Update suspend fun update(champion: ChampionEntity): Int
    @Query("UPDATE champions SET gender = :gender, species = :species, region = :region WHERE id = :id")
    suspend fun updateTraits(id: String, gender: String, species: String, region: String): Int
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

@Database(entities = [ChampionEntity::class, GameEntity::class], version = DATABASE_VERSION, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun champions(): ChampionDao
    abstract fun games(): GameDao

    companion object {
        private const val DATABASE_NAME = "loldle.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE champions ADD COLUMN gender TEXT NOT NULL DEFAULT 'Desconhecido'")
                db.execSQL("ALTER TABLE champions ADD COLUMN species TEXT NOT NULL DEFAULT 'Desconhecida'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, DATABASE_VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE champions ADD COLUMN region TEXT NOT NULL DEFAULT 'Desconhecida'")
            }
        }

        fun create(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            GameDatabase::class.java,
            DATABASE_NAME
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }
}
