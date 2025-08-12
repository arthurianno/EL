package com.elta.android.presentation.features.home.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.core.ui.dialog.buttons
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.databinding.FragmentHomeFlowBinding
import com.elta.android.presentation.features.home.model.ManualSyncError
import com.elta.android.presentation.features.home.pm.HomeFlowPm
import com.elta.android.presentation.features.home.ui.adapter.HomeBottomSheetAdapter
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.utils.openSettingsIntent
import com.elta.android.presentation.widgets.BottomNavigationView
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.DialogControl
import me.dmdev.rxpm.widget.bindTo

private const val KEY_SELECTED_MENU_ID = "key_selected_menu_id"

class HomeFlowFragment : BaseFlowFragment<HomeFlowPm, FragmentHomeFlowBinding>(FragmentHomeFlowBinding::inflate) {
    companion object {
        fun newInstance() = HomeFlowFragment()
    }

    override val screenLayout: Int = R.layout.fragment_home_flow
    override val classToken: Class<HomeFlowPm> = HomeFlowPm::class.java

    @Inject
    lateinit var adapter: HomeBottomSheetAdapter

    @Inject
    lateinit var bus: RxBus

    private lateinit var rxPermissions: RxPermissions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Откладываем инициализацию RxPermissions
        view.post {
            if (isAdded && !requireActivity().supportFragmentManager.isStateSaved) {
                rxPermissions = RxPermissions(requireActivity())
            }
        }
        savedInstanceState?.getInt(KEY_SELECTED_MENU_ID)
            ?.passTo(presentationModel.menuItemRestoredAction)
        initBottomSheetItemsView()
    }

    override fun onBindPresentationModel(pm: HomeFlowPm) {
        super.onBindPresentationModel(pm)
        binding.homeActionView.clicks()
            .subscribe { binding.homeActionView.isSelected.not().passTo(pm.homeAction) }
        pm.selectedItemIdState.bindTo(binding.homeBottomNavigationView.selection())
        pm.bottomSheetItems.bindTo { adapter.submitList(it) }
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
        // Проверяем, инициализирован ли rxPermissions перед использованием
        if (::rxPermissions.isInitialized) {
            pm.btControl.bindTo(compositeDestroy, rxPermissions, this)
        } else {
            view?.post {
                if (::rxPermissions.isInitialized) {
                    pm.btControl.bindTo(compositeDestroy, rxPermissions, this)
                }
            }
        }
        pm.likeAppDialogControl.bindLikeAppDialog()
        pm.googlePlayDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.feedbackDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.glucoseDataReminderDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.openSettingsCommand.bindTo { openSettingsIntent(requireContext()) }
        bindHelpBottomSheet(pm)
    }

    private fun bindHelpBottomSheet(pm: HomeFlowPm) {
        pm.closeHelpBottomSheetCommand.bindTo { binding.helpBottomSheetView.hide() }
        pm.showHelpBottomSheetCommand.bindTo {
            binding.homeActionView.hide()
            binding.helpBottomSheetView.show()
        }
        binding.helpBottomSheetView.visibilityChanges().subscribe {
            binding.homeActionView.isVisible = !it
        }
        binding.helpBottomSheetView.findViewById<AppCompatTextView>(R.id.confirmButtonView)
            .clicks()
            .subscribe(pm.firstSyncAction.consumer)
        pm.glucoseFormat.bindTo {
            val (drawableId, textId, textSecondId) = when (it) {
                GlucoseFormat.CAPILLARY -> Triple(R.drawable.img_help_glucose_caplilary, R.string.on_boarding_glucose_format_event_text_capillary, R.string.on_boarding_glucose_format_event_text_second_capillary)
                GlucoseFormat.PLASMA -> Triple(R.drawable.img_help_glucose_plasma, R.string.on_boarding_glucose_format_event_text_plasma, R.string.on_boarding_glucose_format_event_text_second_plasma)
            }
            with(binding.helpBottomSheetView) {
                findViewById<ImageView>(R.id.image).setImageResource(drawableId)
                findViewById<TextView>(R.id.warning_text).setText(textId)
                findViewById<TextView>(R.id.warning_text_second).setText(textSecondId)
            }
        }

        pm.manualSyncError.bindTo { error ->
            with(binding.syncErrorBottomSheetView) {
                val title = when (error) {
                    ManualSyncError.ErrorSync -> R.string.sync_connection_sync_error_title
                    ManualSyncError.NotFound -> R.string.sync_connect_device_not_found
                    ManualSyncError.PermissionNotGranted -> R.string.sync_connect_device_check_permission
                }
                val errorTextView = findViewById<TextView>(R.id.error_sync_text)
                val confirmButton = findViewById<AppCompatTextView>(R.id.confirmButtonView)

                val (errorMessageId, buttonMessageId, action) =
                    if (error is ManualSyncError.PermissionNotGranted)
                        Triple(
                            R.string.sync_connection_permission_not_granted_description,
                            R.string.sync_connection_permission_not_granted_button,
                            pm.permissionSyncErrorAction
                        )
                    else
                        Triple(
                            R.string.sync_connection_error_text,
                            R.string.repeat_sync_button_text,
                            pm.manualSyncErrorAction
                        )

                errorTextView.isVisible = error is ManualSyncError.ErrorSync || error is ManualSyncError.PermissionNotGranted
                errorTextView.text = resources.getString(errorMessageId)

                findViewById<TextView>(R.id.title).setText(title)
                findViewById<TextView>(R.id.not_found_text).isVisible = error is ManualSyncError.NotFound

                confirmButton.text = resources.getString(buttonMessageId)
                confirmButton.clicks().bindTo(action)
                findViewById<AppCompatImageView>(R.id.dialogCloseButtonView).clicks()
                    .bindTo(pm.closeBottomSheetErrorAction)
            }
        }

        binding.syncErrorBottomSheetView.visibilityChanges().subscribe {
            binding.homeActionView.isVisible = !it
        }
        pm.manualSyncErrorBottomSheetCommand.bindTo {
            binding.homeActionView.hide()
            binding.syncErrorBottomSheetView.show()
        }
        pm.closeManualSyncErrorBottomSheetCommand.bindTo {
            binding.syncErrorBottomSheetView.hide()
            binding.homeActionView.isVisible = true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            if (!binding.homeBottomSheetView.handleBack()) router.exit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presentationModel.btControl.resolveResults(requestCode, resultCode)
        super.onActivityResult(requestCode, resultCode, data)
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
}