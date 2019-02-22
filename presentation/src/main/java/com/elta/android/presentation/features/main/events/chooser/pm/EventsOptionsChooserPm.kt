package com.elta.android.presentation.features.main.events.chooser.pm

import com.elta.android.domain.features.events.model.UserEvent
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserType
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserHeaderItem
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class EventsOptionsChooserPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    private val configurationState = State<ChooserConfiguration>()
    val toolbarTitleCommand = Command<String>()
    val appBarBackgroundCommand = Command<Int>()

    override fun onCreate() {
        super.onCreate()

        configurationState.observable
            .doOnNext(::setUpToolbarTitle)
            .doOnNext(::setUpAppBarBackground)
            .doOnNext { items.consumer.accept(addMockItems()) } // TODO test case
            .subscribe()
            .untilDestroy()
    }

    fun setConfiguration(configuration: ChooserConfiguration) {
        configurationState.consumer.accept(configuration)
    }

    // TODO replace
    private fun addMockItems(): List<ListItem> =
        arrayListOf(
            ChooserHeaderItem(configurationState.value.toHeaderTitle())
        )

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
}