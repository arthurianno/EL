@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.elta.android.data.features.auth.datasource.social

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elta.android.data.features.auth.datasource.social.delegates.ActivityDelegate
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.nullgr.core.intents.launch
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider
import io.reactivex.Observable

class RxSocialActivity : Activity() {

    private lateinit var delegate: ActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val network = intent.extras?.get(EXTRA_SOCIAL) as SocialNetworkType
        delegate = network.getDelegate(this)
        delegate.onCreate(savedInstanceState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        delegate.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        delegate.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val EXTRA_SOCIAL = "extra_social"

        fun newInstance(context: Context, network: SocialNetworkType): Intent =
            Intent(context, RxSocialActivity::class.java)
                .apply {
                    putExtra(EXTRA_SOCIAL, network)
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

        fun launchForResult(context: Context, network: SocialNetworkType): Observable<SocialResult> =
            Observable.fromCallable { newInstance(context, network).launch(context) }
                .flatMap {
                    SingletonRxBusProvider.BUS.observable(RxBus.Keys.SINGLE)
                        .filter { it is SocialResult }
                        .map { it as SocialResult }
                        .flatMap { Observable.just(it) }
                }
    }
}
