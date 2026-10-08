package com.elta.android.presentation.features.statistic.period.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentStatisticPeriodBinding
import com.elta.android.presentation.features.statistic.flow.ui.StatisticFlowFragment
import com.elta.android.presentation.features.statistic.period.pm.PeriodPm
import com.elta.android.presentation.features.statistic.period.model.DEFAULT_VISIBLE_STATISTICS_BLOCKS
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import com.elta.android.presentation.features.statistic.period.ui.compose.StatisticsDashboardScreen
import com.elta.android.presentation.features.statistic.period.ui.compose.toStatisticsDashboardUiState
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.utils.bundle
import me.dmdev.rxpm.bindTo

class PeriodFragment :
    BaseFragment<PeriodPm, FragmentStatisticPeriodBinding>(
        FragmentStatisticPeriodBinding::inflate
    ) {

    override val screenLayout: Int = R.layout.fragment_statistic_period
    override val classToken: Class<PeriodPm> = PeriodPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider? = null

    override val backgroundColor: Int? = null

    private var selectedPeriod: Period = Period.SEVEN
    private var uiState by mutableStateOf(null.toStatisticsDashboardUiState(Period.SEVEN))
    private var visibleBlocks by mutableStateOf<List<StatisticsBlock>>(DEFAULT_VISIBLE_STATISTICS_BLOCKS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedPeriod = arguments?.getSerializable(EXTRA_PERIOD) as? Period ?: Period.SEVEN
        presentationModel.setPeriod(selectedPeriod)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.statisticsComposeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.statisticsComposeView.setContent {
            EltaTheme {
                StatisticsDashboardScreen(
                    uiState = uiState,
                    visibleBlocks = visibleBlocks,
                    onVisibleBlocksSaved = presentationModel::saveVisibleBlocks,
                    onPeriodSelected = { period ->
                        (parentFragment as? StatisticFlowFragment)?.selectPeriod(period)
                    },
                    onBack = { router.exit() },
                    onExport = {
                        (parentFragment as? StatisticFlowFragment)?.openReportChooser()
                    },
                )
            }
        }
    }

    override fun onBindPresentationModel(pm: PeriodPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        pm.statisticsByPeriodState.bindTo { models ->
            uiState = models.current.toStatisticsDashboardUiState(
                selectedPeriod = selectedPeriod,
                previous = models.previous
            )
        }
        pm.visibleBlocksState.bindTo { visibleBlocks = it }
    }

    companion object {
        private const val EXTRA_PERIOD = "extra_period"
        fun newInstance(period: Period): PeriodFragment {
            return PeriodFragment().apply {
                arguments = bundle(EXTRA_PERIOD to period)
            }
        }
    }
}
