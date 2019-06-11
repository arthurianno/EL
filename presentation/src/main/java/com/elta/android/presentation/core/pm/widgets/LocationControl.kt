package com.elta.android.presentation.core.pm.widgets

import android.app.Activity
import android.support.v4.app.Fragment
import com.elta.android.presentation.core.geo.RxLocationManagerFixed
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import me.dmdev.rxpm.PresentationModel

class LocationControl(pm: PresentationModel, private val locationManager: RxLocationManagerFixed) {

    val requestEnableLocationCommand = pm.Command<Unit>(bufferSize = 1)

    val locationEnabledAction = pm.Action<Unit>()

    val locationNotAllowedAction = pm.Action<Unit>()

    fun enableLocation(fragment: Fragment) {
        locationManager.enableLocation(fragment)
    }

    companion object {
        const val REQUEST_CODE_ENABLE_LOCATION = 234
    }
}

fun PresentationModel.locationControl(
    locationManager: RxLocationManagerFixed
): LocationControl = LocationControl(this, locationManager)

fun LocationControl.bindTo(compositeUnbind: CompositeDisposable, fragment: Fragment) {
    requestEnableLocationCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            enableLocation(fragment)
        }
        .addTo(compositeUnbind)
}

fun LocationControl.resolveResults(requestCode: Int, resultCode: Int) {
    if (requestCode == LocationControl.REQUEST_CODE_ENABLE_LOCATION) {
        when (resultCode) {
            Activity.RESULT_OK -> locationEnabledAction.consumer.accept(Unit)
            else -> locationNotAllowedAction.consumer.accept(Unit)
        }
    }
}