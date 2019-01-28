package com.elta.android.data.features.auth.datasource.social.delegates

import android.app.Activity
import com.elta.android.data.features.auth.datasource.social.SocialResult
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider

abstract class SocialDelegate(activity: Activity) : ActivityDelegate(activity) {

    abstract val network: SocialNetwork

    override fun onBackPressed() {
        super.onBackPressed()
        sendResult(SocialResult.Cancel)
    }

    protected fun sendResult(result: SocialResult) {
        SingletonRxBusProvider.BUS.post(RxBus.Keys.SINGLE, result)
        activity.finish()
        activity.overridePendingTransition(0, 0)
    }
}