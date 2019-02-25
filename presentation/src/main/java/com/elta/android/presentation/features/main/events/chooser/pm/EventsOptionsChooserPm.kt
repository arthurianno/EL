package com.elta.android.presentation.features.main.events.chooser.pm

import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserHeaderItem
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

@Suppress("MagicNumber", "ForEachOnRange", "LabeledExpression")
class EventsOptionsChooserPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val toolbarTitleCommand = State<String>()
    val appBarBackgroundCommand = State<Int>()
    val confirmButtonVisibilityCommand = Command<Boolean>(bufferSize = 1)

    private val selectedItemIdState = State(NONE_ID)
    private val configurationState = State<ChooserConfiguration>()

    override fun onCreate() {
        super.onCreate()

        configurationState.observable
            .doOnNext(::setUpToolbarTitle)
            .doOnNext(::setUpAppBarBackground)
            .doOnNext { items.consumer.accept(addMockItems()) } // TODO test case
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
    }

    // TODO replace
    private fun addMockItems(): List<ListItem> =
        arrayListOf<ListItem>().apply {
            add(ChooserHeaderItem(configurationState.value.toHeaderTitle()))
            (0..20).forEach {
                add(
                    ChooserItem(
                        id = it.toString(),
                        title = "Option $it",
                        iconId = R.drawable.ic_event_medicine_with_bg
                    )
                )
            }
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

    // TODO this backgrounds can be changed
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

    private fun ChooserConfiguration.toHeaderTitle(): String =
        resources.getString(
            when {
                chooserType == ChooserType.VARIANTS && eventType == EventType.INSULIN ->
                    R.string.events_options_chooser_header_variants
                else -> R.string.events_options_chooser_header_tags
            }
        )

    companion object {
        private const val NONE_ID = "none_id"
    }
}