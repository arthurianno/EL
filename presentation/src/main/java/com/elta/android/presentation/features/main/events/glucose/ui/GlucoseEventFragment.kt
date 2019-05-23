package com.elta.android.presentation.features.main.events.glucose.ui

import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.events.base.initializer.DEFAULT_NOTE_LENGTH
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.features.main.events.glucose.pm.GlucoseEventPm
import com.elta.android.presentation.utils.appbar.collapseProgress
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.hideKeyboardFun
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.ui.extensions.applyLengthFilter
import kotlinx.android.synthetic.main.fragment_glucose_event.*
import java.util.concurrent.TimeUnit

class GlucoseEventFragment : BaseFragment<GlucoseEventPm>() {

    override val screenLayout: Int = R.layout.fragment_glucose_event
    override val classToken: Class<GlucoseEventPm> = GlucoseEventPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentLightStatusBarConfigProvider
    override val backgroundColor: Int? = null

    private var maxTranslation: Int = 0
    private val viewsState = ViewsState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { presentationModel.setEventData(checkNotNull(it[EXTRA_ID]) as String) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        maxTranslation = view.resources?.getDimensionPixelSize(R.dimen.toolbar_translation) ?: 0
        with(viewsState) {
            glucoseEventValueTextView.alpha = valueViewAlpha
            glucoseEventUnitsTextView.alpha = valueViewAlpha
            eventInfoContainerView.alpha = eventInfoAlpha
            toolbarTitleView.translationY = titleTranslation
            toolbarSubTitleView.translationY = subTitleTranslation
            toolbarSubTitleView.alpha = subTitleAlpha
            formSaveButtonView.visibility = buttonVisibility
        }
        formNoteView.applyLengthFilter(DEFAULT_NOTE_LENGTH)
    }

    override fun onPause() {
        super.onPause()
        with(viewsState) {
            valueViewAlpha = glucoseEventValueTextView.alpha
            eventInfoAlpha = eventInfoContainerView.alpha
            titleTranslation = toolbarTitleView.translationY
            subTitleTranslation = toolbarSubTitleView.translationY
            subTitleAlpha = toolbarSubTitleView.alpha
            buttonVisibility = formSaveButtonView.visibility
        }
    }

    override fun onBindPresentationModel(pm: GlucoseEventPm) {
        super.onBindPresentationModel(pm)
        observeAppBarChanges()
        bindProgressDialog(pm)
        formSaveButtonView.clicks().bindTo(pm.mainAction)
        menuButtonView.clicks().bindTo(pm.shareAction)

        pm.glucoseValueState.bindTo {
            glucoseEventValueTextView.text = it
            toolbarSubTitleView.text = it
        }
        pm.glucoseInfoState.bindTo(eventInfoTextView.text())
        pm.glucoseLevelBackgroundState.bindTo { appBarLayoutView.setBackgroundResource(it) }
        pm.mainActionTitleState.bindTo(formSaveButtonView.text())
        pm.mainActionVisibilityState.observable
            .throttleLast(DEBOUNCE, TimeUnit.MILLISECONDS)
            .bindTo(formSaveButtonView.visibility())

        pm.tagSelector.bind(formTagSelectorView, compositeUnbind)
        pm.dateSelector.bind(formDateSelectorView, compositeUnbind)
        pm.timeSelector.bind(formTimeSelectorView, compositeUnbind)
        pm.noteInput.bindTo(formNoteView)

        pm.exitDialogControl.bindTo { data, dc ->
            MaterialDialog.Builder(checkNotNull(activity))
                .cancelable(false)
                .title(data.title)
                .content(data.message)
                .negativeText(data.negative)
                .positiveText(data.positive)
                .onPositive { _, _ -> dc.sendResult(BaseEventPm.DialogResult.POSITIVE) }
                .onNegative { _, _ -> dc.sendResult(BaseEventPm.DialogResult.NEGATIVE) }
                .build()
        }
        pm.hideKeyBoardCommand.bindTo { view?.hideKeyboardFun() }
    }

    override fun handleBack() {
        view?.hideKeyboardFun()
        passTo(presentationModel.backHandleAction)
    }

    private fun observeAppBarChanges() {
        appBarLayoutView.collapseProgress().bindTo {
            val alpha = 1 - Math.abs(it / 100f)
            glucoseEventValueTextView.alpha = alpha
            glucoseEventUnitsTextView.alpha = alpha
            eventInfoContainerView.alpha = alpha

            val translation = maxTranslation * it / 100f
            toolbarTitleView.translationY = translation
            toolbarSubTitleView.alpha = 1 - alpha
            toolbarSubTitleView.translationY = translation
        }
    }

    private data class ViewsState(
        var valueViewAlpha: Float = 1f,
        var eventInfoAlpha: Float = 1f,
        var titleTranslation: Float = 0f,
        var subTitleTranslation: Float = 0f,
        var subTitleAlpha: Float = 0f,
        var buttonVisibility: Int = View.INVISIBLE
    )

    companion object {
        private const val EXTRA_ID = "extra_id"

        fun newInstance(id: String): GlucoseEventFragment {
            return GlucoseEventFragment().apply {
                arguments = bundle(EXTRA_ID to id)
            }
        }
    }
}
