package com.elta.android.domain.features.rostech

import io.reactivex.Completable

interface RosTechRepository {

    fun init(): Completable
}