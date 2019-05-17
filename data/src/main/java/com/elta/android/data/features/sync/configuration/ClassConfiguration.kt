package com.elta.android.data.features.sync.configuration

import com.elta.android.data.features.common.dto.StateDto

data class ClassConfiguration(
    val supportedState: List<StateDto>,
    val mapper: LocalSyncMapper<*>
)