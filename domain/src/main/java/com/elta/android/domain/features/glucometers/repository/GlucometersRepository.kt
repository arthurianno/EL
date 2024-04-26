package com.elta.android.domain.features.glucometers.repository

import io.reactivex.Completable

interface GlucometersRepository {
    fun sync(): Completable
}