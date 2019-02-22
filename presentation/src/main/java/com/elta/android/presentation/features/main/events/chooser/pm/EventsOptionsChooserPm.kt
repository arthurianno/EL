package com.elta.android.presentation.features.main.events.chooser.pm

import com.elta.android.domain.features.events.model.UserEvent
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserType
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserHeaderItem
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

@Suppress("MagicNumber", "ForEachOnRange")
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
                val id = it.id
                if (id == selectedItemIdState.value) NONE_ID
                else id
            }
            .doOnNext(selectedItemIdState.consumer)
            .subscribe()
            .untilDestroy()
    }

    // TODO replace
    private fun addMockItems(): List<ListItem> =
        arrayListOf<ListItem>().apply {
            add(ChooserHeaderItem(configurationState.value.toHeaderTitle()))
            (0..10).forEach {
                add(
                    ChooserItem(
                        id = it.toString(),
                        title = "Option $it",
                        iconId = R.drawable.ic_event_medicine_with_bg
                    )
                )
            }
        }

    // TODO IMPROVE????
    private fun performSelection(id: String) {
        val copy = addMockItems()
        copy.map {
            if (it is ChooserItem) {
                it.isSelected = it.id.equals(id, true)
            }
            it
        }
        items.consumer.accept(copy)
    }

    private fun setUpToolbarTitle(configuration: ChooserConfiguration) {
        toolbarTitleCommand.consumer.accept(
            resources.getString(
                when {
                    configuration.chooserType == ChooserType.VARIANTS &&
                        configuration.eventType == UserEvent.INSULIN ->
                        R.string.events_options_chooser_title_insulin
                    configuration.chooserType == ChooserType.VARIANTS &&
                        configuration.eventType == UserEvent.ACTIVITY ->
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
                UserEvent.XE -> R.drawable.bg_gradient_bread
                UserEvent.ACTIVITY -> R.drawable.bg_gradient_activity
                UserEvent.WEIGHT -> R.drawable.bg_gradient_weight
                UserEvent.MEDICINE -> R.drawable.bg_gradient_medicine
                UserEvent.INSULIN -> R.drawable.bg_gradient_insulin
            }
        )
    }

    private fun ChooserConfiguration.toHeaderTitle(): String =
        resources.getString(
            when {
                chooserType == ChooserType.VARIANTS && eventType == UserEvent.INSULIN ->
                    R.string.events_options_chooser_header_variants
                else -> R.string.events_options_chooser_header_tags
            }
        )

    companion object {
        private const val NONE_ID = "none_id"
    }
}