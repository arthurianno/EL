package com.elta.android.presentation.features.onboaring.pm

import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingWeightItem
import javax.inject.Inject

class OnBoardingPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val pageChangedAction = Action<Int>()
    val currentPageState = State(0)
    val nextPageAction = Action<Unit>()
    val previousPageAction = Action<Unit>()
    val nextPageAvailableCommand = Command<Boolean>(bufferSize = 1)
    val previousPageAvailableCommand = Command<Boolean>(bufferSize = 1)

    override fun onCreate() {
        super.onCreate()
        items.consumer.accept(
            listOf(
                OnBoardingGenderItem(
                    resources.getString(R.string.on_boarding_header_user_sex)
                ),
                OnBoardingWeightItem(
                    resources.getString(R.string.on_boarding_header_user_weight)
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

        currentPageState.observable
            .map { it < items.valueOrNull?.size?.minus(1) ?: 0 } // TODO add selection result check
            .doOnNext(nextPageAvailableCommand.consumer)
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
    }

    private fun nextPage(i: Unit) {
        var currentPage = currentPageState.value
        if (currentPage < items.value.size - 1)
            currentPageState.consumer.accept(++currentPage)
        else
            saveAccount()
    }

    private fun prevPage(i: Unit) {
        currentPageState.consumer.accept(currentPageState.value - 1)
    }

    private fun saveAccount() {}
}