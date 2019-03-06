package com.elta.android.presentation.features.main.events.base.ui

import android.os.Bundle
import android.view.View
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentLightStatusBarConfigProvider
import com.elta.android.presentation.features.main.events.base.initializer.formInitializer
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import com.elta.android.presentation.utils.appbar.AppBarState
import com.elta.android.presentation.utils.appbar.observeState
import com.nullgr.core.ui.toast.showToast
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_event_form.*

abstract class BaseEventFragment<T : BaseEventPm> : BaseFragment<T>() {

    override val screenLayout: Int = R.layout.fragment_event_form
    override val statusBarConfigProvider: StatusBarConfigProvider = TransparentLightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarView.setNavigationOnClickListener { activity?.onBackPressed() }
        formInitializer(getEventType()).init(view)
    }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        formPickerView.valueChanges().skip(1).bindTo { it.toString().showToast(activity) }

        Observables.combineLatest(
            formPickerView.valueChangesFormatted(),
            appBarLayoutView.observeState())
            .filter { it.first.isNotEmpty() }
            .bindTo {
                when (it.second) {
                    AppBarState.COLLAPSED -> toolbarView.subtitle = it.first
                    else -> toolbarView.subtitle = null
                }
            }
    }

    abstract fun getEventType(): EventType
}
