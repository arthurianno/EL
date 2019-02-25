package com.elta.android.domain.features.diary.chooser.interactor

import com.elta.android.domain.features.diary.chooser.model.ChooserOptionModel
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.tags.repository.TagsRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetChooserOptionsUseCase @Inject constructor(
    private val tagsRepo: TagsRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<ChooserOptionModel, GetChooserOptionsUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Observable<List<ChooserOptionModel>> {
        val p = checkNotNull(params)
        return buildChooserOptions(p.eventType, p.chooserType, tagsRepo)
    }

    data class Params(
        val eventType: EventType,
        val chooserType: ChooserType
    )
}