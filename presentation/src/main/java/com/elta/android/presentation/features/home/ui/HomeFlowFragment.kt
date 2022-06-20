package com.elta.android.presentation.features.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.bindTo
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.core.ui.dialog.buttons
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.databinding.FragmentHomeFlowBinding
import com.elta.android.presentation.features.home.pm.HomeFlowPm
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.utils.makeSnackBarWithAction
import com.elta.android.presentation.widgets.BottomNavigationView
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.adapter.DynamicAdapter
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.Observables
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.DialogControl
import me.dmdev.rxpm.widget.bindTo
import javax.inject.Inject

class HomeFlowFragment :
    BaseFlowFragment<HomeFlowPm, FragmentHomeFlowBinding>(FragmentHomeFlowBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_home_flow
    override val classToken: Class<HomeFlowPm> = HomeFlowPm::class.java

    @Inject
    lateinit var adapter: DynamicAdapter

    @Inject
    lateinit var bus: RxBus

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.getInt(KEY_SELECTED_MENU_ID)
            ?.passTo(presentationModel.menuItemRestoredAction)
        initBottomSheetItemsView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view?.findViewById<BottomNavigationView>(R.id.homeBottomNavigationView)?.selectedId?.let {
            outState.putInt(KEY_SELECTED_MENU_ID, it)
        }
    }

    override fun onBindPresentationModel(pm: HomeFlowPm) {
        super.onBindPresentationModel(pm)
        binding.homeActionView.clicks()
            .subscribe { binding.homeActionView.isSelected.not().passTo(pm.homeAction) }
        pm.selectedItemIdState.bindTo(binding.homeBottomNavigationView.selection())
        pm.bottomSheetItems.observable.bindTo(adapter, compositeUnbind)
        pm.closeBottomSheetCommand.bindTo { binding.homeBottomSheetView.hide() }
        pm.showBottomSheetCommand.bindTo { binding.homeBottomSheetView.show() }
        Observables.combineLatest(
            pm.pulseCommand.observable,
            pm.selectedItemIdState.observable.map { it == R.id.mainMenuItemView }
        )
            .map { it.first && it.second }
            .distinctUntilChanged()
            .subscribe {
                with(binding) {
                    if (it) {
                        homePulseView.show()
                        homePulseView.start()
                    } else {
                        homePulseView.stop()
                        homePulseView.hide()
                    }
                }
            }
        binding.homeBottomSheetView.visibilityChanges().subscribe { visible ->
            binding.homeActionView.isSelected = visible
            bus.event(Events.HomeBottomSheetStateChanged(visible))
        }
        binding.homeBottomNavigationView.tabClicks().bindTo(pm.menuItemSelectedAction)
        pm.retryDeviceNotFoundControl.bindTo { data, sc ->
            makeSnackBarWithAction(
                binding.root,
                data,
                sc
            )
        }
        pm.btControl.bindTo(compositeUnbind, rxPermissions, this)

        pm.likeAppDialogControl.bindLikeAppDialog()
        pm.googlePlayDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.feedbackDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    override fun handleBack() {
        if (!binding.homeBottomSheetView.handleBack()) {
            super.handleBack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presentationModel.btControl.resolveResults(requestCode, resultCode)
    }

    private fun initBottomSheetItemsView() {
        binding.homeBottomSheetView.findViewById<RecyclerView>(R.id.bottomSheetItemsView).apply {
            layoutManager = FixedLinearLayoutManager(requireActivity())
            adapter = this@HomeFlowFragment.adapter
        }
    }

    private fun DialogControl<DialogData, DialogResult>.bindLikeAppDialog() =
        bindTo { data, dc ->
            val dialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_like_app_dialog, null)
                    .apply {
                        findViewById<AppCompatTextView>(R.id.titleView).text = data.title
                        findViewById<TextView>(R.id.contentView).text = data.message
                    }
            MaterialDialog.Builder(requireActivity())
                .customView(dialogView, false)
                .cancelable(false)
                .buttons(dc, data)
                .build()
        }

    companion object {
        fun newInstance() = HomeFlowFragment()
        private const val KEY_SELECTED_MENU_ID = "key_selected_menu_id"
    }
}
