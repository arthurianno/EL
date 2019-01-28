package com.elta.android.data.features.auth.datasource.social.datasource

import android.content.Context
import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
import com.elta.android.domain.features.auth.model.SocialNetwork
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialDataSourceFactory @Inject constructor(private val context: Context) {

    private val fbSdkDataSource by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        FbSdkDataSource(context)
    }
    private val vkSdkDataSource by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        VkSdkDataSource(context)
    }
    private val okSdkDataSource by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        OkSdkDataSource(context)
    }

    fun getDataSource(network: SocialNetwork): SocialNetworkDataSource =
        when (network) {
            SocialNetwork.FB -> fbSdkDataSource
            SocialNetwork.VK -> vkSdkDataSource
            SocialNetwork.OK -> okSdkDataSource
        }
}