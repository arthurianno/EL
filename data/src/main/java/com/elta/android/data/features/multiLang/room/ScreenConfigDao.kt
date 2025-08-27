package com.elta.android.data.features.multiLang.room

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "screen_configs")
data class ScreenConfigEntity(
    @PrimaryKey val slug: String,
    val descriptionJson: String, // JSON string of Map<String, String>
    val backgroundImageUrl: String?,
    val lastUpdated: Long // timestamp
)

@Dao
interface ScreenConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<ScreenConfigEntity>)

    @Query("SELECT * FROM screen_configs WHERE slug IN (:slugs)")
    suspend fun getConfigs(slugs: List<String>): List<ScreenConfigEntity>

    @Query("SELECT MAX(lastUpdated) FROM screen_configs")
    suspend fun getLastUpdateTime(): Long?

    @Query("DELETE FROM screen_configs")
    suspend fun clearAll()
}

@Database(entities = [ScreenConfigEntity::class], version = 1, exportSchema = false)
abstract class ConfigDatabase : RoomDatabase() {
    abstract fun screenConfigDao(): ScreenConfigDao
}