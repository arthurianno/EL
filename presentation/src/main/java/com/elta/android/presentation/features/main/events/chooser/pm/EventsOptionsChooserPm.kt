package com.elta.android.presentation.features.main.events.chooser.pm

import com.elta.android.domain.features.diary.chooser.interactor.GetChooserOptionsUseCase
import com.elta.android.domain.features.diary.chooser.interactor.GetMedicinesChooserOptionsUseCase
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.features.main.events.chooser.models.MedicamentChooser
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserWithSubtypeItem
import com.elta.android.presentation.features.main.events.chooser.ui.builder.ChooserOptionsItemsBuilder
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val NONE_ID = "none_id"
private const val CLICK_DELAY_MILLIS = 100L
private const val TIMES_EXIT_TO_LEAVE_CHOOSER = 2

@Suppress("MagicNumber", "ForEachOnRange", "LabeledExpression")
class EventsOptionsChooserPm @Inject constructor(
    private val getChooserOptionsUseCase: GetChooserOptionsUseCase,
    private val getMedicinesChooserOptionsUseCase: GetMedicinesChooserOptionsUseCase,
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
    private val chooserInsulinDefaultList = state<List<Medicament>>()

    private val loadChooserOptionsAction = action<ChooserConfiguration>()

    private var selectedItemId = NONE_ID

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
                    .map { items ->
                        items
                            .find { item -> item is ChooserItem && item.isSelected }
                            ?.let { chooserItem ->
                                selectedItemId = (chooserItem as ChooserItem).id
                                setPreviousSelection(configurationState.value)
                            }
                        items
                    }
                    .doOnNext(items.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        bindSelectionBehaviour()

        configurationState.observable
                .filter { it.chooserType == ChooserType.VARIANTS && it.eventType == EventType.INSULIN }
                .flatMap { chooserConfiguration ->
                    val insulinType = chooserConfiguration.medicament?.let { medicamentChooser ->
                        GetMedicinesChooserOptionsUseCase.Params(
                            MedicamentInsulinType(
                                id = medicamentChooser.insulinId,
                                code = medicamentChooser.insulinCode,
                                name = medicamentChooser.insulinName,
                            )
                        )
                    }
                    getMedicinesChooserOptionsUseCase.execute(insulinType)
                }
                .subscribe(chooserInsulinDefaultList.consumer)
                .untilDestroy()

    }

    fun setConfiguration(configuration: ChooserConfiguration) {
        configurationState.consumer.accept(configuration)
    }

    private fun bindSelectionBehaviour() {
        selectedItemIdState.observable
            .skip(1)
            .doOnNext { performSelection(selectedItemId) }
            .map { items.value.find { it is ChooserItem && it.isSelected } != null }
            .doOnNext(confirmButtonVisibilityCommand.consumer)
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ChooserOptionClicked>()
            .map {
                selectedItemId = it.item.id
                if (it.item.id == selectedItemIdState.value) NONE_ID
                else it.item.id
            }
            .throttleLatest(CLICK_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .doOnNext(selectedItemIdState.consumer)
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ChooserWithSubtypesOptionClicked>()
            .throttleLatest(CLICK_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .map {
                it.item.getConfigurator()
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
                        ChooserType.VARIANTS_WITH_SUBTYPE -> Events.ChooserVariantWithSubtypesSelected(it)
                    }
                )
            }
            .doOnNext {
                val configuration = configurationState.value
                if (configuration.eventType == EventType.INSULIN &&
                    configuration.chooserType == ChooserType.VARIANTS
                ) {
                    router.leaveChooser()
                } else {
                    router.exit()
                }
            }
            .subscribe()
            .untilDestroy()
    }

    private fun FlowRouter.leaveChooser() {
        repeat(TIMES_EXIT_TO_LEAVE_CHOOSER) { exit() }
    }

    private fun performSelection(id: String) {
        items.consumer.accept(
            items.value.map {
                return@map if (it is ChooserItem) {
                    it.copy(isSelected = it.id == id && !it.isSelected)
                } else it
            }
        )
    }

    private fun buildChooserResult(i: Unit): ChooserResult {
        val item = items.value.find { it is ChooserItem && it.id == selectedItemId }
        val chooserItem = item as? ChooserItem
        val medicinesInsulinType = getInsulinType(chooserInsulinDefaultList.valueOrNull)
        return ChooserResult(
            id = selectedItemId,
            name = getChooserResultName(chooserItem?.title, medicinesInsulinType),
            iconId = chooserItem?.iconId,
            meta = chooserItem?.meta
        )
    }

    private fun getInsulinType(medicines: List<Medicament>?): MedicamentInsulinType {
        val type = medicines?.find { it.id.toString() == selectedItemId }?.insulinType
        return type ?: MedicamentInsulinType.nullMedicament()
    }

    private fun getChooserResultName(title: String?, insulinTypeName: MedicamentInsulinType): String? {
        return if (configurationState.value.eventType == EventType.INSULIN) {
            "${insulinTypeName.name}(${title.orEmpty()})"
        } else {
            title
        }
    }

    private fun createParams(chooserConfiguration: ChooserConfiguration): GetChooserOptionsUseCase.Params =
        GetChooserOptionsUseCase.Params(
            chooserConfiguration.eventType,
            chooserConfiguration.chooserType,
            configurationState.valueOrNull?.medicament?.toInsulinType()
        )

    private fun setUpToolbarTitle(configuration: ChooserConfiguration) {
        val accept = when {
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
                configurationState.valueOrNull?.medicament?.insulinName
                    ?: resources.getString(R.string.events_options_chooser_title_tags)
            }

            else ->
                resources.getString(R.string.events_options_chooser_title_tags)
        }
        toolbarTitleCommand.consumer.accept(accept)
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

    private fun setPreviousSelection(configuration: ChooserConfiguration) {
        val accept = if (configuration.chooserType == ChooserType.VARIANTS) {
            configuration.medicament?.medicamentId?.toString() ?: NONE_ID
        } else {
            NONE_ID
        }

        previousSelectionState.consumer.accept(accept)
    }

    private fun MedicamentChooser.toInsulinType() = MedicamentInsulinType(
        code = insulinCode,
        id = insulinId,
        name = insulinName
    )

    private fun ChooserWithSubtypeItem.getConfigurator(): ChooserConfiguration {
        val insulinType = meta as MedicamentInsulinType
        val medicamentChooser =  if (medicament?.insulinCode == insulinType.code)
            medicament
        else
            MedicamentChooser(
                insulinCode = insulinType.code,
                insulinName = insulinType.name,
                insulinId = insulinType.id
            )

        return ChooserConfiguration(
            chooserType = ChooserType.VARIANTS,
            eventType = EventType.INSULIN,
            id = insulinType.code,
            medicament = medicamentChooser
        )
    }
}
