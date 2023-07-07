package com.elta.android.presentation.features.googlefit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider

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
        val googleFitAuthResult = if (result) GoogleFitAuthResult.Access else GoogleFitAuthResult.NotAccess
        SingletonRxBusProvider.BUS.post(RxBus.Keys.SINGLE, googleFitAuthResult)
        finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 101
        private const val ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE = 102
    }
}

private fun makeFitnessOptions(): FitnessOptions =
    FitnessOptions.builder()
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
        .build()

