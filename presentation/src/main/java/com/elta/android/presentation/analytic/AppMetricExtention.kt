package com.elta.android.presentation.analytic

import com.elta.android.common.errors.EmiasError
import com.elta.android.domain.features.emias.model.EmiasStatus
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.analytic.model.appmetric.AppMetricAttribute
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.models.AlertType
import com.elta.android.presentation.analytic.model.appmetric.params.AlertResultParam
import com.elta.android.presentation.analytic.model.appmetric.params.DiabetesTypeParam
import com.elta.android.presentation.analytic.model.appmetric.params.EmiasErrorParam
import com.elta.android.presentation.analytic.model.appmetric.params.EmiasStatusParam
import com.elta.android.presentation.analytic.model.appmetric.params.GlucoseFormatParam
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.tbruyelle.rxpermissions2.Permission

fun Profile.getMetricAttributes(): List<AppMetricAttribute> {
    val attributes = mutableListOf<AppMetricAttribute>()
    email?.let { attributes.add(AppMetricAttribute.Email(it)) }
    diabetes?.getMetricAttribute()?.let { diabetesType -> attributes.add(diabetesType) }
    return attributes
}

fun EmiasStatus.getMetricName(): AppMetricEvent.EmiasScreen {
    val eventParam = when (this) {
        EmiasStatus.LINKED -> EmiasStatusParam.REGISTERED
        EmiasStatus.UNLINKED -> EmiasStatusParam.NOT_REGISTERED
    }
    return AppMetricEvent.EmiasScreen(eventParam)
}

fun EmiasError.getMetricName(): AppMetricEvent.EmiasNotBinded {
    val eventParam = when (this) {
        is EmiasError.UserInEmiasNotFound -> EmiasErrorParam.USER_NOT_FOUND
        is EmiasError.AgreementForEmiasUsageNotFound -> EmiasErrorParam.NO_AGREEMENT
        is EmiasError.OmsAlreadyLinked -> EmiasErrorParam.ACCOUNT_ALREADY_LINKED
        else -> EmiasErrorParam.INTERNAL_ERROR
    }
    return AppMetricEvent.EmiasNotBinded(eventParam)
}

@OptIn(ExperimentalPermissionsApi::class)
fun PermissionState.getMetricName(alertType: AlertType): AppMetricEvent {
    return when {
        !status.isGranted && !status.shouldShowRationale -> getAlertMetric(alertType)
        !status.isGranted && status.shouldShowRationale ->
            getAlertClickMetric(
                alertType = alertType,
                param = AlertResultParam.PROHIBIT
            )

        else -> getAlertClickMetric(
            alertType = alertType,
            param = AlertResultParam.ALLOW
        )
    }
}

fun Permission.getMetricName(alertType: AlertType): AppMetricEvent {
    return when {
        !granted && !shouldShowRequestPermissionRationale -> getAlertMetric(alertType)
        !granted && shouldShowRequestPermissionRationale ->
            getAlertClickMetric(
                alertType = alertType,
                param = AlertResultParam.PROHIBIT
            )

        else -> getAlertClickMetric(
            alertType = alertType,
            param = AlertResultParam.ALLOW
        )
    }
}

fun Diabetes.getMetricName(): AppMetricEvent.DiabetesTypeSave {
    val params = when (this) {
        Diabetes.FIRST -> DiabetesTypeParam.DIABETES_FIRST
        Diabetes.SECOND -> DiabetesTypeParam.DIABETES_SECOND_INSULIN
        Diabetes.SECOND_TABLETS -> DiabetesTypeParam.DIABETES_SECOND_PILLS
    }
    return AppMetricEvent.DiabetesTypeSave(params)
}

fun GlucoseFormat.getMetricName(): AppMetricEvent.GlucoseFormatSave {
    val eventParams = when (this) {
        GlucoseFormat.CAPILLARY -> GlucoseFormatParam.CAPILLARY
        GlucoseFormat.PLASMA -> GlucoseFormatParam.PLASMA
    }
    return AppMetricEvent.GlucoseFormatSave(eventParams)
}

private fun Diabetes.getMetricAttribute(): AppMetricAttribute.DiabetesType {
    val params = when (this) {
        Diabetes.FIRST -> DiabetesTypeParam.DIABETES_FIRST
        Diabetes.SECOND -> DiabetesTypeParam.DIABETES_SECOND_INSULIN
        Diabetes.SECOND_TABLETS -> DiabetesTypeParam.DIABETES_SECOND_PILLS
    }
    return AppMetricAttribute.DiabetesType(params)
}

private fun getAlertMetric(alertType: AlertType): AppMetricEvent {
    return when (alertType) {
        AlertType.Camera -> AppMetricEvent.Permission.Alert.Camera
        AlertType.Location -> AppMetricEvent.Permission.Alert.Location
        AlertType.Bluetooth -> AppMetricEvent.Permission.Alert.Bluetooth
    }
}

private fun getAlertClickMetric(
    alertType: AlertType,
    @AlertResultParam param: String
): AppMetricEvent {
    return when (alertType) {
        AlertType.Camera -> AppMetricEvent.Permission.AlertClick.Camera(param)
        AlertType.Location -> AppMetricEvent.Permission.AlertClick.Location(param)
        AlertType.Bluetooth -> AppMetricEvent.Permission.AlertClick.Bluetooth(param)
    }
}
