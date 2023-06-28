package com.elta.android.presentation.features.onboaring.pm

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
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.analytics.updateStableParam
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.initializer.WeightFormInitializer
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGlucoseFormatItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingWeightItem
import com.nullgr.core.date.toTimestamp
import java.util.Date
import javax.inject.Inject
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state

class OnBoardingPm @Inject constructor(
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    services: ServiceFacade,
) : BaseListPm(services) {

    val pageChangedAction = action<Int>()
    val currentPageState = state(0)
    val skipPageAction = action<Unit>()
    val nextPageAction = action<Unit>()
    val previousPageAction = action<Unit>()
    val backHandleAction = action<Unit>()
    val nextPageVisibilityState = state(false)
    val previousPageVisibilityState = state(false)
    val titleState = state(resources.getString(R.string.on_boarding_header_user_sex))
    val toolbarMenuButtonIsVisibleState = state(true)

    private val params = hashMapOf<Class<out OnBoardingItem>, Any?>()
    private val updateProfileSettingsAction = action<Unit>()
    private val updateUserInfoAction = action<Unit>()

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
            .map { it > 0 }
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
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun observeBusEvents() {
        bus.events<Events.OnBoardingPageSelected>()
            .subscribe(::onBoardingPageSelected)
            .untilDestroy()
    }

    private fun observeLifecycle() {
        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(updateUserInfoAction.consumer)
            .untilDestroy()
    }

    private fun observeBackAction() {
        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()
    }

    private fun handleBack(i: Unit) {
        if (currentPageState.value != START_PAGE) {
            previousPageAction.consumer.accept(Unit)
        } else {
            router.exit()
        }
    }

    private fun nextPage(i: Unit) {
        val currentPage = currentPageState.value
        if (currentPage == items.value.size - 1) {
            updateProfileSettingsAction.consumer.accept(Unit)
        } else {
            val nextPage = currentPage + 1
            if (nextPage.isPageInRange()) {
                currentPageState.consumer.accept(nextPage)
            }
        }
    }

    private fun skipPage(i: Unit) {
        val currentPage = currentPageState.value
        val currentItem = items.value[currentPage] as OnBoardingItem
        params[currentItem::class.java] = null
        nextPage(i)
    }

    private fun prevPage(i: Unit) {
        val currentPage = currentPageState.value
        val prevPage = currentPage - 1
        if (prevPage.isPageInRange()) {
            currentPageState.consumer.accept(prevPage)
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
                is OnBoardingGlucoseFormatItem,
                -> currentItem.data != null

                is OnBoardingWeightItem -> true
                else -> false
            }
        )
    }

    private fun createUseCaseParams(i: Unit): UpdateProfileUseCase.Params {
        val gender = params[OnBoardingGenderItem::class.java] as? Gender
        val weight = params[OnBoardingWeightItem::class.java] as? Double
        val diabetes = params[OnBoardingDiabetesItem::class.java] as? Diabetes
        val glucoseFormat = params[OnBoardingGlucoseFormatItem::class.java] as? GlucoseFormat
        val profile = Profile(
            gender = gender ?: Gender.NOT_SPECIFIED,
            weight = weight,
            diabetes = diabetes,
            timeStamp = Date().toTimestamp(),
            glucoseFormat = glucoseFormat ?: GlucoseFormat.CAPILLARY
        )
        return UpdateProfileUseCase.Params(profile = profile, isOnboarding = true)
    }

    private fun createEmailUserInfoParams(i: Unit): UpdateUserInfoUseCase.Params =
        UpdateUserInfoUseCase.Params(UserInfo(isEmailConfirmed = true))

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
