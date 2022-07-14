package com.elta.android.presentation.features.main.events.chooser.chooserWithSubtypes.pm

import com.elta.android.domain.features.diary.chooser.interactor.GetChooserOptionsUseCase
import com.elta.android.domain.features.diary.chooser.model.ChooserType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.chooser.builder.ChooserOptionsItemsBuilder
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class EventsOptionsChooserWithSubtypesPm @Inject constructor(
    private val getChooserOptionsUseCase: GetChooserOptionsUseCase,
    private val itemsBuilder: ChooserOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    val toolbarTitleCommand = state<String>()
    val appBarBackgroundCommand = state<Int>()

    private val configurationState = state<ChooserConfiguration>()
    private val loadChooserOptionsAction = action<ChooserConfiguration>()

    override fun onCreate() {
        super.onCreate()

        loadChooserOptionsAction.observable
            .map(::createParams)
            .flatMap {
                getChooserOptionsUseCase.execute(it)
                    .hideErrorContainer()
                    .bindProgress()
                    .map { options ->
                        itemsBuilder.buildItems(configurationState.value, options)
                    }
                    .doOnNext(items.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        configurationState.observable
            .doOnNext(loadChooserOptionsAction.consumer)
            .subscribe()
            .untilDestroy()
    }

    fun setConfiguration(configuration: ChooserConfiguration) {
        configurationState.consumer.accept(configuration)
    }

    private fun setUpToolbarTitle(configuration: ChooserConfiguration) {
        toolbarTitleCommand.consumer.accept(
            resources.getString(
                when {
                    configuration.chooserType == ChooserType.VARIANTS_WITH_SUBTYPE &&
                            configuration.eventType == EventType.INSULIN ->
                        R.string.events_options_chooser_title_insulin
                    else ->
                        R.string.events_options_chooser_title_tags
                }
            )
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

    private fun createParams(chooserConfiguration: ChooserConfiguration): GetChooserOptionsUseCase.Params =
        GetChooserOptionsUseCase.Params(
            chooserConfiguration.eventType,
            chooserConfiguration.chooserType
        )
}