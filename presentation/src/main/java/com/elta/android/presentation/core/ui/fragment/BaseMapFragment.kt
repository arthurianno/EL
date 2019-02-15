package com.elta.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.BackHandler
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.pm.BaseMapPm
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.elta.android.presentation.core.ui.state_view.StateView
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.utils.makeSnackBar
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.jakewharton.rxbinding2.view.visibility
import com.nullgr.core.ui.extensions.setStatusBarColor
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.map.MapPmExtension
import me.dmdev.rxpm.map.MapPmView
import me.dmdev.rxpm.map.delegate.MapPmSupportFragmentDelegate
import javax.inject.Inject

@Suppress("TooManyFunctions", "ForbiddenComment")
abstract class BaseMapFragment<T> : Fragment(), MapPmView<T>, BackHandler
    where T : BaseMapPm, T : MapPmExtension {

    @Inject
    lateinit var factory: PmFactory

    private val delegate by lazy(LazyThreadSafetyMode.NONE) { MapPmSupportFragmentDelegate(this) }

    final override val compositeUnbind = CompositeDisposable()

    final override val presentationModel get() = delegate.presentationModel

    final override var mapView: MapView? = null
    final override var googleMap: GoogleMap? = null

    protected abstract val screenLayout: Int

    protected abstract val classToken: Class<T>

    protected abstract val statusBarConfigProvider: StatusBarConfigProvider

    private val router by lazy(LazyThreadSafetyMode.NONE) {
        ((parentFragment ?: activity) as RouterProvider).router as FlowRouter
    }

    private var errorStateView: StateView? = null
    private var emptyStateView: StateView? = null
    private var progressView: View? = null

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        MapsInitializer.initialize(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(screenLayout, container, false).apply {
            delegate.onCreateView(this, savedInstanceState)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        errorStateView = view.findViewById<View>(R.id.errorStateView) as? StateView
        emptyStateView = view.findViewById<View>(R.id.emptyStateView) as? StateView
        progressView = view.findViewById(R.id.progressView)
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
        with(statusBarConfigProvider) {
            activity?.window?.setStatusBarColor(statusBarColor, lightStatusBar)
        }
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        delegate.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        delegate.onLowMemory()
    }

    @CallSuper
    override fun onBindPresentationModel(pm: T) {
        errorStateView?.let { stateView -> pm.errorControl.bind(stateView, compositeUnbind) }
        emptyStateView?.let { stateView -> pm.emptyControl.bind(stateView, compositeUnbind) }
        progressView?.let { view -> pm.progressState.bindTo(view.visibility()) }
        pm.showSnackBarCommand.bindTo { showSnackBar(it) }
    }

    override fun providePresentationModel(): T {
        val pm = factory.createViewModel(classToken)
        pm.router = router
        return pm
    }

    override fun handleBack() {
        router.exit()
    }

    private fun showSnackBar(data: SnackBarData) {
        view?.let { content ->
            makeSnackBar(content, data).show()
        }
    }
}