package com.elta.android.data.features.googlefit.datasource.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.nullgr.core.intents.launch
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider
import io.reactivex.Observable

class RxGoogleFitAuthActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GoogleSignIn.requestPermissions(
            this,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            GoogleSignIn.getLastSignedInAccount(this),
            makeFitnessOptions()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            sendResult(true)
        } else {
            sendResult(false)
        }
    }

    private fun sendResult(result: Boolean) {
        SingletonRxBusProvider.BUS.post(RxBus.Keys.SINGLE, AuthResult(result))
        finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 101

        fun newInstance(context: Context): Intent =
            Intent(context, RxGoogleFitAuthActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        fun launchForResult(context: Context): Observable<AuthResult> =
            Observable.fromCallable { newInstance(context).launch(context) }
                .flatMap {
                    SingletonRxBusProvider.BUS.observable(RxBus.Keys.SINGLE)
                        .filter { it is AuthResult }
                        .map { it as AuthResult }
                        .flatMap { Observable.just(it) }
                }
    }
}