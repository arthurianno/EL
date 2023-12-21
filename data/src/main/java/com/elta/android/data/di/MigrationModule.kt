package com.elta.android.data.di

import com.elta.android.data.features.diary.events.migration.EventsDataMigration
import com.elta.android.domain.features.diary.events.migration.EventsMigration
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class MigrationModule {

    @Binds
    @Singleton
    abstract fun bindEventsMigration(migration: EventsDataMigration): EventsMigration
}
