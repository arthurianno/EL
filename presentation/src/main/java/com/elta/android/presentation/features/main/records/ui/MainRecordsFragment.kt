package com.elta.android.presentation.features.main.records.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenTransparentStatusBarConfigProvider
import com.elta.android.presentation.widgets.decoration.MainScreenMarginItemDecoration
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleVisibilityState
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_main_records.*
import javax.inject.Inject

class MainRecordsFragment : BaseListFragment<MainRecordsPm>() {

    @Inject
    lateinit var bus: RxBus

    override val screenLayout: Int = R.layout.fragment_main_records
    override val classToken: Class<MainRecordsPm> = MainRecordsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        MainScreenTransparentStatusBarConfigProvider
    override val backgroundColor: Int = R.color.pale_gray

    private val secondaryProvider: StatusBarConfigProvider = MainScreenLightStatusBarConfigProvider
    private val bottomSheetState = BehaviorRelay.createDefault(false)
    private val headerState = BehaviorRelay.createDefault(true)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsView?.addItemDecoration(
            MainScreenMarginItemDecoration(
                checkNotNull(context),
                R.dimen.overlap_first_item_margin
            )
        )
    }

    override fun onBindPresentationModel(pm: MainRecordsPm) {
        super.onBindPresentationModel(pm)
        pm.mainScreenState.bind(mainScreenStateView, compositeUnbind)
        pm.mainScreenState
            .visibilityState
            .observable.map { !it }
            .subscribe {
                itemsView?.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            }

        bus.events<Events.HomeBottomSheetStateChanged>().map { it.opened }
            .subscribe(bottomSheetState)

        bus.events<Events.RecordsAttachedStateChanged>().map { it.attached }
            .subscribe(headerState)

        Observables.combineLatest(bottomSheetState, headerState)
            .subscribe {
                val bottomSheetVisible = it.first
                val headerVisible = it.second
                if (bottomSheetVisible) {
                    statusBarConfigProvider.applyStatusBarConfig()
                } else if (!headerVisible) {
                    secondaryProvider.applyStatusBarConfig()
                } else {
                    statusBarConfigProvider.applyStatusBarConfig()
                }
            }
    }

    companion object {
        fun newInstance() = MainRecordsFragment()
    }
}
