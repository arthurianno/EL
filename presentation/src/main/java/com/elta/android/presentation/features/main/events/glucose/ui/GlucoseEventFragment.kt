package com.elta.android.presentation.features.main.events.glucose.ui

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentGlucoseEventBinding
import com.elta.android.presentation.features.main.events.base.initializer.DEFAULT_NOTE_LENGTH
import com.elta.android.presentation.features.main.events.glucose.pm.GlucoseEventPm
import com.elta.android.presentation.utils.OnApplyBottomWindowInsetsListener
import com.elta.android.presentation.utils.WindowBottomInsetsForViewListenerFactory.instance
import com.elta.android.presentation.utils.appbar.collapseProgress
import com.elta.android.presentation.utils.applyWindowBottomInsetsListener
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.findAndClearFocus
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.removeWindowBottomInsetsListener
import com.elta.android.presentation.utils.scrollToBottom
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import com.jakewharton.rxbinding2.widget.textChanges
import com.nullgr.core.ui.extensions.applyLengthFilter
import com.nullgr.core.ui.extensions.hideKeyboard
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.bindTo
import kotlin.math.abs

private const val EXTRA_ID = "extra_id"

class GlucoseEventFragment :
    BaseFragment<GlucoseEventPm, FragmentGlucoseEventBinding>(FragmentGlucoseEventBinding::inflate) {
    companion object {
        fun newInstance(id: String): GlucoseEventFragment {
            return GlucoseEventFragment().apply {
                arguments = bundle(EXTRA_ID to id)
            }
        }
    }

    override val screenLayout: Int = R.layout.fragment_glucose_event
    override val classToken: Class<GlucoseEventPm> = GlucoseEventPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentLightStatusBarConfigProvider
    override val backgroundColor: Int? = null

    private lateinit var insetsListener: OnApplyBottomWindowInsetsListener
    private var maxTranslation: Int = 0
    private var isTouchingScroll = false
    private var isTouchingAppBar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(EXTRA_ID)?.let { presentationModel.setEventData(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            insetsListener = instance(formSaveButtonView, formContainerView) { offset ->
                if (!isTouchingScroll || !isTouchingAppBar) {
                    val isOffsetZero = offset == 0
                    appBarLayoutView.setExpanded(isOffsetZero, true)
                }
            }
            maxTranslation = view.resources?.getDimensionPixelSize(R.dimen.toolbar_translation) ?: 0
            formNoteView.applyLengthFilter(DEFAULT_NOTE_LENGTH)
        }
    }

    override fun onStart() {
        super.onStart()
        view?.applyWindowBottomInsetsListener(insetsListener)
    }

    override fun onStop() {
        super.onStop()
        view?.removeWindowBottomInsetsListener(insetsListener)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            view?.hideKeyboardFun()?.passTo(presentationModel.backHandleAction)
        }
    }

    override fun onBindPresentationModel(pm: GlucoseEventPm) {
        super.onBindPresentationModel(pm)
        observeAppBarChanges()
        bindProgressDialog(pm)
        binding.formSaveButtonView.clicks().bindTo(pm.mainAction)
        binding.menuButtonView.clicks().bindTo(pm.shareAction)

        pm.glucoseValueState.bindTo {
            binding.glucoseEventValueTextView.text = it
            binding.toolbarSubTitleView.text = it
        }
        pm.glucoseFormatState.bindTo {
            getString(
                when (it) {
                    GlucoseFormat.CAPILLARY -> R.string.main_records_glucose_capillary
                    GlucoseFormat.PLASMA -> R.string.main_records_glucose_plasma
                }
            )
        }
        pm.glucoseInfoState.bindTo(binding.eventInfoTextView.text())
        pm.glucoseLevelBackgroundState.bindTo { binding.appBarLayoutView.setBackgroundResource(it) }
        pm.mainActionTitleState.bindTo(binding.formSaveButtonView.text())
        pm.mainActionVisibilityState.observable
            .subscribe(binding.formSaveButtonView.visibility())

        pm.tagSelector.bind(binding.formTagSelectorView, compositeUnbind)
        pm.dateSelector.bind(binding.formDateSelectorView, compositeUnbind)
        pm.timeSelector.bind(binding.formTimeSelectorView, compositeUnbind)
        pm.noteInput.bindTo(binding.formNoteView)

        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
        with(binding) {
            formNoteView.textChanges().subscribe { binding.scrollableView.scrollToBottom() }
            beforeEatingAttribute.clicks().bindTo(pm.beforeMealAction)
            afterEatingAttribute.clicks().bindTo(pm.afterMealAction)
            pm.mealSelector.bindTo(::toggleMealTagButtons)
        }
    }

    private fun toggleMealTagButtons(it: MealTag?) {
        when (it) {
            MealTag.BEFOREMEAL -> {
                binding.afterEatingAttribute.isSelected = false
                binding.beforeEatingAttribute.isSelected = true
            }

            MealTag.AFTERMEAL -> {
                binding.afterEatingAttribute.isSelected = true
                binding.beforeEatingAttribute.isSelected = false
            }

            else -> {
                binding.afterEatingAttribute.isSelected = false
                binding.beforeEatingAttribute.isSelected = false
            }
        }
    }

    private fun observeAppBarChanges() {
        binding.apply {
            scrollableView.setOnTouchListener { _, me ->
                isTouchingScroll = me.action == MotionEvent.ACTION_MOVE
                isTouchingAppBar = isTouchingScroll
                false
            }

            glucoseEventFormContainerView.setOnTouchListener { _, me ->
                isTouchingAppBar = me.action == MotionEvent.ACTION_MOVE
                isTouchingScroll = isTouchingAppBar
                false
            }

            appBarLayoutView.collapseProgress().subscribe {
                val alpha = 1 - abs(it / 100f)
                glucoseEventValueTextView.alpha = alpha
                glucoseEventUnitsTextView.alpha = alpha
                eventInfoContainerView.alpha = alpha

                val translation = maxTranslation * it / 100f
                toolbarTitleView.translationY = translation
                toolbarSubTitleView.alpha = 1 - alpha
                toolbarSubTitleView.translationY = translation
                if (isTouchingScroll || isTouchingAppBar) {
                    view?.hideKeyboard()
                    requireActivity().findAndClearFocus()
                }
            }
        }
    }
}
