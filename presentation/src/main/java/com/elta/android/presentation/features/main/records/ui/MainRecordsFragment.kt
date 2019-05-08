package com.elta.android.presentation.features.main.records.ui

import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.main.records.pm.MainRecordsPm
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.records.ui.status_bar.MainScreenTransparentStatusBarConfigProvider
import com.elta.android.presentation.widgets.decoration.MainScreenMarginItemDecoration
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nullgr.core.rx.RxBus
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_main_records.*
import me.dmdev.rxpm.widget.DialogControl
import javax.inject.Inject

class MainRecordsFragment : BaseListFragment<MainRecordsPm>() {

    @Inject
    lateinit var bus: RxBus

    override val screenLayout: Int = R.layout.fragment_main_records
    override val classToken: Class<MainRecordsPm> = MainRecordsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = MainScreenTransparentStatusBarConfigProvider
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

        bus.events<Events.HomeBottomSheetStateChanged>().map { it.opened }
            .bindTo(bottomSheetState)

        bus.events<Events.RecordsAttachedStateChanged>().map { it.attached }
            .bindTo(headerState)

        Observables.combineLatest(bottomSheetState, headerState)
            .bindTo {
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

        pm.googlePlayDialogControl.bindDialog()
        pm.feedbackDialogControl.bindDialog()
    }

    private fun DialogControl<DialogData, MainRecordsPm.DialogResult>.bindDialog() =
        bindTo { data, dc ->
            MaterialDialog.Builder(checkNotNull(activity))
                .cancelable(false)
                .title(data.title)
                .content(data.message)
                .negativeText(data.negative)
                .positiveText(data.positive)
                .onPositive { _, _ -> dc.sendResult(MainRecordsPm.DialogResult.POSITIVE) }
                .onNegative { _, _ -> dc.sendResult(MainRecordsPm.DialogResult.NEGATIVE) }
                .build()
        }

    companion object {
        fun newInstance() = MainRecordsFragment()
    }
}
