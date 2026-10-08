package com.elta.android.presentation.features.main.records.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentMainRecordsBinding
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.features.main.records.ui.compose.DashboardSyncState
import com.elta.android.presentation.features.main.records.ui.compose.DashboardSyncTarget
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardAction
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardScreen
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardUiState
import com.elta.android.presentation.features.main.records.ui.compose.NewDesignPaletteController
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenTransparentStatusBarConfigProvider
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleVisibilityState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.bindTo
import org.threeten.bp.YearMonth
import javax.inject.Inject

class MainRecordsFragment :
    BaseFragment<MainRecordsPm, FragmentMainRecordsBinding>(FragmentMainRecordsBinding::inflate) {

    @Inject
    lateinit var bus: RxBus

    override val screenLayout: Int = R.layout.fragment_main_records
    override val classToken: Class<MainRecordsPm> = MainRecordsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        MainScreenTransparentStatusBarConfigProvider
    override val backgroundColor: Int = R.color.white

    private val secondaryProvider: StatusBarConfigProvider = MainScreenLightStatusBarConfigProvider
    private val bottomSheetState = BehaviorRelay.createDefault(false)
    private val headerState = BehaviorRelay.createDefault(true)
    private var lastMainScreenConfig: ScreenEntity? = null
    private var dashboardUiState by mutableStateOf<GlucoseDashboardUiState?>(null)
    private var detailedEventsByMonth by mutableStateOf<Map<YearMonth, List<EventV2>>>(emptyMap())
    private var syncState = DashboardSyncState("")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dashboardView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.dashboardView.setContent {
            dashboardUiState?.let { uiState ->
                GlucoseDashboardScreen(
                    uiState = uiState,
                    loadedEventsByMonth = detailedEventsByMonth,
                    syncState = syncState.asUiState(),
                    onAction = ::onDashboardAction,
                    onDebugPaletteSelected = { palette ->
                        NewDesignPaletteController.select(palette)
                        bus.event(Events.NewDesignPaletteChanged)
                    }
                )
            }
        }
    }

    override fun onBindPresentationModel(pm: MainRecordsPm) {
        super.onBindPresentationModel(pm)

        fun applyMainScreenConfig(config: ScreenEntity?) {
            config ?: return
            binding.mainScreenStateView.updateFromConfig(
                title = config.title,
                description = config.description,
                imageUrl = config.backgroundImageUrl
            )
        }

        pm.dashboardState.bindTo {
            syncState.updateDisplayedTime(it.syncTimeText)
            dashboardUiState = it
        }
        pm.detailedEventsByMonth.bindTo { detailedEventsByMonth = it }
        compositeUnbind.add(
            bus.events<Events.Sync>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event -> syncState.handle(event, viewLifecycleOwner.lifecycleScope) }
        )
        pm.mainScreenConfig.bindTo { config ->
            lastMainScreenConfig = config
            applyMainScreenConfig(config)
        }
        pm.mainScreenState.bind(binding.mainScreenStateView, compositeUnbind)
        compositeUnbind.add(
            pm.mainScreenState.dataState.observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ applyMainScreenConfig(lastMainScreenConfig) }, {})
        )
        compositeDestroy.add(
            pm.mainScreenState.visibilityState.observable
                .map { !it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { binding.dashboardView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE) },
                    {}
                )
        )
        compositeDestroy.add(
            bus.events<Events.HomeBottomSheetStateChanged>()
                .map { it.opened }
                .subscribe(bottomSheetState)
        )
        compositeDestroy.add(
            bus.events<Events.RecordsAttachedStateChanged>()
                .map { it.attached }
                .subscribe(headerState)
        )
        compositeDestroy.add(
            Observables.combineLatest(bottomSheetState, headerState).subscribe { (bottomSheetVisible, headerVisible) ->
                when {
                    bottomSheetVisible -> statusBarConfigProvider.applyStatusBarConfig()
                    !headerVisible -> secondaryProvider.applyStatusBarConfig()
                    else -> statusBarConfigProvider.applyStatusBarConfig()
                }
            }
        )
    }

    override fun onDestroyView() {
        dashboardUiState = null
        detailedEventsByMonth = emptyMap()
        syncState = DashboardSyncState("")
        super.onDestroyView()
    }

    private fun onDashboardAction(action: GlucoseDashboardAction) {
        when (action) {
            GlucoseDashboardAction.RequestSync -> if (!syncState.isSyncing) {
                bus.event(Events.ManualGlucometerSyncRequested)
            }
            is GlucoseDashboardAction.RetrySync -> if (!syncState.isSyncing) {
                bus.event(
                    if (action.target == DashboardSyncTarget.SERVER) Events.ServerSyncRequested
                    else Events.ManualGlucometerSyncRequested
                )
            }
            GlucoseDashboardAction.DismissSyncMessage -> syncState.dismiss()
            GlucoseDashboardAction.ConnectDevice -> bus.event(Events.DashboardConnectDeviceRequested)
            is GlucoseDashboardAction.OpenDevice -> bus.event(
                Events.DashboardDeviceInfoRequested(action.device.name, action.device.address)
            )
            is GlucoseDashboardAction.RequestDetailedRange -> {
                var month = YearMonth.from(action.start)
                val lastMonth = YearMonth.from(action.end)
                while (!month.isAfter(lastMonth)) {
                    bus.event(Events.DetailedChartRangeRequested(month.atDay(1), month.atEndOfMonth()))
                    month = month.plusMonths(1)
                }
            }
            is GlucoseDashboardAction.SelectCategory -> Unit
        }
    }

    companion object {
        fun newInstance() = MainRecordsFragment()
    }
}
