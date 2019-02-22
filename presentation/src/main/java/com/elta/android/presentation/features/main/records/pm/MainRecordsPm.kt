package com.elta.android.presentation.features.main.records.pm

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.elta.android.presentation.utils.getGreetingText
import com.elta.android.presentation.utils.toIconWithBg
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@Suppress("MagicNumber", "ForEachOnRange")
class MainRecordsPm @Inject constructor(
    private val getHomeModelUseCase: GetHomeModelUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val mainScreenState = stateControl()

    private val loadScreenAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        items.consumer.accept(arrayListOf<ListItem>().apply {
            add(
                RecordsHeaderItem(
                    null,
                    1.2,
                    R.drawable.ic_change_index_down,
                    4.5,
                    6.2
                )
            )
            (0..5).forEach { group ->
                add(
                    RecordsGroupItem(
                        id = group,
                        icon = R.drawable.ic_event_medicine,
                        name = "Name #$group",
                        items = arrayListOf<ListItem>().apply {
                            EventType.values().forEachIndexed { index, event ->
                                add(
                                    RecordItem(
                                        id = index,
                                        icon = event.toIconWithBg(),
                                        title = "Title #$event",
                                        type = resources.getString(event.toName()),
                                        count = "$index ед",
                                        date = "12:00",
                                        showLabel = index % 2 == 0
                                    )
                                )
                            }
                        }
                    )
                )
            }
        })

        loadScreenAction.observable
            .skipWhileInProgress()
            .flatMap { params ->
                getHomeModelUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .doOnNext { loadScreenAction.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()

        bindMainScreenState()
    }

    private fun bindMainScreenState() {
        mainScreenState.dataState.consumer.accept(makeNewDayLaunchState())
        mainScreenState.visibilityState.consumer.accept(false)
    }

    private fun makeFirsLaunchState() =
        States.MainRecordsScreenFirstLaunchState(resources)

    private fun makeNewDayLaunchState() =
        States.MainRecordsScreenNewDayState(
            resources,
            title = Calendar.getInstance().getGreetingText(resources)
        )

    private fun handleSuccess(model: HomeModel) {
        Timber.d(model.toString())
    }
}