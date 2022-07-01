package com.elta.android.presentation.core.geo

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.nullgr.core.rx.location.EMPTY_LOCATION
import io.reactivex.Observable
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import timber.log.Timber

class RxLocationManagerFixed(
    private var context: Context,
    private val updatesInterval: Long = 180000,
    private val updateCount: Int? = null
) {

    private val rxLocationProvider: ReactiveLocationProvider by lazy {
        ReactiveLocationProvider(context)
    }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = updatesInterval
            updateCount?.let { numUpdates = it }
        }
    }

    private val locationSettingsRequest: LocationSettingsRequest by lazy {
        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    fun requestLocation(): Observable<Location> {
        return rxLocationProvider.checkLocationSettings(locationSettingsRequest)
            .switchMap {
                when (it.status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> locationObservable()
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        if (!it.status.hasResolution())
                            Observable.just(EMPTY_LOCATION)
                        else Observable.error(LocationTurnedOffError)
                    else -> Observable.just(EMPTY_LOCATION)
                }
            }
    }

    fun enableLocation(fragment: Fragment) {
        val result = SettingsClient(checkNotNull(fragment.context))
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(
                        LocationRequest.create().apply {
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            interval = updatesInterval
                            updateCount?.let { numUpdates = it }
                        }
                    )
                    .setAlwaysShow(true)
                    .build()
            )
        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            (e as? ResolvableApiException)?.startResolutionForResult(
                                checkNotNull(fragment.activity),
                                RxLocationManagerFixed.REQUEST_CODE_ENABLE_LOCATION
                            )
                        } catch (e1: IntentSender.SendIntentException) {
                            Timber.e(e1)
                        }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun locationObservable() = Observable.merge(
        rxLocationProvider.lastKnownLocation,
        rxLocationProvider.getUpdatedLocation(locationRequest)
    )

    companion object {
        const val REQUEST_CODE_ENABLE_LOCATION = 234
    }
}

object LocationTurnedOffError : RuntimeException()
