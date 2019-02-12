package com.elta.android.domain.features.sale_points.interactor

import com.elta.android.domain.features.sale_points.model.SalePoint
import com.elta.android.domain.features.sale_points.repository.SalePointsRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class SearchSalePointsUseCase @Inject constructor(
    private val repository: SalePointsRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<SalePoint, SearchSalePointsUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<List<SalePoint>> =
        repository.searchSalePoints(checkNotNull(params).query)

    data class Params(val query: String)
}