package com.elta.android.presentation.features.main.records.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentMainRecordsBinding
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardScreen
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardUiState
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
                    bus = bus
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

        pm.dashboardState.bindTo { dashboardUiState = it }
        pm.detailedEventsByMonth.bindTo { detailedEventsByMonth = it }
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
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = MainRecordsFragment()
    }
}
