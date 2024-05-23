package com.elta.android.domain.features.rostech.repository

import io.reactivex.Completable

interface IomtRepository {

    fun init(): Completable

    fun connect(pin: String, address: String, email: String)

    fun setListeners(onDisconnect: (() -> Unit)?, onException: ((Exception) -> Unit)?)
}
