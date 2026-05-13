package com.elta.android.data.features.common.storage

import io.reactivex.Completable

interface SyncStorage {

    var lastSalePointsSync: Long?

    var lastEventsSync: Long?

    var lastTagsSync: Long?

    var lastGoogleFitSync: Long?

    var lastMedicamentSync: Long?

    fun getLastMedicamentSync(countryCode: String, languageTag: String): Long?

    fun setLastMedicamentSync(countryCode: String, languageTag: String, value: Long?)

    fun deleteDbFiles(): Completable
}
