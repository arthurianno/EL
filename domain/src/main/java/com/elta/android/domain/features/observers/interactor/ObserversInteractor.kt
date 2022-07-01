package com.elta.android.domain.features.observers.interactor

import com.elta.android.domain.features.observers.model.Observer

val Observer.userName
    get() = customName ?: name
