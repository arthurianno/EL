package com.elta.android.domain.features.diary.events.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.events.model.State
import com.elta.android.domain.features.diary.events.repository.EventsRepository
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.tags.model.Tag
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import org.threeten.bp.ZonedDateTime
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
            EventV2(
                id = UUID.randomUUID().toString(),
                additionTime = date,
                tagId = p.tag?.id,
                tag = p.tag,
                note = p.note,
                modificationTime = null,
                value = p.value,
                name = p.name,
                kind = p.kind,
                temperature = null,
                duration = p.duration,
                activityType = p.activity,
                insulinMedicament = p.insulinMedicament,
                medicament = p.medicament,
                tabletsNumber = p.tabletsNumber,
                type = p.eventType,
                mealTag = p.mealTag,
                state = State.CREATED,
                glucometerSerialNumber = p.glucometerSerialNumber,
                dishes = p.dishes,
                glucoseInputType = p.glucoseInputType
            )
        )
    }

    data class Params(
        val value: Double? = null,
        val kind: String? = null,
        val name: String? = null,
        val duration: Long? = null,
        val date: ZonedDateTime? = null,
        val tag: Tag? = null,
        val insulinMedicament: InsulinMedicament? = null,
        val medicament: Medicament? = null,
        val tabletsNumber: Double? = null,
        val activity: ActivityType? = null,
        val note: String? = null,
        val glucometerSerialNumber: String?,
        val eventType: EventType,
        val dishes: List<Dish> = emptyList(),
        val glucoseInputType: GlucoseInputType? = null,
        val mealTag: MealTag? = null
    )
}
