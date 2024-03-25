package com.elta.android.domain.features.rostech

import com.elta.android.domain.features.devices.model.GlucometerEvent
import io.reactivex.Completable

interface RosTechRepository {

    fun init(): Completable

    fun sendMeasurements(address: String, events: List<GlucometerEvent>)
}