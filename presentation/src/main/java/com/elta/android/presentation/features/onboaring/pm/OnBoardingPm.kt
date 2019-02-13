package com.elta.android.presentation.features.onboaring.pm

import com.elta.android.domain.features.user.interactor.UpdateUserSettingsUseCase
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingWeightItem
import javax.inject.Inject

class OnBoardingPm @Inject constructor(
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val pageChangedAction = Action<Int>()
    val currentPageState = State(0)
    val skipPageAction = Action<Unit>()
    val nextPageAction = Action<Unit>()
    val previousPageAction = Action<Unit>()
    val nextPageVisibilityState = State(false)
    val previousPageVisibilityState = State(false)
    val titleState = State(resources.getString(R.string.on_boarding_header_user_sex))

    private val params = hashMapOf<Class<out OnBoardingItem>, Any?>()
    private val updateProfileSettingsAction = Action<Unit>()

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()
        addItems()

        pageChangedAction.observable
            .filter { it.isPageInRange() && it != currentPageState.value }
            .subscribe(currentPageState.consumer)
            .untilDestroy()

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
                updateNextButtonState(currentItem)
            }
            .untilDestroy()

        skipPageAction.observable
            .debounceAction()
            .subscribe(::skipPage)
            .untilDestroy()

        nextPageAction.observable
            .debounceAction()
            .subscribe(::nextPage)
            .untilDestroy()

        previousPageAction.observable
            .debounceAction()
            .subscribe(::prevPage)
            .untilDestroy()

        bindBusEvents()
        bindUpdateProfileBehaviour()
    }

    private fun addItems() {
        items.consumer.accept(
            listOf(
                OnBoardingGenderItem(
                    resources.getString(R.string.on_boarding_header_user_sex)
                ),
                OnBoardingWeightItem(
                    resources.getString(R.string.on_boarding_header_user_weight),
                    INITIAL_WEIGHT
                ),
                OnBoardingDiabetesItem(
                    resources.getString(R.string.on_boarding_header_user_diabetes_type),
                    Diabetes.values().toList()
                )
            )
        )
    }

    private fun bindUpdateProfileBehaviour() {
        updateProfileSettingsAction.observable
            .skipWhileInProgress()
            .map(::createUseCaseParams)
            .flatMapCompletable { params ->
                updateUserSettingsUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindBusEvents() {
        bus.events<Events.OnBoardingPageSelected>()
            .subscribe(::onBoardingPageSelected)
            .untilDestroy()
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
        val data = item.data
        params[item::class.java] = data
        updateNextButtonState(item)
    }

    private fun updateNextButtonState(currentItem: OnBoardingItem) {
        val data = currentItem.data
        val isNextPageAvailable = when (currentItem) {
            is OnBoardingGenderItem -> data != null
            is OnBoardingDiabetesItem -> data != null
            is OnBoardingWeightItem -> true
            else -> false
        }
        nextPageVisibilityState.consumer.accept(isNextPageAvailable)
    }

    private fun createUseCaseParams(i: Unit): UpdateUserSettingsUseCase.Params {
        val gender = params[OnBoardingGenderItem::class.java] as? Gender
        val weight = params[OnBoardingWeightItem::class.java] as? Double
        val diabetes = params[OnBoardingDiabetesItem::class.java] as? Diabetes
        return UpdateUserSettingsUseCase.Params(gender, weight, diabetes)
    }

    private fun handleSuccess() {
        router.newRootScreen(Screens.ShopsFlow)
    }

    private fun Int.isPageInRange(): Boolean = this in 0 until items.value.size

    private companion object {
        const val INITIAL_WEIGHT = 70.0
    }
}