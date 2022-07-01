package com.elta.android.data.features.common.storage

interface SyncStorage {

    var lastSalePointsSync: Long?

    var lastEventsSync: Long?

    var lastTagsSync: Long?

    var lastGoogleFitSync: Long?
}
