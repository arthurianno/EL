package com.elta.android.presentation.features.onboaring.pm

import android.content.Context
import android.util.Log
import coil.imageLoader
import com.elta.android.common.errors.EmiasError
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.domain.features.diary.events.interactor.AddNewEventUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.emias.interactor.GetEmiasStatusUseCase
import com.elta.android.domain.features.emias.interactor.UpdateEmiasUseCase
import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.user.interactor.UpdateProfileUseCase
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getMetricAttributes
import com.elta.android.presentation.analytic.getMetricName
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.updateStableParam
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.main.events.base.initializer.WeightFormInitializer
import com.elta.android.presentation.features.onboaring.ui.adapter.items.EmiasUi
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingEmiasProfileItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGlucoseFormatItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingWeightItem
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.date.toTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class OnBoardingPm @Inject constructor(
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateEmiasUseCase: UpdateEmiasUseCase,
    private val getEmiasStatus: GetEmiasStatusUseCase,
    private val addNewEvent: AddNewEventUseCase,
    private val appMetric: AppMetricTracker,
    services: ServiceFacade,
) : BaseListPm(services) {

    val pageChangedAction = action<Int>()
    val currentPageState = state(EMIAS_PAGE)
    val skipPageAction = action<Unit>()
    val nextPageAction = action<Unit>()
    val previousPageAction = action<Unit>()
    val backHandleAction = action<Unit>()
    val nextPageVisibilityState = state(false)
    val previousPageVisibilityState = state(false)
    val titleState = state(resources.getString(R.string.on_boarding_header_user_sex))
    val toolbarMenuButtonIsVisibleState = state(true)
    val showDialog = dialogControl<DialogData, DialogResult>()

    private val params = hashMapOf<Class<out OnBoardingItem>, Any?>()
    private val updateProfileSettingsAction = action<Unit>()
    private val saveDateBirthDateAction = action<LocalDate>()
    private val updateUserInfoAction = action<Unit>()
    private val linkedStatusState = state(EmiasStatus.UNLINKED)
    private val emiasDialogs = EmiasDialogs(resources)

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()
        addItems()
        observeCurrentPageState()
        observePageActions()
        observeBusEvents()
        observeUpdateProfile()
        observeLifecycle()
        observeBackAction()
    }

    private fun observeCurrentPageState() {
        currentPageState.observable
            .map {
                if (linkedStatusState.valueOrNull == EmiasStatus.LINKED) it > GENDER_PAGE
                else it > EMIAS_PAGE
            }
            .doOnNext(previousPageVisibilityState.consumer)
            .subscribe()
            .untilDestroy()
        currentPageState.observable
            .filter { it.isPageInRange() }
            .subscribe {
                val currentItem = items.value[it] as OnBoardingItem
                titleState.consumer.accept(currentItem.title)
                val currentItemIsGlucoseFormat = items.value[it] !is OnBoardingGlucoseFormatItem
                toolbarMenuButtonIsVisibleState.consumer.accept(currentItemIsGlucoseFormat)
                updateNextButtonState(currentItem)
            }
            .untilDestroy()
        linkedStatusState.observable
            .subscribe { linkedStatus ->
                appMetric.trackEvent(linkedStatus.getMetricName())
                appMetric.setProfileAttributes(linkedStatus.getMetricAttributes())
                val page = if (linkedStatus == EmiasStatus.LINKED) GENDER_PAGE else EMIAS_PAGE
                currentPageState.consumer.accept(page)
            }
            .untilDestroy()
    }

    private fun observePageActions() {
        pageChangedAction.observable
            .filter { it.isPageInRange() && it != currentPageState.value }
            .subscribe(currentPageState.consumer)
            .untilDestroy()
        skipPageAction.observable
            .debounceAction()
            .subscribe(::skipPage)
            .untilDestroy()
        nextPageAction.observable
            .debounceAction()
            .trackEvent { createOnBoardingEvent() }
            .subscribe(::nextPage)
            .untilDestroy()
        previousPageAction.observable
            .debounceAction()
            .subscribe(::prevPage)
            .untilDestroy()
    }

    private fun addItems() {
        val emiasAccountItem = OnBoardingEmiasProfileItem(
            resources.getString(R.string.on_boarding_header_emias_account)
        )
        val genderItem = OnBoardingGenderItem(
            resources.getString(R.string.on_boarding_header_user_sex)
        )
        val weightItem = OnBoardingWeightItem(
            resources.getString(R.string.on_boarding_header_user_weight),
            WeightFormInitializer.WEIGHT_DEFAULT_VALUE
        )
        val diabetesItem = OnBoardingDiabetesItem(
            resources.getString(R.string.on_boarding_header_user_diabetes_type),
            Diabetes.values().toList()
        )
        val glucoseFormatItem = OnBoardingGlucoseFormatItem(
            resources.getString(R.string.on_boarding_header_user_glucose_format)
        )
        items.consumer.accept(
            listOf(
                emiasAccountItem,
                genderItem,
                weightItem,
                diabetesItem,
                glucoseFormatItem
            )
        )
    }

    private fun observeUpdateProfile() {
        updateUserInfoAction.observable
            .skipWhileInProgress()
            .map(::createEmailUserInfoParams)
            .flatMapCompletable { params ->
                updateUserInfoUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        updateProfileSettingsAction.observable
            .skipWhileInProgress()
            .map(::createUseCaseParams)
            .flatMapCompletable { params ->
                updateProfileUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete { updateStableParam(profile = params.profile) }
                    .doOnComplete {
                        appMetric.setProfileAttributes(params.profile.getMetricAttributes())
                        appMetric.trackEvent(params.profile.glucoseFormat.getMetricName())
                        params.profile.diabetes?.getMetricName()?.let { appMetric.trackEvent(it) }
                    }
                    .doOnComplete {
                        params.profile.weight?.let { addWeightEvent(it) }
                    }
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
        saveDateBirthDateAction.observable
            .map(::createUpdateProfileUseCaseParamsForBirthDate)
            .flatMapCompletable { params ->
                updateProfileUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete { bus.event(Events.ProfileDataChanged) }
                    .doOnError(::handleError)
            }
    }

    private fun observeBusEvents() {
        bus.events<Events.OnBoardingPageSelected>()
            .subscribe(::onBoardingPageSelected)
            .untilDestroy()
    }

    private fun observeLifecycle() {
        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .doOnNext { appMetric.trackEvent(AppMetricEvent.OnboardingScreen) }
            .map { }
            .subscribe(updateUserInfoAction.consumer)
            .untilDestroy()
        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .flatMapSingle {
                getEmiasStatus.execute()
                    .map { it.first }
                    .doOnSuccess(linkedStatusState.consumer)
                    .onErrorReturn { EmiasStatus.UNLINKED }
            }
            .subscribe()
            .untilDestroy()
    }

    private fun observeBackAction() {
        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()
    }

    private fun handleBack(i: Unit) {
        when {
            currentPageState.value == EMIAS_PAGE ||
                    linkedStatusState.value == EmiasStatus.LINKED &&
                    currentPageState.value == GENDER_PAGE -> router.exit()

            else -> previousPageAction.consumer.accept(Unit)
        }
    }

    private fun nextPage(i: Unit) {
        when (currentPageState.value) {
            EMIAS_PAGE -> {
                val emiasUi = params[OnBoardingEmiasProfileItem::class.java] as EmiasUi
                connectToEmias(emiasUi)
            }

            items.value.size - 1 -> updateProfileSettingsAction.consumer.accept(Unit)
            else -> stepForward()
        }
    }

    private fun connectToEmias(emiasUi: EmiasUi) {
        updateEmiasUseCase.execute(createEmaisParams(emiasUi))
            .doOnComplete { linkedStatusState.consumer.accept(EmiasStatus.LINKED) }
            .doOnComplete(::showCompleteDialog)
            .doOnError(::showErrorDialog)
            .bindProgress()
            .subscribe()
            .untilDestroy()
    }

    private fun skipPage(i: Unit) {
        hideKeyBoardCommand.consumer.accept(Unit)
        val currentPage = currentPageState.value
        val currentItem = items.value[currentPage] as OnBoardingItem
        params[currentItem::class.java] = null
        stepForward()
    }

    private fun prevPage(i: Unit) {
        val currentPage = currentPageState.value
        val prevPage = currentPage - ONE_PAGE
        if (prevPage.isPageInRange()) {
            currentPageState.consumer.accept(prevPage)
        }
    }

    private fun stepForward() {
        val nextPage = currentPageState.value + ONE_PAGE
        if (nextPage.isPageInRange()) {
            currentPageState.consumer.accept(nextPage)
        }
    }

    private fun onBoardingPageSelected(event: Events.OnBoardingPageSelected) {
        val item = event.item
        savePageData(item)
        updateNextButtonState(item)
    }

    private fun savePageData(item: OnBoardingItem) {
        params[item::class.java] = item.data
    }

    private fun updateNextButtonState(currentItem: OnBoardingItem) {
        nextPageVisibilityState.consumer.accept(
            when (currentItem) {
                is OnBoardingGenderItem,
                is OnBoardingDiabetesItem,
                is OnBoardingGlucoseFormatItem ->
                    currentItem.data != null

                is OnBoardingEmiasProfileItem -> currentItem.omsIsValid && currentItem.birthdayIsValid
                is OnBoardingWeightItem -> true
                else -> false
            }
        )
    }

    private fun addWeightEvent(weight: Double) {
        addNewEvent.execute(
            AddNewEventUseCase.Params(
                value = weight,
                date = ZonedDateTime.now(),
                eventType = EventType.Weight,
                glucometerSerialNumber = null
            )
        )
            .doOnError(::handleError)
            .subscribe()
            .untilDestroy()
    }

    private fun createUseCaseParams(i: Unit): UpdateProfileUseCase.Params {
        val gender = params[OnBoardingGenderItem::class.java] as? Gender
        val weight = params[OnBoardingWeightItem::class.java] as? Double
        val diabetes = params[OnBoardingDiabetesItem::class.java] as? Diabetes
        val glucoseFormat = params[OnBoardingGlucoseFormatItem::class.java] as? GlucoseFormat
        val emiasUi = params[OnBoardingEmiasProfileItem::class.java] as? EmiasUi
        val profile = Profile(
            gender = gender ?: Gender.NOT_SPECIFIED,
            weight = weight,
            diabetes = diabetes,
            birthDate = emiasUi?.let { parseDate(it.birthday) },
            timeStamp = Date().toTimestamp(),
            glucoseFormat = glucoseFormat ?: GlucoseFormat.CAPILLARY
        )
        return UpdateProfileUseCase.Params(profile = profile, isOnboarding = true)
    }

    private fun createUpdateProfileUseCaseParamsForBirthDate(it: LocalDate) =
        UpdateProfileUseCase.Params(
            Profile(
                birthDate = it,
                glucoseFormat = GlucoseFormat.CAPILLARY
            )
        )

    private fun createEmailUserInfoParams(i: Unit): UpdateUserInfoUseCase.Params =
        UpdateUserInfoUseCase.Params(UserInfo(isEmailConfirmed = true))

    private fun createEmaisParams(emiasUi: EmiasUi): UpdateEmiasUseCase.Params {
        return UpdateEmiasUseCase.Params(
            Emias(
                oms = emiasUi.oms.orEmpty(),
                birthdayDate = SimpleDateFormat(
                    CommonFormats.FORMAT_SIMPLE_DATE,
                    Locale.getDefault()
                ).parse(emiasUi.birthday.orEmpty()) ?: Date()
            )
        )
    }

    private fun parseDate(birthDate: String?): LocalDate? =
        birthDate?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern(CommonFormats.FORMAT_SIMPLE_DATE))
        }

    private fun showErrorDialog(error: Throwable) {
        if (error is EmiasError) appMetric.trackEvent(error.getMetricName())
        val errorDialog = when (error) {
            is EmiasError.EmiasInternalError,
            is ServiceUnavailableError -> emiasDialogs.internalErrorDialogData

            is EmiasError.OmsAlreadyLinked -> emiasDialogs.userAlreadyLinkedDialogData
            is EmiasError.UserInEmiasNotFound -> emiasDialogs.userNotFoundDialogData
            is EmiasError.AgreementForEmiasUsageNotFound -> emiasDialogs.agreementNotFoundDialogData
            else -> emiasDialogs.badInternetConnectionDialogData
        }
        showDialog.show(errorDialog)
    }

    private fun showCompleteDialog() {
        appMetric.trackEvent(AppMetricEvent.EmiasBinded)
        showDialog
            .showForResult(emiasDialogs.userConnectedDialogData)
            .filter { it == DialogResult.POSITIVE }
            .subscribe {
                currentPageState.consumer.accept(GENDER_PAGE)
            }
            .untilDestroy()
    }

    private fun handleSuccess() {
        router.newRootScreen(Screens.ConnectStartScreen(isOnBoarding = true))
    }

    private fun createOnBoardingEvent(): AnalyticsEvent? {
        val item = items.value[currentPageState.value] as OnBoardingItem
        return params[item::class.java]?.let {
            val data = it.toString()
            when (item) {
                is OnBoardingGenderItem ->
                    AnalyticsEvent(
                        AnalyticsEventType.ONB_GENDER_ADD,
                        hashMapOf(AnalyticsEventParam.GENDER to data)
                    )

                is OnBoardingWeightItem ->
                    AnalyticsEvent(
                        AnalyticsEventType.ONB_WEIGHT_ADD
                    )

                is OnBoardingDiabetesItem ->
                    AnalyticsEvent(
                        AnalyticsEventType.ONB_DIABETES_ADD,
                        hashMapOf(AnalyticsEventParam.TYPE to data)
                    )

                else -> null
            }
        }
    }

    private fun Int.isPageInRange(): Boolean = this in 0 until items.value.size
}

private const val EMIAS_PAGE = 0
private const val GENDER_PAGE = 1
private const val ONE_PAGE = 1
