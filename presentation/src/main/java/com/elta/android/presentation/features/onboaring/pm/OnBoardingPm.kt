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
    val nextPageAvailableCommand = Command<Boolean>(bufferSize = 1)
    val previousPageAvailableCommand = Command<Boolean>(bufferSize = 1)
    val titleState = State(resources.getString(R.string.on_boarding_header_user_sex))

    private val params = hashMapOf<Class<out OnBoardingItem>, Any?>()
    private val updateProfileSettingsAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

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

        pageChangedAction.observable
            .filter { it != currentPageState.value }
            .subscribe(currentPageState.consumer)
            .untilDestroy()

        currentPageState.observable
            .map { it > 0 }
            .doOnNext(previousPageAvailableCommand.consumer)
            .subscribe()
            .untilDestroy()

        skipPageAction.observable
            .doOnNext(::skipPage)
            .subscribe()
            .untilDestroy()

        nextPageAction.observable
            .doOnNext(::nextPage)
            .subscribe()
            .untilDestroy()

        previousPageAction.observable
            .doOnNext(::prevPage)
            .subscribe()
            .untilDestroy()

        bus.events<Events.OnBoardingPageSelected>()
            .subscribe(::onBoardingPageSelected)
            .untilDestroy()

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

    private fun nextPage(i: Unit) {
        var currentPage = currentPageState.value
        if (currentPage < items.value.size - 1) {
            currentPageState.consumer.accept(++currentPage)
            val currentItem = items.value[currentPageState.value] as OnBoardingItem
            titleState.consumer.accept(currentItem.title)
        } else {
            updateProfileSettingsAction.consumer.accept(Unit)
        }
    }

    private fun skipPage(i: Unit) {
        nextPage(i)
        val currentItem = items.value[currentPageState.value] as OnBoardingItem
        params[currentItem::class.java] = null
    }

    private fun prevPage(i: Unit) {
        currentPageState.consumer.accept(currentPageState.value - 1)
    }

    private fun onBoardingPageSelected(event: Events.OnBoardingPageSelected) {
        val item = event.item
        val data = item.data
        val isNextPageAvailable = when (item) {
            is OnBoardingGenderItem -> data != null
            is OnBoardingDiabetesItem -> data != null
            is OnBoardingWeightItem -> data != null && data != INITIAL_WEIGHT
            else -> false
        }
        params[item::class.java] = data
        nextPageAvailableCommand.consumer.accept(isNextPageAvailable)
    }

    private fun createUseCaseParams(i: Unit): UpdateUserSettingsUseCase.Params {
        val gender = params[OnBoardingGenderItem::class.java] as? Gender
        val weight = params[OnBoardingWeightItem::class.java] as? Double
        val diabetes = params[OnBoardingDiabetesItem::class.java] as? Diabetes
        return UpdateUserSettingsUseCase.Params(gender, weight, diabetes)
    }

    private fun handleSuccess() {
        router.navigateTo(Screens.Maps)
    }

    private companion object {
        const val INITIAL_WEIGHT = 70.0
    }
}