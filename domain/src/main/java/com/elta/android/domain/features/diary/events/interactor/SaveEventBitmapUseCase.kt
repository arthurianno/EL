package com.elta.android.domain.features.diary.events.interactor

import android.graphics.Bitmap
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class SaveEventBitmapUseCase @Inject constructor(
    private val eventsRepo: EventsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<File, SaveEventBitmapUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<File> {
        val p = checkNotNull(params)
        return eventsRepo.saveEventBitmap(p.eventHash, p.path, p.bitmap)
    }

    data class Params(val eventHash: String, val path: String, val bitmap: Bitmap)
}