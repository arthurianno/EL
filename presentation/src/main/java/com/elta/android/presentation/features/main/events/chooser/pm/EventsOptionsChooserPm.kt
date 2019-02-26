package com.elta.android.presentation.features.main.events.chooser.pm

import com.elta.android.domain.features.diary.chooser.interactor.GetChooserOptionsUseCase
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.elta.android.presentation.features.main.events.chooser.ui.builder.ChooserOptionsItemsBuilder
import javax.inject.Inject

@Suppress("MagicNumber", "ForEachOnRange", "LabeledExpression")
class EventsOptionsChooserPm @Inject constructor(
    private val getChooserOptionsUseCase: GetChooserOptionsUseCase,
    private val itemsBuilder: ChooserOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val toolbarTitleCommand = State<String>()
    val appBarBackgroundCommand = State<Int>()
    val confirmButtonVisibilityCommand = Command<Boolean>(bufferSize = 1)
    val selectionConfirmedAction = Action<Unit>()

    private val selectedItemIdState = State(NONE_ID)
    private val configurationState = State<ChooserConfiguration>()
    private val loadChooserOptionsAction = Action<ChooserConfiguration>()

    override fun onCreate() {
        super.onCreate()

        configurationState.observable
            .doOnNext(::setUpToolbarTitle)
            .doOnNext(::setUpAppBarBackground)
            .doOnNext(loadChooserOptionsAction.consumer)
            .subscribe()
            .untilDestroy()

        loadChooserOptionsAction.observable
            .map(::createParams)
            .flatMap {
                getChooserOptionsUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .map { options -> itemsBuilder.buildItems(configurationState.value, options) }
                    .doOnNext(items.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        bindSelectionBehaviour()
    }

    fun setConfiguration(configuration: ChooserConfiguration) {
        configurationState.consumer.accept(configuration)
    }

    private fun bindSelectionBehaviour() {
        selectedItemIdState.observable
            .skip(1)
            .doOnNext(::performSelection)
            .map { it != NONE_ID }
            .doOnNext(confirmButtonVisibilityCommand.consumer)
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ChooserOptionClicked>()
            .map {
                if (it.id == selectedItemIdState.value) NONE_ID
                else it.id
            }
            .doOnNext(selectedItemIdState.consumer)
            .subscribe()
            .untilDestroy()

        selectionConfirmedAction.observable
            .map(::buildChooserResult)
            .doOnNext { bus.event(Events.ChooserOptionSelected(it)) }
            .doOnNext { router.exit() }
            .subscribe()
            .untilDestroy()
    }

    private fun performSelection(id: String) {
        items.consumer.accept(
            items.value.map {
                if (it is ChooserItem) {
                    return@map when {
                        it.isSelected -> it.copy(isSelected = false)
                        it.id == id -> it.copy(isSelected = true)
                        else -> it
                    }
                }
                return@map it
            }
        )
    }

    private fun buildChooserResult(i: Unit): ChooserResult {
        val selectedItemId = selectedItemIdState.value
        val item = items.value
            .find { it is ChooserItem && it.id == selectedItemId }
        return ChooserResult(selectedItemId, (item as? ChooserItem)?.title)
    }

    private fun createParams(chooserConfiguration: ChooserConfiguration): GetChooserOptionsUseCase.Params =
        GetChooserOptionsUseCase.Params(chooserConfiguration.eventType, chooserConfiguration.chooserType)

    private fun setUpToolbarTitle(configuration: ChooserConfiguration) {
        toolbarTitleCommand.consumer.accept(
            resources.getString(
                when {
                    configuration.chooserType == ChooserType.VARIANTS &&
                        configuration.eventType == EventType.INSULIN ->
                        R.string.events_options_chooser_title_insulin
                    configuration.chooserType == ChooserType.VARIANTS &&
                        configuration.eventType == EventType.ACTIVITY ->
                        R.string.events_options_chooser_title_activities
                    else ->
                        R.string.events_options_chooser_title_tags
                }
            )
        )
    }

    private fun setUpAppBarBackground(configuration: ChooserConfiguration) {
        appBarBackgroundCommand.consumer.accept(
            when (configuration.eventType) {
                EventType.BREAD -> R.drawable.bg_gradient_bread
                EventType.ACTIVITY -> R.drawable.bg_gradient_activity
                EventType.WEIGHT -> R.drawable.bg_gradient_weight
                EventType.MEDICAMENTS -> R.drawable.bg_gradient_medicine
                EventType.INSULIN -> R.drawable.bg_gradient_insulin
                else -> R.drawable.bg_gradient_insulin
            }
        )
    }

    companion object {
        private const val NONE_ID = "none_id"
    }
}