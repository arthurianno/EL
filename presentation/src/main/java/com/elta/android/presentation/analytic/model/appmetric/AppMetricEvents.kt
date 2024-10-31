package com.elta.android.presentation.analytic.model.appmetric

import com.elta.android.presentation.analytic.model.appmetric.params.AlertResultParam
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingPathParam
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingTypeParam
import com.elta.android.presentation.analytic.model.appmetric.params.DiabetesTypeParam
import com.elta.android.presentation.analytic.model.appmetric.params.EmiasErrorParam
import com.elta.android.presentation.analytic.model.appmetric.params.EmiasStatusParam
import com.elta.android.presentation.analytic.model.appmetric.params.GlucoseFormatParam
import com.elta.android.presentation.analytic.model.appmetric.params.SnackStatusParam
import com.elta.android.presentation.analytic.model.appmetric.params.SynchronizedStatusParam
import com.elta.android.presentation.analytic.model.appmetric.params.TurningResultParam
import com.elta.android.presentation.analytic.model.appmetric.params.TypeOfInsulinParam

sealed class AppMetricEvent(
    val eventName: String,
    val eventParams: Pair<String, String>? = null
) {
    data object AppStart : AppMetricEvent("start")

    // Регистрация
    data object RegistrationClick : AppMetricEvent("tap_button_register")
    data object RegistrationContinueClick : AppMetricEvent("tap_button_continue")
    data object ProfileActivationScreen : AppMetricEvent("view_screen_profile_activation")
    data object SendLetter : AppMetricEvent("tap_button_send_a_letter")
    data object ActivationContinue : AppMetricEvent("tap_button_continue_activation")
    data object ProfileVerificationError : AppMetricEvent("view_error_verify_profile")
    data object OnboardingScreen : AppMetricEvent("view_screen_onboarding")

    // Авторизация
    data object AuthorizationClick : AppMetricEvent("tap_button_authorization")
    data object LoginClick : AppMetricEvent("tap_button_login")
    data object MainScreen : AppMetricEvent("view_screen_main")

    // ЕМИАС
    data object EmiasClick : AppMetricEvent("tap_button_emias")
    data class EmiasScreen(@EmiasStatusParam val emiasStatus: String) :
        AppMetricEvent(
            eventName = "view_screen_emias",
            eventParams = "status_emias" to emiasStatus
        )

    data object EmiasSaveClick : AppMetricEvent("tap_button_save")
    data object EmiasBinded : AppMetricEvent("view_alert_emias_success")
    data class EmiasNotBinded(@EmiasErrorParam val emiasError: String) :
        AppMetricEvent(
            eventName = "view_alert_emias_error",
            eventParams = "context" to emiasError
        )

    // Подключение
    data class DeviceConnectingClick(@ConnectingPathParam val connectingPath: String) :
        AppMetricEvent(
            eventName = "tap_button_connecting",
            eventParams = "path" to connectingPath
        )

    data object DeviceConnectingScreen : AppMetricEvent("view_screen_connecting_glucometer")
    data class ConnectingOptionClick(@ConnectingTypeParam val connectingType: String) :
        AppMetricEvent(
            eventName = "tap_button_connecting_glucometer",
            eventParams = "button_name" to connectingType
        )

    data object BluetoothTurningAlert : AppMetricEvent("view_alert_turn_on_bluetooth")
    data class BluetoothTurningAlertClick(@TurningResultParam val turningResult: String) :
        AppMetricEvent(
            eventName = "tap_button_turn_on_bluetooth",
            eventParams = "button_name" to turningResult
        )

    data object DevicesFoundScreen : AppMetricEvent("view_screen_found_devices")
    data object SelectedDeviceConnectClick : AppMetricEvent("tap_button_connect_device")
    data object DeviceConnectClick : AppMetricEvent("tap_button_connect")
    data object DeviceConnectedScreen : AppMetricEvent("view_screen_connected")
    data class DeviceSynchronizedScreen(@SynchronizedStatusParam val statusName: String) :
        AppMetricEvent(
            eventName = "view_screen_synchronization_complete",
            eventParams = "status" to statusName
        )

    data object Permission {
        data object Alert {
            data object Camera : AppMetricEvent("view_alert_allow_access_camera")
            data object Location : AppMetricEvent("view_alert_allow_location_access")
            data object Bluetooth : AppMetricEvent("view_alert_permission_bluetooth")
        }

        data object AlertClick {
            data class Camera(@AlertResultParam val alertResult: String) :
                AppMetricEvent(
                    eventName = "tap_button_allow_access_camera",
                    eventParams = "button_name" to alertResult
                )

            data class Location(@AlertResultParam val alertResult: String) :
                AppMetricEvent(
                    eventName = "tap_button_allow_location_access",
                    eventParams = "button_name" to alertResult
                )

            data class Bluetooth(@AlertResultParam val alertResult: String) :
                AppMetricEvent(
                    eventName = "tap_button_permission_bluetooth",
                    eventParams = "button_name" to alertResult
                )
        }
    }


    data object CameraScanningScreen : AppMetricEvent("view_screen_scan")
    data object ConnectionToDeviceScreen : AppMetricEvent("view_screen_connection_process")
    data object SynchronizationDeviceClick : AppMetricEvent("tap_button_synchronization_device")

    // Синхронизация на главной
    data object SnackProcessing : AppMetricEvent("view_snack_processing")
    data class SnackSynchronization(@SnackStatusParam val statusName: String) :
        AppMetricEvent(
            eventName = "view_snack_synchronization",
            eventParams = "status" to statusName
        )

    data object ReceivedMeasurementsSugar : AppMetricEvent("received_measurements_sugar")

    // Настройки
    data object SettingsScreen : AppMetricEvent("view_screen_settings")
    data object SettingDeleteProfileClick : AppMetricEvent("tap_button_delete_profile")
    data object DeleteProfileAlertClick : AppMetricEvent("tap_button_delete")
    data class DiabetesTypeSave(@DiabetesTypeParam val type: String) :
        AppMetricEvent(
            eventName = "tap_button_save_type_of_diabetes",
            eventParams = "type_of_diabetes" to type
        )

    data class GlucoseFormatSave(@GlucoseFormatParam val format: String) :
        AppMetricEvent(
            eventName = "tap_button_save_glucose_level_format",
            eventParams = "format" to format
        )

    data object TapUploadingAReport : AppMetricEvent("tap_uploading_a_report")
    data object TapSupport : AppMetricEvent("tap_support")
    data object ViewScreenFood : AppMetricEvent("view_screen_food")
    data object TapButtonFoodSave : AppMetricEvent("tap_button_food_save")
    data object ViewScreenFoodNew : AppMetricEvent("view_screen_food_new")
    data object TapButtonFoodNewSave : AppMetricEvent("tap_button_food_new_save")
    data object ViewScreenActivity : AppMetricEvent("view_screen_activity")
    data object ViewScreenActivityType : AppMetricEvent("view_screen_activity_type")
    data object TapButtonActivityType : AppMetricEvent("tap_button_activity_type")
    data object TapButtonActivitySave : AppMetricEvent("tap_button_activity_save")
    data object ViewScreenInsulin : AppMetricEvent("view_screen_insulin")
    data class ViewScreenInsulinType(@TypeOfInsulinParam val param: String) : AppMetricEvent(
        eventName = "view_screen_insulin_type",
        eventParams = "type_of_insulin" to param
    )
    data object TapButtonInsulinSave : AppMetricEvent("tap_button_insulin_save")
    data object ViewScreenMedicines : AppMetricEvent("view_screen_medicines")
    data object ViewScreenMedicinesName : AppMetricEvent("view_screen_medicines_name")
    data object TapButtonMedicinesType : AppMetricEvent("tap_button_medicines_type")
    data object TapButtonMedicinesSave : AppMetricEvent("tap_button_medicines_save")
    data object ViewScreenWeight : AppMetricEvent("view_screen_weight")
    data object TapButtonWeightSave : AppMetricEvent("tap_button_weight_save")
    data object ViewScreenManualGlucose : AppMetricEvent("tap_button_glucose_measurements")
    data object TapButtonManualGlucoseSave : AppMetricEvent("tap_button_glucose_measurements_save")
    data object ViewScreenHbA1c : AppMetricEvent("view_screen_HbA1c")
    data object TapButtonHbA1cSave : AppMetricEvent("tap_button_HbA1c_save")
    data object TapButtonReminders : AppMetricEvent("tap_button_reminders")
    data object TapButtonRemindersNew : AppMetricEvent("tap_button_reminders_new")
    data object TapButtonRemindersSave : AppMetricEvent("tap_button_reminders_save")
    data object TapOnlineConsultant : AppMetricEvent("tap_online_consultant")
}
