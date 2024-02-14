package com.elta.android.domain.features.rostech

import io.reactivex.Completable

interface RosTechRepository {

    fun init(): Completable

    fun sendMeasurments(address: String, events: List<String>)
}