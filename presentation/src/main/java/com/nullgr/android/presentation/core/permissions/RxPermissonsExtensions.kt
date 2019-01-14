package com.nullgr.android.presentation.core.permissions

import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable

fun RxPermissions.statusFor(permission: String): PermissionStatus {
    return when {
        this.isGranted(permission) -> PermissionStatus.GRANTED
        else -> PermissionStatus.REQUIRED
    }
}

fun RxPermissions.requestStatus(permission: String): Observable<PermissionStatus> {
    return this.requestEach(permission).map {
        when {
            it.granted -> PermissionStatus.GRANTED
            it.shouldShowRequestPermissionRationale -> PermissionStatus.DECLINED
            else -> PermissionStatus.DECLINED_NEVER_ASK
        }
    }
}