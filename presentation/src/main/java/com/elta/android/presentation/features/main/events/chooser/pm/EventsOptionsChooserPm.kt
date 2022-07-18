package com.elta.android.presentation.features.main.events.chooser.pm

import com.elta.android.domain.features.diary.chooser.interactor.GetChooserOptionsUseCase
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.elta.android.presentation.features.main.events.chooser.ui.builder.ChooserOptionsItemsBuilder
import com.elta.android.presentation.utils.toName
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Suppress("MagicNumber", "ForEachOnRange", "LabeledExpression")
class EventsOptionsChooserPm @Inject constructor(
    private val getChooserOptionsUseCase: GetChooserOptionsUseCase,
    private val itemsBuilder: ChooserOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val toolbarTitleCommand = state<String>()
    val appBarBackgroundCommand = state<Int>()
    val confirmButtonVisibilityCommand = command<Boolean>(bufferSize = 1)
    val selectionConfirmedAction = action<Unit>()

    private val selectedItemIdState = state(NONE_ID)
    private val previousSelectionState = state<String>()
    private val configurationState = state<ChooserConfiguration>()
    private val loadChooserOptionsAction = action<ChooserConfiguration>()

    override fun onCreate() {
        super.onCreate()

        configurationState.observable
            .doOnNext(::setUpAppBarBackground)
            .doOnNext(::setPreviousSelection)
            .doOnNext(::setUpToolbarTitle)
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
            .map { it != NONE_ID && it != previousSelectionState.valueOrNull }
            .doOnNext(confirmButtonVisibilityCommand.consumer)
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ChooserOptionClicked>()
            .map {
                if (it.item.id == selectedItemIdState.value) NONE_ID
                else it.item.id
            }
            .throttleLatest(CLICK_DELAY, TimeUnit.MILLISECONDS)
            .doOnNext(selectedItemIdState.consumer)
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ChooserWithSubtypesOptionClicked>()
            .throttleLatest(CLICK_DELAY, TimeUnit.MILLISECONDS)
            .map {
                ChooserConfiguration(ChooserType.VARIANTS, EventType.INSULIN, it.item.title)
            }
            .subscribe {
                router.navigateTo(Screens.EventsChooserScreen(it))
            }
            .untilDestroy()

        selectionConfirmedAction.observable
            .map(::buildChooserResult)
            .doOnNext {
                bus.event(
                    when (configurationState.value.chooserType) {
                        ChooserType.VARIANTS -> Events.ChooserVariantSelected(it)
                        ChooserType.GROUP_TAGS -> Events.ChooserTagSelected(it)
                        ChooserType.VARIANTS_WITH_SUBTYPE -> Events.ChooserVariantWithSubtypesSelected(
                            it
                        )
                    }
                )
            }
            .doOnNext {
                val configuration = configurationState.value
                if (configuration.eventType == EventType.INSULIN &&
                    configuration.chooserType == ChooserType.VARIANTS
                ) {
                    router.exit()
                }
                router.exit()
            }
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
        val item = items.value.find { it is ChooserItem && it.id == selectedItemId }
        val chooserItem = item as? ChooserItem
        return ChooserResult(
            id = selectedItemId,
            name = getChooserResultName(chooserItem),
            iconId = chooserItem?.iconId,
            meta = chooserItem?.meta
        )
    }

    private fun getChooserResultName(chooserItem: ChooserItem?) =
        if (configurationState.value.eventType == EventType.INSULIN &&
            previousSelectionState.value != NONE_ID
        ) {
            "${previousSelectionState.value}(${chooserItem?.title.orEmpty()})"
        } else {
            chooserItem?.title
        }

    private fun createParams(chooserConfiguration: ChooserConfiguration): GetChooserOptionsUseCase.Params =
        GetChooserOptionsUseCase.Params(
            chooserConfiguration.eventType,
            chooserConfiguration.chooserType,
            getInsulinTypeByString(previousSelectionState.value)
        )

    private fun setUpToolbarTitle(configuration: ChooserConfiguration) {
        toolbarTitleCommand.consumer.accept(
            when {
                configuration.chooserType == ChooserType.VARIANTS_WITH_SUBTYPE &&
                    configuration.eventType == EventType.INSULIN -> {
                    resources.getString(R.string.events_options_chooser_title_insulin)
                }
                configuration.chooserType == ChooserType.VARIANTS &&
                    configuration.eventType == EventType.ACTIVITY -> {
                    resources.getString(R.string.events_options_chooser_title_activities)
                }
                configuration.chooserType == ChooserType.VARIANTS &&
                    configuration.eventType == EventType.INSULIN -> {
                    previousSelectionState.valueOrNull
                        ?: resources.getString(R.string.events_options_chooser_title_tags)
                }
                else ->
                    resources.getString(R.string.events_options_chooser_title_tags)
            }
        )
    }

    private fun setUpAppBarBackground(configuration: ChooserConfiguration) {
        appBarBackgroundCommand.consumer.accept(
            when (configuration.eventType) {
                EventType.BREAD -> R.color.color_chooser_bg_bread
                EventType.ACTIVITY -> R.color.color_chooser_bg_activity
                EventType.WEIGHT -> R.color.color_chooser_bg_weight
                EventType.MEDICAMENTS -> R.color.color_chooser_bg_medicaments
                EventType.INSULIN -> R.color.color_chooser_bg_insulin
                else -> R.color.color_chooser_bg_insulin
            }
        )
    }

    private fun getInsulinTypeByString(string: String) =
        when (string) {
            resources.getString(InsulinType.INTERMIDIATE.toName()) -> InsulinType.INTERMIDIATE
            resources.getString(InsulinType.ULTRASHORT.toName()) -> InsulinType.ULTRASHORT
            resources.getString(InsulinType.LONG.toName()) -> InsulinType.LONG
            resources.getString(InsulinType.MIXED.toName()) -> InsulinType.MIXED
            resources.getString(InsulinType.ULTRALONG.toName()) -> InsulinType.ULTRALONG
            resources.getString(InsulinType.SHORT.toName()) -> InsulinType.SHORT
            else -> null
        }

    private fun setPreviousSelection(configuration: ChooserConfiguration) {
        previousSelectionState.consumer.accept(
            if (configuration.chooserType == ChooserType.VARIANTS) {
                configuration.id
            } else {
                NONE_ID
            }
        )
    }

    companion object {
        private const val NONE_ID = "none_id"
        private const val CLICK_DELAY = 100L // millis
    }
}
