package com.elta.android.presentation.features.main.events.glucose.pm

import android.net.Uri
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.interactor.GetEventByIdUseCase
import com.elta.android.domain.features.diary.events.interactor.GetShareEventUriUseCase
import com.elta.android.domain.features.diary.events.interactor.SaveEventBitmapUseCase
import com.elta.android.domain.features.diary.events.interactor.UpdateEventUseCase
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.home.interactor.glucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.formSelectorControl
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getFormattedTemperature
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getTag
import com.elta.android.presentation.features.main.events.edit.pm.mapper.getValue
import com.elta.android.presentation.features.main.events.glucose.model.GlucoseFormModel
import com.elta.android.presentation.features.main.events.glucose.share.ShareImageBuilder
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toEventDate
import com.elta.android.presentation.utils.toEventTime
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Singles
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import me.dmdev.rxpm.widget.inputControl
import org.threeten.bp.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val OPEN_SCREEN_DELAY_MILLIS = 300L

@Suppress("TooManyFunctions")
class GlucoseEventPm @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val getShareEventUriUseCase: GetShareEventUriUseCase,
    private val saveEventBitmapUseCase: SaveEventBitmapUseCase,
    private val shareImageBuilder: ShareImageBuilder,
    services: ServiceFacade
) : BasePm(services) {
    val glucoseValueState = state<String>()
    val glucoseInfoState = state<String>()

    val glucoseLevelBackgroundState = state<Int>()
    val tagSelector = formSelectorControl()
    val dateSelector = formSelectorControl(false)
    val timeSelector = formSelectorControl(false)
    val noteInput = inputControl()

    val mealSelector = state(MealTag.NOT_SELECTED)
    val mainActionTitleState = state<String>()
    val mainActionVisibilityState = state(false)

    val mainAction = action<Unit>()
    val backHandleAction = action<Unit>()
    val exitDialogAction = action<Unit>()
    val shareAction = action<Unit>()
    val beforeMealAction = action<Unit>()

    val afterMealAction = action<Unit>()

    val exitDialogControl = dialogControl<DialogData, DialogResult>()
    private val selectedDateState = state<ZonedDateTime>()
    private val eventIdState = state<String>()
    private val glucoseLevelSettingsState = state<GlucoseLevelSettings>()

    private val eventState = state<Event>()

    private val eventFormHolderState = state(GlucoseFormModel())
    private val exitDialogData: DialogData by lazy { Dialogs.ExitAndLoseData(resources) }

    private val loadScreenAction = action<Unit>()

    override fun onCreate() {
        super.onCreate()
        mainActionTitleState.consumer.accept(resources.getString(R.string.event_form_save_updated_entry_title))
        bindFormTagSelection()
        bindMealTagsSelection()
        bindDateSelectors()
        bindHandleBack()
        observeEventChanges()
        observeSaveEventAction()
        bindShare()
        loadEvent()
    }

    private fun bindMealTagsSelection() {
        beforeMealAction.observable
            .doOnNext { switchMealTag(MealTag.BEFOREMEAL) }
            .subscribe()
            .untilDestroy()
        afterMealAction.observable
            .doOnNext { switchMealTag(MealTag.AFTERMEAL) }
            .subscribe()
            .untilDestroy()
    }

    private fun switchMealTag(mealTag: MealTag) {
        mealSelector.consumer.accept(
            if (mealSelector.value == MealTag.NOT_SELECTED) {
                mealTag
            } else {
                MealTag.NOT_SELECTED
            }
        )
    }

    fun setEventData(id: String) {
        eventIdState.consumer.accept(id)
    }

    private fun observeEventChanges() {
        Observables.combineLatest(
            tagSelector.option.observable,
            noteInput.text.observable
        ) { tag, note ->
            eventFormHolderState.value.copy(
                tag = tag.meta as? Tag,
                noteValue = note
            )
        }
            .doOnNext { eventFormHolderState.consumer.accept(it) }
            .map(::checkIsChanged)
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()

        mealSelector.observable
            .map { eventFormHolderState.value.copy(mealTag = if (it == MealTag.NOT_SELECTED) null else it) }
            .doOnNext { eventFormHolderState.consumer.accept(it) }
            .map(::checkIsChanged)
            .subscribe(mainActionVisibilityState.consumer)
            .untilDestroy()
    }

    private fun loadEvent() {
        loadScreenAction.observable
            .skipWhileInProgress()
            .map(::createGetEventUseCaseParams)
            .flatMapSingle {
                Singles.zip(
                    getEventByIdUseCase.execute(it),
                    getProfileUseCase.execute()
                )
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess(::handleScreenLoading)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        eventState.observable
            .take(1)
            .doOnNext(::bindEvent)
            .subscribe()
            .untilDestroy()

        eventIdState.observable
            .map { Unit }
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()
    }

    private fun handleScreenLoading(data: Pair<Event, Profile>) {
        glucoseLevelSettingsState.consumer.accept(data.second.glucoseLevelSettings)
        eventState.consumer.accept(data.first)
    }

    private fun bindEvent(event: Event) {
        glucoseValueState.consumer.accept(NumberFormatter.format(event.getValue()))
        glucoseInfoState.consumer.accept(
            resources.getString(
                R.string.event_form_glucose_info_mask_title,
                event.getFormattedTemperature()
            )
        )
        event.getTag(resources)?.let { tagSelector.option.consumer.accept(it) }
        selectedDateState.consumer.accept(event.additionTime)
        event.note?.let { noteInput.text.consumer.accept(it) }
        glucoseLevelBackgroundState.consumer.accept(
            event.glucoseLevel(glucoseLevelSettingsState.value).toBackground()
        )
        event.mealTag?.let { mealSelector.consumer.accept(it) }
    }

    private fun bindFormTagSelection() {
        tagSelector.clickAction.observable
            .debounceAction()
            .doOnNext { hideKeyBoardCommand.consumer.accept(Unit) }
            .delay(OPEN_SCREEN_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .map {
                ChooserConfiguration(
                    ChooserType.GROUP_TAGS,
                    EventType.GLUCOSE,
                    (tagSelector.option.value.meta as? Tag)?.id
                )
            }
            .subscribe { router.navigateTo(Screens.EventsChooserScreen(it)) }
            .untilDestroy()

        bus.events<Events.ChooserTagSelected>()
            .map { it.chooserResult.toSelectorOption() }
            .subscribe(tagSelector.option.consumer)
            .untilDestroy()
    }

    private fun bindDateSelectors() {
        selectedDateState.observable
            .map { it.toEventTime(resources).toSimpleSelectorOption() }
            .subscribe(timeSelector.option.consumer)
            .untilDestroy()

        selectedDateState.observable
            .map { it.toEventDate(resources).toSimpleSelectorOption() }
            .subscribe(dateSelector.option.consumer)
            .untilDestroy()
    }

    private fun checkIsChanged(eventFormModel: GlucoseFormModel): Boolean =
        eventState.valueOrNull?.isGlucoseEventChanged(
            tagId = eventFormModel.tag?.id,
            note = eventFormModel.noteValue,
            mealTag = eventFormModel.mealTag
        ) ?: false

    private fun observeSaveEventAction() {
        mainAction.observable
            .skipWhileInProgress()
            .map(::createEditEventParams)
            .flatMapCompletable { params ->
                updateEventUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete { handleSuccess() }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindShare() {
        shareAction.observable
            .map(::createGetShareEventUriUseCaseParams)
            .flatMapSingle {
                getShareEventUriUseCase.execute(it)
                    .flatMap { uri ->
                        when (uri != Uri.EMPTY) {
                            true -> Single.just(uri)
                            else ->
                                saveEventBitmapUseCase.execute(createSaveEventBitmapUseCaseParams())
                        }
                    }
                    .hideErrorContainer()
                    .bindProgress()
                    .trackEvent(AnalyticsEventType.SHARE_GLUCOSE)
                    .map { uri ->
                        Screens.ShareEventScreen(
                            uri,
                            resources.getString(R.string.event_share_dialog_title)
                        )
                    }
                    .doOnSuccess(router::navigateTo)
            }
            .subscribe()
            .untilDestroy()
    }

    private fun bindHandleBack() {
        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()

        exitDialogAction.observable
            .switchMapMaybe {
                exitDialogControl.showForResult(exitDialogData)
            }
            .filter { it == DialogResult.POSITIVE }
            .doOnNext { router.exit() }
            .subscribe()
            .untilDestroy()
    }

    private fun handleBack(i: Unit) {
        when (mainActionVisibilityState.value) {
            true -> exitDialogAction.consumer.accept(Unit)
            else -> router.exit()
        }
    }

    private fun handleSuccess() {
        bus.event(Events.EventsChanged(false))
        router.exit()
    }

    private fun createGetEventUseCaseParams(i: Unit) =
        GetEventByIdUseCase.Params(eventIdState.value)

    private fun createEditEventParams(i: Unit): UpdateEventUseCase.Params {
        val form = eventFormHolderState.value
        return UpdateEventUseCase.Params(
            event = eventState.value.copy(
                tagId = form.tag?.id,
                tag = form.tag,
                note = form.noteValue,
                mealTag = form.mealTag
            )
        )
    }

    private fun createGetShareEventUriUseCaseParams(i: Unit) =
        GetShareEventUriUseCase.Params(eventState.value, glucoseLevelSettingsState.value)

    private fun createSaveEventBitmapUseCaseParams() =
        SaveEventBitmapUseCase.Params(
            event = eventState.value,
            glucoseLevelSettings = glucoseLevelSettingsState.value,
            bitmap = shareImageBuilder.createBitmap(
                eventState.value,
                glucoseLevelSettingsState.value
            )
        )

    private fun Event.isGlucoseEventChanged(
        tagId: String?,
        note: String?,
        mealTag: MealTag?
    ): Boolean = this.tagId != tagId || this.note.orEmpty() != note || this.mealTag != mealTag

    private fun ChooserResult.toSelectorOption() =
        SelectorOption(
            text = name,
            icon = iconId?.let { id -> resources.getDrawable(id) },
            meta = meta
        )

    private fun String.toSimpleSelectorOption() =
        SelectorOption(this)

    private fun GlucoseLevel.toBackground(): Int =
        when {
            this == GlucoseLevel.HIGH -> R.drawable.bg_gradient_red
            this == GlucoseLevel.LOW -> R.drawable.bg_gradient_blue
            else -> R.drawable.bg_gradient_green
        }
}
