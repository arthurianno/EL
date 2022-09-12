package com.elta.android.presentation.core.pm.widgets

import android.app.Activity
import androidx.fragment.app.Fragment
import com.elta.android.presentation.core.geo.RxLocationManagerFixed
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command

class LocationControl(pm: PresentationModel, private val locationManager: RxLocationManagerFixed) {

    val requestEnableLocationCommand = pm.command<Unit>(bufferSize = 1)

    val locationEnabledAction = pm.action<Unit>()

    val locationNotAllowedAction = pm.action<Unit>()

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
