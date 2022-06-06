package com.elta.android.presentation.features.main.events.base.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.events.base.initializer.FormInitializer
import com.elta.android.presentation.features.main.events.base.initializer.makeFormInitializer
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.utils.OnApplyBottomWindowInsetsListener
import com.elta.android.presentation.utils.WindowBottomInsetsForViewListenerFactory.instance
import com.elta.android.presentation.utils.appbar.collapseProgress
import com.elta.android.presentation.utils.applyWindowBottomInsetsListener
import com.elta.android.presentation.utils.findAndClearFocus
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.removeWindowBottomInsetsListener
import com.elta.android.presentation.utils.scrollToBottom
import com.elta.android.presentation.utils.showDatePickerDialog
import com.elta.android.presentation.utils.showTimePickerDialog
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import com.jakewharton.rxbinding2.widget.textChanges
import com.nullgr.core.ui.extensions.hideKeyboard
import kotlinx.android.synthetic.main.fragment_event_form.*
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.passTo
import me.dmdev.rxpm.widget.bindTo
import org.threeten.bp.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlin.math.abs

abstract class BaseEventFragment<T : BaseEventPm> : BaseFragment<T>() {

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        insetsListener = instance(formSaveButtonView, formContainerView) { offset ->
            if (!isTouchingScroll || !isTouchingAppBar) {
                val isOffsetZero = offset == 0
                appBarLayoutView?.setExpanded(isOffsetZero, true)
                if (isOffsetZero) requireActivity().findAndClearFocus()
                if (!isOffsetZero) scrollableView.scrollToBottom()
            }
        }
        maxTranslation = view.resources?.getDimensionPixelSize(R.dimen.toolbar_translation) ?: 0
        with(viewsState) {
            formPickerView.alpha = formPickerViewAlpha
            eventInfoContainerView.alpha = eventInfoAlpha
            toolbarTitleView.translationY = titleTranslation
            toolbarSubTitleView.translationY = subTitleTranslation
            toolbarSubTitleView.alpha = subTitleAlpha
            formSaveButtonView.visibility = buttonVisibility
        }
        initializer.init(view)
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        observeAppBarChanges()
        formPickerView.valueChanges().bindTo(pm.formPickerValueChangedAction)
        formSaveButtonView.clicks().bindTo(pm.mainAction)
        pm.updateFormPickerValueCommand.bindTo { formPickerView.setValues(it.first, it.second) }
        pm.mainActionTitleState.bindTo(formSaveButtonView.text())
        pm.mainActionVisibilityState.observable
            .throttleLast(DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe(formSaveButtonView.visibility())
        pm.formInput.bindTo(formInputView)
        pm.formSelector.bind(formVariantSelectorView, compositeUnbind)
        pm.tagSelector.bind(formTagSelectorView, compositeUnbind)
        pm.dateSelector.bind(formDateSelectorView, compositeUnbind)
        pm.timeSelector.bind(formTimeSelectorView, compositeUnbind)
        pm.noteInput.bindTo(formNoteView)
        pm.bindDateSelection()
        pm.exitDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
        formNoteView.textChanges().subscribe { scrollableView.scrollToBottom() }
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
            formPickerViewAlpha = formPickerView.alpha
            eventInfoAlpha = eventInfoContainerView.alpha
            titleTranslation = toolbarTitleView.translationY
            subTitleTranslation = toolbarSubTitleView.translationY
            subTitleAlpha = toolbarSubTitleView.alpha
            buttonVisibility = formSaveButtonView.visibility
        }
    }

    override fun handleBack() {
        view?.hideKeyboardFun()?.passTo(presentationModel.backHandleAction)
    }

    abstract fun getEventType(): EventType

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

    @Suppress("MagicNumber")
    private fun observeAppBarChanges() {
        formPickerView.valueChangesFormatted().subscribe(toolbarSubTitleView.text())

        scrollableView.setOnTouchListener { _, me ->
            isTouchingScroll = me.action == MotionEvent.ACTION_MOVE
            isTouchingAppBar = isTouchingScroll
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
                view?.hideKeyboard()
                requireActivity().findAndClearFocus()
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

    private companion object {
        const val DEBOUNCE = 100L
    }
}
