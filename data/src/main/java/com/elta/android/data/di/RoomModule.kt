package com.elta.android.data.di


import android.content.Context
import androidx.room.Room
import com.elta.android.data.features.multiLangsConfig.room.ConfigDatabase
import com.elta.android.data.features.multiLangsConfig.room.ScreenConfigDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class RoomModule {

    @Provides
    @Singleton
    fun provideConfigDatabase(context: Context): ConfigDatabase {
        return Room.databaseBuilder(
            context,
            ConfigDatabase::class.java,
            "config_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScreenConfigDao(database: ConfigDatabase): ScreenConfigDao {
        return database.screenConfigDao()
    }
}