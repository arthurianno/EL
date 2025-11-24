package com.elta.android.presentation.features.profile.settings.emias.viewmodel

import android.os.Bundle
import androidx.core.text.isDigitsOnly
import com.elta.android.common.errors.EmiasError
import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.common.utils.CommonFormats.FORMAT_ONLY_DIGITS
import com.elta.android.domain.common.mapDistinct
import com.elta.android.domain.features.emias.interactor.UnbindEmiasUseCase
import com.elta.android.domain.features.emias.interactor.UpdateEmiasUseCase
import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getMetricAttributes
import com.elta.android.presentation.analytic.getMetricName
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.EmiasErrorParam
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.InputTextFieldWidgetModel
import com.elta.android.presentation.features.profile.settings.emias.model.DATE_LENGTH
import com.elta.android.presentation.features.profile.settings.emias.model.EmiasProfileAction
import com.elta.android.presentation.features.profile.settings.emias.model.EmiasProfileViewState
import com.elta.android.presentation.features.profile.settings.emias.model.OMS_RANGE
import com.elta.android.presentation.features.profile.settings.emias.model.isDateValid
import com.elta.android.presentation.features.profile.settings.emias.model.isOmsValid
import com.elta.android.presentation.features.profile.settings.emias.model.validateDate
import com.elta.android.presentation.features.profile.settings.emias.model.validateOms
import com.elta.android.presentation.features.profile.settings.emias.ui.EmiasProfileFragment
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withTimeout
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class EmiasProfileViewModel @Inject constructor(
    private val unbindEmiasProfile: UnbindEmiasUseCase,
    private val updateEmias: UpdateEmiasUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val getProfile: GetProfileUseCase,
    private val appMetric: AppMetricTracker,
    private val bus: RxBus
) : BaseViewModel<EmiasProfileViewState>() {
    override fun createInitState(): EmiasProfileViewState =
        EmiasProfileViewState(
            oms = "",
            dateBirth = "",
            isLoading = false,
            isLinked = false
        )

    val networkConnectionErrorDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {})
    val agreementNotFoundDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {})
    val omsAlreadyLinkedDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {})
    val emiasProfileUnbindedDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {
        resetData()
    })
    val internalServerErrorDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {})
    val userNotFoundDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {})
    val warningExitDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {
        router.backTo(Screens.ProfileSettings)
    })
    val warningEmiasUnbindDialog = BaseDialogWidgetModel<Nothing>(positiveOnCLick = {
        reduceState { state.value.copy(isLoading = true) }
        unbindProfile()
    })

    val appTopBar = BaseAppTopBarWidgetModel()
    val saveButton = DownButtonWidgetModel()
    val omsInput = InputTextFieldWidgetModel()
    val dateInput = InputTextFieldWidgetModel()

    private var startOms: String = ""
    private var startDateBirth: String = ""

    override val widgets = listOf(appTopBar, saveButton, omsInput, dateInput).actionObserve()

    init {
        setupFilters()
        launch {
            state
                .map { it.oms to it.dateBirth }
                .collect {
                    val isNotSame = it.first != startOms || it.second != startDateBirth
                    val isValid = it.first.isOmsValid() && it.second.isDateValid()

                    saveButton.visibilityState(isNotSame || !state.value.isLinked)
                    saveButton.setEnableState(isValid)
                }
        }

        launch {
            state
                .mapDistinct { it.isLinked }
                .collect { isLinked ->
                    val textId = if (isLinked) R.string.emias_profile_unlink
                    else null

                    appTopBar.setEndText(textId)
                }
        }

        launch {
            combine(omsInput.state, dateInput.state) { oms, date ->
                oms.textField.text to date.textField.text
            }
                .collectLatest {
                    reduceState {
                        state.value.copy(oms = it.first, dateBirth = it.second)
                    }
                }
        }

        launch {
            omsInput.state
                .mapDistinct { it.isFocused }
                .drop(1)
                .filter { isFocused -> !isFocused }
                .map { omsInput.state.value.textField.text }
                .collect { oms ->
                    val errorId = oms.validateOms()

                    omsInput.setError(errorId)
                }
        }

        launch {
            dateInput.state
                .mapDistinct { it.isFocused }
                .drop(1)
                .filter { isFocused -> !isFocused }
                .map { dateInput.state.value.textField.text }
                .collect { date ->
                    val errorId = date.validateDate()

                    dateInput.setError(errorId)
                }
        }
    }

    override fun handleFragmentArguments(arguments: Bundle) {
        val statusArgument = arguments.getString(EmiasProfileFragment.LINK_STATUS_KEY_EXTRA)
        val status = statusArgument?.let { EmiasStatus.valueOf(it) } ?: EmiasStatus.UNLINKED

        val oms = arguments.getString(EmiasProfileFragment.OMS_KEY_EXTRA, "")
        val date = arguments.getString(EmiasProfileFragment.BIRTH_DATE_KEY_EXTRA, "")

        appMetric.trackEvent(status.getMetricName())
        appMetric.setProfileAttributes(status.getMetricAttributes())

        startOms = oms
        startDateBirth = date

        reduceState {
            state.value.copy(isLinked = status == EmiasStatus.LINKED)
        }

        omsInput.setText(oms)
        dateInput.setText(date)

        // Явная валидация после установки текста (используем функции из модели)
        val omsErrorId = oms.validateOms()  // Возвращает Int? (ID ошибки) или null
        omsInput.setError(omsErrorId)

        val dateErrorId = date.validateDate()  // Аналогично
        dateInput.setError(dateErrorId)
    }


    override fun handleUserAction(action: Action) {
        when (action) {
            is EmiasProfileAction.UnbindEmias -> warningEmiasUnbindDialog.dialogOpen()
            DownButtonClick -> updateEmias()
            else -> super.handleUserAction(action)
        }
    }

    override fun backClick() {
        if (dataChanged()) warningExitDialog.dialogOpen()
        else super.backClick()
    }

    private fun setupFilters() {
        omsInput.textFilter = { textField ->
            textField
                .takeIf { it.text.length <= OMS_RANGE.last }
                .takeIf { it?.text?.isDigitsOnly() ?: false }
        }

        dateInput.textFilter = { textField ->
            textField
                .takeIf { it.text.length <= DATE_LENGTH }
                .takeIf { it?.text?.isDigitsOnly() ?: false }
        }
    }

    private fun updateEmias() {
        appMetric.trackEvent(AppMetricEvent.EmiasSaveClick)
        launch {
            updateBirthDate(
                LocalDate.parse(
                    state.value.dateBirth,
                    DateTimeFormatter.ofPattern(FORMAT_ONLY_DIGITS)
                )
            )
            bindEmiasProfile()
        }
    }

    private suspend fun updateBirthDate(value: LocalDate) {
        getProfile()
            .map { profile -> profile.copy(birthDate = value) }
            .collect {
                updateProfile
                    .execute(UpdateProfileUseCase.Params(profile = it))
                    .doOnComplete { bus.event(Events.ProfileDataChanged) }
                    .doOnError(::handleError)
                    .await()
            }
    }

    private suspend fun bindEmiasProfile() {
        val emias = Emias(
            oms = state.value.oms,
            birthdayDate = SimpleDateFormat(
                CommonFormats.FORMAT_ONLY_DIGITS,
                Locale.getDefault()
            ).parse(state.value.dateBirth) ?: Date()
        )
        val params = UpdateEmiasUseCase.Params(emias)

        try {
            withTimeout(EMIAS_TIMEOUT) {
                updateEmias.execute(params)
                    .toObservable<Unit>()
                    .asFlow()
                    .onStart {
                        reduceState { state.value.copy(isLoading = true) }
                    }
                    .onCompletion { throwable ->
                        appMetric.trackEvent(AppMetricEvent.EmiasBinded)
                        reduceState {
                            state.value.copy(
                                isLoading = false,
                                isLinked = throwable == null
                            )
                        }

                        startOms = omsInput.state.value.textField.text
                        startDateBirth = dateInput.state.value.textField.text
                    }
                    .catch { error ->
                        reduceState { state.value.copy(isLoading = false) }
                        handleEmiasError(error)
                        val eventName = when (error) {
                            is EmiasError -> error.getMetricName()
                            is ServiceUnavailableError ->
                                AppMetricEvent.EmiasNotBinded(EmiasErrorParam.INTERNAL_ERROR)

                            else -> null
                        }
                        eventName?.let { appMetric.trackEvent(it) }
                    }
                    .collect()
            }
        } catch (ex: Exception) {
            handleEmiasError(ex)
        }
    }

    private fun unbindProfile() {
        launch {
            try {
                withTimeout(EMIAS_TIMEOUT) {
                    unbindEmiasProfile()
                }
                emiasProfileUnbindedDialog.dialogOpen()

            } catch (ex: Exception) {
                handleEmiasError(ex)
            }
            reduceState { state.value.copy(isLoading = false) }
        }
    }

    private fun resetData() {
        omsInput.setText("")
        dateInput.setText("")

        startOms = ""
        startDateBirth = ""

        reduceState { state.value.copy(isLinked = false) }
    }

    private fun handleEmiasError(it: Throwable) {
        when (it) {
            is EmiasError.UserInEmiasNotFound -> userNotFoundDialog.dialogOpen()
            is EmiasError.AgreementForEmiasUsageNotFound -> agreementNotFoundDialog.dialogOpen()
            is EmiasError.OmsAlreadyLinked -> omsAlreadyLinkedDialog.dialogOpen()
            is EmiasError.EmiasInternalError, is ServiceUnavailableError ->
                internalServerErrorDialog.dialogOpen()

            is NetworkConnectionError, is TimeoutCancellationException ->
                networkConnectionErrorDialog.dialogOpen()

            else -> handleError(it)
        }
    }

    private fun dataChanged() =
        state.value.oms != startOms || state.value.dateBirth != startDateBirth
}

const val EMIAS_TIMEOUT = 30000L
