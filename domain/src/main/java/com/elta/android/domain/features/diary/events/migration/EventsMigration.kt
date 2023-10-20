package com.elta.android.domain.features.diary.events.migration

import io.reactivex.Completable

interface EventsMigration {

    fun migrationEventsToEventsV2(): Completable

}