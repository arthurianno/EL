package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.events.model.State
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.tags.model.Tag
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class AddNewEventUseCase @Inject constructor(
    private val repo: EventsRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<AddNewEventUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        val date = checkNotNull(p.date)
        return repo.addEvent(
            Event(
                id = UUID.randomUUID().toString(),
                additionTime = date,
                additionTimeString = date.toString(), // iso
                tagId = p.tag?.id,
                note = p.note,
                modificationTime = date,
                value = p.value,
                name = p.name,
                kind = p.kind,
                duration = p.duration,
                activityType = p.activity,
                insulinType = p.insulin,
                type = p.eventType,
                mealTag = null,
                state = State.CREATED
            )
        )
    }

    data class Params(
        val value: Double? = null,
        val kind: String? = null,
        val name: String? = null,
        val duration: Long? = null,
        val date: Date? = null,
        val tag: Tag? = null,
        val insulin: InsulinType? = null,
        val activity: ActivityType? = null,
        val note: String? = null,
        val eventType: EventType
    )
}