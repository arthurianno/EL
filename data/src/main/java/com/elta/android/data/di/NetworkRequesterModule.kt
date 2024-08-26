package com.elta.android.data.di

import com.elta.android.data.features.common.network.NetworkDataRequester
import com.elta.android.data.features.common.network.NetworkRequester
import dagger.Binds
import dagger.Module

@Module
interface NetworkRequesterModule {

    @Binds
    fun bindNetworkRequester(source: NetworkDataRequester): NetworkRequester
}
