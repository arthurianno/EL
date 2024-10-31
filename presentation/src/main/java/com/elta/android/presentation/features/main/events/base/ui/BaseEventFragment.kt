package com.elta.android.presentation.features.main.events.base.ui

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentEventFormBinding
import com.elta.android.presentation.features.main.events.base.initializer.FormInitializer
import com.elta.android.presentation.features.main.events.base.initializer.ManualGlucoseFormInitializer
import com.elta.android.presentation.features.main.events.base.initializer.makeFormInitializer
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.utils.OnApplyBottomWindowInsetsListener
import com.elta.android.presentation.utils.WindowBottomInsetsForViewListenerFactory.instance
import com.elta.android.presentation.utils.appbar.collapseProgress
import com.elta.android.presentation.utils.applyWindowBottomInsetsListener
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.removeWindowBottomInsetsListener
import com.elta.android.presentation.utils.scrollToBottom
import com.elta.android.presentation.utils.showDatePickerDialog
import com.elta.android.presentation.utils.showTimePickerDialog
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import com.jakewharton.rxbinding2.widget.textChanges
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.show
import com.nullgr.core.ui.extensions.toggleVisibilityState
import io.reactivex.android.schedulers.AndroidSchedulers
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.bindTo
import org.threeten.bp.ZonedDateTime
import kotlin.math.abs

abstract class BaseEventFragment<T : BaseEventPm> :
    BaseFragment<T, FragmentEventFormBinding>(FragmentEventFormBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_event_form
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentLightStatusBarConfigProvider
    override val backgroundColor: Int? = null

    private lateinit var insetsListener: OnApplyBottomWindowInsetsListener
    private lateinit var initializer: FormInitializer
    private var maxTranslation: Int = 0
    private val viewsState = ViewsState()
    private var isTouchingScroll = false
    private var isTouchingAppBar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventType = getEventType()
        initializer = eventType.makeFormInitializer()
        presentationModel.setEventType(eventType)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setWeightPicker()
        setGlucosePickerInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        insetsListener = instance(binding.formSaveButtonView, binding.formContainerView) { offset ->
            if (!isTouchingScroll || !isTouchingAppBar) {
                val isOffsetZero = offset == 0
                binding.appBarLayoutView.setExpanded(isOffsetZero, true)
            }
        }
        maxTranslation = view.resources?.getDimensionPixelSize(R.dimen.toolbar_translation) ?: 0
        with(viewsState) {
            binding.run {
                formPickerView.alpha = formPickerViewAlpha
                eventInfoContainerView.alpha = eventInfoAlpha
                toolbarTitleView.translationY = titleTranslation
                toolbarSubTitleView.translationY = subTitleTranslation
                toolbarSubTitleView.alpha = subTitleAlpha
                formSaveButtonView.visibility = buttonVisibility
            }
        }
        initializer.init(view)
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        pm.progressState.bindTo {
            binding.progressView.toggleVisibilityState(it, defaultFalseState = View.INVISIBLE)
            binding.scrollableView.toggleVisibilityState(!it, defaultFalseState = View.INVISIBLE)
        }
        binding.mealTagButtons.beforeEatingAttribute.clicks().bindTo(pm.beforeMealAction)
        binding.mealTagButtons.afterEatingAttribute.clicks().bindTo(pm.afterMealAction)
        pm.mealSelector.bindTo(::toggleMealTagButtons)
        observeAppBarChanges(pm)
        observeFoodChanges(pm)
        observeMedicamentChanges(pm)
        observeGlucoseChanges(pm)
        observeFormKeyEvent(pm)

        binding.formPickerView.valueChanges().bindTo(pm.formPickerValueChangedAction)
        binding.formSaveButtonView.clicks().bindTo(pm.mainAction)
        binding.homeButtonView.clicks().bindTo(pm.backHandleAction)
        pm.updateFormPickerValueCommand.bindTo {
            binding.formPickerView.setValues(
                it.first,
                it.second
            )
        }
        pm.mainActionTitleState.bindTo(binding.formSaveButtonView.text())
        pm.mainActionVisibilityState.bindTo(binding.formSaveButtonView.visibility())
        pm.formInput.bindTo(binding.formInputView)
        pm.additionalInput.bindTo(binding.additionalInput)
        pm.formSelector.bind(binding.formVariantSelectorView, compositeUnbind)
        pm.tagSelector.bind(binding.formTagSelectorView, compositeUnbind)
        pm.dateSelector.bind(binding.formDateSelectorView, compositeUnbind)
        pm.timeSelector.bind(binding.formTimeSelectorView, compositeUnbind)
        pm.noteInput.bindTo(binding.formNoteView)
        pm.bindDateSelection()
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.editingXEIsNotAvailableControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.breadUnitsChangeDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.userHadChangesBreadUnitsDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
        binding.formNoteView.textChanges().subscribe(
            { binding.scrollableView.scrollToBottom() },
            {}
        )
    }

    override fun onStart() {
        super.onStart()
        view?.applyWindowBottomInsetsListener(insetsListener)
    }

    override fun onStop() {
        super.onStop()
        view?.removeWindowBottomInsetsListener(insetsListener)
    }

    override fun onPause() {
        super.onPause()
        with(viewsState) {
            formPickerViewAlpha = binding.formPickerView.alpha
            eventInfoAlpha = binding.eventInfoContainerView.alpha
            titleTranslation = binding.toolbarTitleView.translationY
            subTitleTranslation = binding.toolbarSubTitleView.translationY
            subTitleAlpha = binding.toolbarSubTitleView.alpha
            buttonVisibility = binding.formSaveButtonView.visibility
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            view?.hideKeyboardFun()?.passTo(presentationModel.backHandleAction)
        }
    }

    override fun onDetach() {
        super.onDetach()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    abstract fun getEventType(): EventType

    private fun setWeightPicker() {
        if (presentationModel.eventTypeState.valueOrNull is EventType.Weight) {
            presentationModel.profileState.bindTo { initializer.setPickerValue(it.weight) }
        }
    }

    private fun setGlucosePickerInfo() {
        if (presentationModel.eventTypeState.valueOrNull is EventType.Glucose) {
            presentationModel.profileState.bindTo {
                val (textId, pickerConfig) = when (it.glucoseFormat) {
                    GlucoseFormat.CAPILLARY -> {
                        R.string.events_helper_text_glucose_capillary to
                                ManualGlucoseFormInitializer.pickerConfiguration
                    }

                    GlucoseFormat.PLASMA -> {
                        R.string.events_helper_text_glucose_plasma to
                                ManualGlucoseFormInitializer.pickerPlasmaConfiguration
                    }
                }
                binding.eventInfoTextView.setText(textId)
                initializer.setPickerConfiguration(pickerConfig)
            }
        }
    }

    private fun T.bindDateSelection() {
        showDatePickerDialog.bindTo { originalDate ->
            activity.showDatePickerDialog(originalDate, maxDate = ZonedDateTime.now()) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
        showTimePickerDialog.bindTo { originalDate ->
            activity.showTimePickerDialog(originalDate) {
                dateTimeSelectedAction.consumer.accept(it)
            }
        }
    }

    private fun observeGlucoseChanges(pm: T) {
        if (pm.eventTypeState.valueOrNull is EventType.Glucose) {
            pm.appBarColorState.bindTo { drawableRes ->
                binding.appBarLayoutView.setBackgroundResource(drawableRes)
            }
            pm.eventTypeState.observable
                .subscribe {
                    binding.mealTagButtons.root.visibility = View.VISIBLE
                }

            binding.formPickerView.valueChanges().bindTo(pm.changeAppBarColorAction)
        }
    }

    private fun observeMedicamentChanges(pm: T) {

        if (pm.eventTypeState.valueOrNull is EventType.Medicaments) {

            pm.medicamentState.observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { medicamentModel ->
                    val medicament = medicamentModel.medicament
                    with(binding) {
                        if (medicament != null) {
                            formInputView.show()
                            formVariantSelectorView.show()

                            formVariantSelectorView.hint = medicament.name
                            formVariantSelectorView.iconText = medicament.name


                            if (medicament.isOther) {
                                medicamentModel.otherName?.let { additionalInput.setText(it) }
                                additionalInput.show()
                            } else {
                                binding.additionalInput.setText("")
                                binding.additionalInput.hide()
                            }
                        } else {
                            binding.formInputView.hide()
                            binding.formVariantSelectorView.hide()
                            binding.additionalInput.show()
                        }
                    }
                }
        }
    }

    private fun observeFoodChanges(pm: T) {
        if (pm.eventTypeState.valueOrNull is EventType.Bread) {
            pm.dishes.observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    val formPickerEnabled = it.isNullOrEmpty()

                    binding.formPickerView.enableFormPicker(formPickerEnabled)

                    binding.formPickerView.setDisableTouchCallback {
                        pm.editingXEIsNotAvailableAction.consumer.accept(Unit)
                    }

                    with(binding.formVariantSelectorView) {
                        if (it.isEmpty()) {
                            hint = context.getString(R.string.events_creation_hint_bread)
                            icon = null
                            iconText = null
                        } else {
                            hint = context.getString(R.string.events_creation_text_bread)
                            icon = context.getDrawable(R.drawable.ic_record_label)
                            iconText = it.count().toString()
                        }
                    }
                }
        }
    }

    @Suppress("MagicNumber")
    private fun observeAppBarChanges(pm: T) {
        binding.apply {
            formPickerView.valueChangesFormatted().subscribe(binding.toolbarSubTitleView.text())

            scrollableView.setOnTouchListener { _, me ->
                isTouchingScroll = me.action == MotionEvent.ACTION_MOVE
                isTouchingAppBar = isTouchingScroll
                pm.hideKeyboardAction.consumer.accept(Unit)
                false
            }

            eventFormContainerView.setOnTouchListener { _, me ->
                isTouchingAppBar = me.action == MotionEvent.ACTION_MOVE
                isTouchingScroll = isTouchingAppBar
                false
            }

            appBarLayoutView.collapseProgress().subscribe {
                val alpha = 1 - abs(it / 100f)
                formPickerView.alpha = alpha
                eventInfoContainerView.alpha = alpha

                val translation = maxTranslation * it / 100f
                toolbarTitleView.translationY = translation
                toolbarSubTitleView.alpha = 1 - alpha
                toolbarSubTitleView.translationY = translation
                if (isTouchingScroll || isTouchingAppBar) {
                    pm.hideKeyboardAction.consumer.accept(Unit)
                }
            }
        }
    }

    private fun observeFormKeyEvent(pm: T) {
        binding.formInputView.setOnKeyListener { _, _, keyEvent ->
            onKeyEventAction(keyEvent, pm)
        }
        binding.formNoteView.setOnKeyListener { _, _, keyEvent ->
            onKeyEventAction(keyEvent, pm)
        }
    }

    private fun onKeyEventAction(keyEvent: KeyEvent, pm: T): Boolean {
        if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
            pm.hideKeyboardAction.consumer.accept(Unit)
        }
        return false
    }

    private fun toggleMealTagButtons(it: MealTag?) {
        when (it) {
            MealTag.BEFOREMEAL -> {
                binding.mealTagButtons.afterEatingAttribute.isSelected = false
                binding.mealTagButtons.beforeEatingAttribute.isSelected = true
            }

            MealTag.AFTERMEAL -> {
                binding.mealTagButtons.afterEatingAttribute.isSelected = true
                binding.mealTagButtons.beforeEatingAttribute.isSelected = false
            }

            else -> {
                binding.mealTagButtons.afterEatingAttribute.isSelected = false
                binding.mealTagButtons.beforeEatingAttribute.isSelected = false
            }
        }
    }

    private data class ViewsState(
        var formPickerViewAlpha: Float = 1f,
        var eventInfoAlpha: Float = 1f,
        var titleTranslation: Float = 0f,
        var subTitleTranslation: Float = 0f,
        var subTitleAlpha: Float = 0f,
        var buttonVisibility: Int = View.INVISIBLE
    )
}
