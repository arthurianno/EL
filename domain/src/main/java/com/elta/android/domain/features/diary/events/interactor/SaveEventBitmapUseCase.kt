package com.elta.android.domain.features.diary.events.interactor

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class SaveEventBitmapUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Uri, SaveEventBitmapUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<Uri> {
        val p = checkNotNull(params)
        return eventsRepo.saveShareEventBitmap(p.event, p.glucoseLevelSettings, p.bitmap)
    }

    data class Params(val event: Event, val glucoseLevelSettings: GlucoseLevelSettings, val bitmap: Bitmap)
}