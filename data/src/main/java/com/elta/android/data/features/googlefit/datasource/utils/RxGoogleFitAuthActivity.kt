package com.elta.android.data.features.googlefit.datasource.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE -> sendResult(true)
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> requestActivitiesPermission()
                else -> {
                    sendResult(false)
                }
            }
        } else {
            sendResult(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            requestCode == ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            sendResult(true)
        } else {
            sendResult(false)
        }
    }

    private fun requestActivitiesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            sendResult(true)
        }
    }

    private fun sendResult(result: Boolean) {
        SingletonRxBusProvider.BUS.post(RxBus.Keys.SINGLE, AuthResult(result))
        finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 101
        private const val ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE = 102
        private const val FIT_PACKAGE_NAME = "com.google.android.apps.fitness"
        private const val PLAY_MARKET_URI = "market://details?id="

        fun newInstance(context: Context): Intent =
            Intent(context, RxGoogleFitAuthActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        fun launchForResult(context: Context): Observable<AuthResult> =
            Observable.fromCallable {
                launchGoogleFitAppAndPermissions(context)
            }
                .flatMap {
                    SingletonRxBusProvider.BUS.observable(RxBus.Keys.SINGLE)
                        .filter { it is AuthResult }
                        .map { it as AuthResult }
                        .flatMap { Observable.just(it) }
                }

        private fun launchGoogleFitAppAndPermissions(context: Context) {
            runCatching {
                context.packageManager.getPackageInfo(
                    FIT_PACKAGE_NAME,
                    PackageManager.GET_ACTIVITIES
                )
            }.onFailure {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(PLAY_MARKET_URI + FIT_PACKAGE_NAME)
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }.onSuccess {
                newInstance(context).launch(context)
            }
        }
    }
}
