package com.nullgr.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.nullgr.android.presentation.R
import com.nullgr.android.presentation.core.navigation.BackHandler
import com.nullgr.android.presentation.core.navigation.RouterProvider
import com.nullgr.android.presentation.core.pm.BasePm
import com.nullgr.android.presentation.core.pm.factory.PmFactory
import com.nullgr.android.presentation.core.pm.widgets.bind
import com.nullgr.android.presentation.core.ui.state_view.StateView
import com.nullgr.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.nullgr.core.ui.extensions.setStatusBarColor
import dagger.android.support.AndroidSupportInjection
import me.dmdev.rxpm.base.PmSupportFragment
import javax.inject.Inject

abstract class BaseFragment<T : BasePm> : PmSupportFragment<T>(), BackHandler {

    @Inject
    lateinit var factory: PmFactory

    protected abstract val screenLayout: Int

    protected abstract val classToken: Class<T>

    protected abstract val statusBarConfigProvider: StatusBarConfigProvider

    open val router by lazy(LazyThreadSafetyMode.NONE) {
        ((parentFragment ?: activity) as RouterProvider).router
    }

    private var errorStateView: StateView? = null
    private var emptyStateView: StateView? = null
    private var progressView: View? = null
    private var homeButton: View? = null

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(screenLayout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        errorStateView = view.findViewById<View>(R.id.errorStateView) as? StateView
        emptyStateView = view.findViewById<View>(R.id.emptyStateView) as? StateView
        progressView = view.findViewById(R.id.progressView)
        homeButton = view.findViewById(R.id.homeButton)
    }

    override fun onStart() {
        super.onStart()
        with(statusBarConfigProvider) {
            activity?.window?.setStatusBarColor(statusBarColor, lightStatusBar)
        }
    }

    @CallSuper
    override fun onBindPresentationModel(pm: T) {
        errorStateView?.let { stateView -> pm.errorControl.bind(stateView, compositeUnbind) }
        emptyStateView?.let { stateView -> pm.emptyControl.bind(stateView, compositeUnbind) }
        progressView?.let { view -> pm.progressState.bindTo(view.visibility()) }
        homeButton?.clicks()?.bindTo(pm.backAction)
    }

    override fun providePresentationModel(): T {
        val pm = factory.createViewModel(classToken)
        pm.router = router
        return pm
    }

    override fun handleBack(): Boolean {
        passTo(presentationModel.backAction)
        return true
    }
}