package com.elta.android.presentation.features.home.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
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
import io.reactivex.rxkotlin.addTo
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

    // Используем lazy для безопасной инициализации RxPermissions
    private val rxPermissions: RxPermissions by lazy {
        RxPermissions(requireActivity())
    }

    private var selectedBottomNavigationItemId = R.id.mainMenuItemView
    private var isMainScreenEmpty = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigationInsets()
        savedInstanceState
            ?.takeIf { it.containsKey(KEY_SELECTED_MENU_ID) }
            ?.getInt(KEY_SELECTED_MENU_ID)
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
            .addTo(compositeDestroy)
        pm.selectedItemIdState.bindTo(binding.homeBottomNavigationView.selection())
        pm.selectedItemIdState.bindTo { itemId ->
            selectedBottomNavigationItemId = itemId
            updateBottomNavigationBackground()
        }
        bus.events<Events.HomeModelChanged>()
            .subscribe { modelChanged ->
                isMainScreenEmpty = !modelChanged.model.hasEvents
                updateBottomNavigationBackground()
            }
            .addTo(compositeDestroy)
        pm.bottomSheetItems.bindTo { adapter.submitList(it) }
        pm.closeBottomSheetCommand.bindTo { binding.homeBottomSheetView.hide() }
        pm.showBottomSheetCommand.bindTo { binding.homeBottomSheetView.show() }
        binding.homeBottomSheetView.visibilityChanges().subscribe { visible ->
            binding.homeActionView.isVisible = !visible
            bus.event(Events.HomeBottomSheetStateChanged(visible))
        }
            .addTo(compositeDestroy)
        binding.homeBottomNavigationView.tabClicks()
            .bindTo(pm.menuItemSelectedAction)
        // Откладываем инициализацию RxPermissions чтобы избежать конфликта с FragmentManager
        view?.post {
            if (isAdded && !isStateSaved) {
                pm.btControl.bindTo(compositeDestroy, rxPermissions, this)
            }
        }
        pm.likeAppDialogControl.bindLikeAppDialog()
        pm.googlePlayDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.feedbackDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.glucoseDataReminderDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.openSettingsCommand.bindTo { openSettingsIntent(requireContext()) }
        bindHelpBottomSheet(pm)
    }

    private fun updateBottomNavigationBackground() {
        val backgroundRes = when (selectedBottomNavigationItemId) {
            R.id.mainMenuItemView -> {
                if (isMainScreenEmpty) R.drawable.bg_home_bottom_empty_state else android.R.color.white
            }
            R.id.notesMenuItemView -> R.color.pale_gray
            else -> android.R.color.white
        }
        binding.bottomContainer.setBackgroundResource(backgroundRes)
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
            .addTo(compositeDestroy)
        binding.helpBottomSheetView.findViewById<AppCompatTextView>(R.id.confirmButtonView)
            .clicks()
            .subscribe(pm.firstSyncAction.consumer)
            .addTo(compositeDestroy)
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
            .addTo(compositeDestroy)
        pm.manualSyncErrorBottomSheetCommand.bindTo {
            binding.homeActionView.hide()
            binding.syncErrorBottomSheetView.show()
        }
        pm.closeManualSyncErrorBottomSheetCommand.bindTo {
            binding.syncErrorBottomSheetView.hide()
            binding.homeActionView.isVisible = true
        }

        with(binding.deviceHardwareErrorBottomSheetView) {
            findViewById<TextView>(R.id.supportButtonView).clicks().bindTo(pm.hardwareErrorSupportAction)
            findViewById<AppCompatImageView>(R.id.dialogCloseButtonView).clicks()
                .bindTo(pm.closeHardwareErrorBottomSheetAction)

        }
        binding.deviceHardwareErrorBottomSheetView.visibilityChanges().subscribe {
            binding.homeActionView.isVisible = !it
        }.addTo(compositeDestroy)

        pm.deviceHardwareErrorBottomSheetCommand.bindTo {
            binding.homeActionView.hide()
            binding.deviceHardwareErrorBottomSheetView.show()
        }
        pm.closeHardwareErrorBottomSheetCommand.bindTo {
            binding.deviceHardwareErrorBottomSheetView.hide()
            binding.homeActionView.isVisible = true
        }

        with(binding.deviceInvalidTimeBottomSheetView) {
            findViewById<TextView>(R.id.continueButtonView).clicks().bindTo(pm.continueInvalidTimeAction)
            findViewById<AppCompatImageView>(R.id.dialogCloseButtonView).clicks()
                .bindTo(pm.closeInvalidTimeBottomSheetAction)
        }
        binding.deviceInvalidTimeBottomSheetView.visibilityChanges().subscribe {
            binding.homeActionView.isVisible = !it
        }.addTo(compositeDestroy)

        pm.deviceInvalidTimeBottomSheetCommand.bindTo {
            binding.homeActionView.hide()
            binding.deviceInvalidTimeBottomSheetView.show()
        }
        pm.closeInvalidTimeBottomSheetCommand.bindTo {
            binding.deviceInvalidTimeBottomSheetView.hide()
            binding.homeActionView.isVisible = true
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            val handled = view
                ?.findViewById<com.elta.android.presentation.widgets.bottom_sheet.BottomSheetView>(R.id.homeBottomSheetView)
                ?.handleBack()
                ?: false
            val handledError = if (!handled) {
                view?.findViewById<com.elta.android.presentation.widgets.bottom_sheet.BottomSheetView>(R.id.deviceHardwareErrorBottomSheetView)
                    ?.handleBack() ?: false
            } else true
            if (!handled && !handledError) router.exit()

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

    private fun setupBottomNavigationInsets() {
        val syncStatusView = requireActivity().findViewById<View>(R.id.syncStatusView)
        syncStatusView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                v.setPadding(
                    v.paddingLeft,
                    v.paddingTop,
                    v.paddingRight,
                    navigationBarsInsets.bottom
                )
                insets
            }
        }
        val connectionStatusView = requireActivity().findViewById<View>(R.id.connectionStatusView)
        connectionStatusView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                v.setPadding(
                    v.paddingLeft,
                    v.paddingTop,
                    v.paddingRight,
                    navigationBarsInsets.bottom
                )
                insets
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomContainer) { v, insets ->
            val navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                navigationBarsInsets.bottom
            )

            val initialPlusMargin = (16 * resources.displayMetrics.density).toInt()
            (binding.homeActionView.layoutParams as? FrameLayout.LayoutParams)?.let { actionParams ->
                actionParams.bottomMargin = initialPlusMargin + navigationBarsInsets.bottom
                binding.homeActionView.layoutParams = actionParams
            }

            insets
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
