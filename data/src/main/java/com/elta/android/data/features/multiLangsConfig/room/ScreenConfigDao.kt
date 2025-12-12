package com.elta.android.data.features.multiLangsConfig.room

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
    val titleJson: String,  // ← JSON Map<String, String>
    val descriptionJson: String,
    val backgroundImageUrl: String?

)

@Dao
interface ScreenConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<ScreenConfigEntity>)

    @Query("SELECT * FROM screen_configs")
    suspend fun getConfigs(): List<ScreenConfigEntity>

    @Query("SELECT * FROM screen_configs WHERE slug = :slug LIMIT 1")
    suspend fun getConfigBySlug(slug: String): ScreenConfigEntity?

//    @Query("SELECT MAX(lastUpdated) FROM screen_configs")
//    suspend fun getLastUpdateTime(): Long?

    @Query("DELETE FROM screen_configs")
    suspend fun clearAll()
}

@Database(entities = [ScreenConfigEntity::class], version = 1, exportSchema = false)
abstract class ConfigDatabase : RoomDatabase() {
    abstract fun screenConfigDao(): ScreenConfigDao
}