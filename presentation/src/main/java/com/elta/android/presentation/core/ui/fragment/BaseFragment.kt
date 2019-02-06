package com.elta.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.BackHandler
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.elta.android.presentation.core.ui.state_view.StateView
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.utils.applyInsetsToContentView
import com.elta.android.presentation.utils.makeSnackBar
import com.elta.android.presentation.widgets.dialogs.ProgressDialog
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.nullgr.core.ui.extensions.setStatusBarColor
import dagger.android.support.AndroidSupportInjection
import me.dmdev.rxpm.base.PmSupportFragment
import javax.inject.Inject

abstract class BaseFragment<T : BasePm> : PmSupportFragment<T>(), BackHandler {

    @Inject
    lateinit var factory: PmFactory

    protected abstract val screenLayout: Int

    protected abstract val classToken: Class<T>

    protected abstract val statusBarConfigProvider: StatusBarConfigProvider?

    open val progressDialog: ProgressDialog by lazy { ProgressDialog.newInstance() }
    open val router by lazy(LazyThreadSafetyMode.NONE) {
        ((parentFragment ?: activity) as RouterProvider).router
    }

    private var errorStateView: StateView? = null
    private var emptyStateView: StateView? = null
    private var progressView: View? = null
    private var homeButtonView: View? = null

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
        homeButtonView = view.findViewById(R.id.homeButtonView)
    }

    override fun onStart() {
        super.onStart()
        statusBarConfigProvider?.let {
            view?.applyInsetsToContentView(!it.drawUnderStatusBar)
            activity?.window?.setStatusBarColor(it.statusBarColor, it.lightStatusBar)
        }
    }

    @CallSuper
    override fun onBindPresentationModel(pm: T) {
        errorStateView?.let { stateView -> pm.errorControl.bind(stateView, compositeUnbind) }
        emptyStateView?.let { stateView -> pm.emptyControl.bind(stateView, compositeUnbind) }
        progressView?.let { view -> pm.progressState.bindTo(view.visibility()) }
        homeButtonView?.clicks()?.bindTo { activity?.onBackPressed() }
        pm.showSnackBarCommand.bindTo { showSnackbar(it) }
    }

    override fun providePresentationModel(): T {
        val pm = factory.createViewModel(classToken)
        pm.router = router
        return pm
    }

    override fun handleBack() {
        router.exit()
    }

    private fun showSnackbar(data: SnackBarData) {
        view?.let { content ->
            makeSnackBar(content, data).show()
        }
    }
}