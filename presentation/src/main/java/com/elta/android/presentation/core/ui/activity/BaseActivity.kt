package com.elta.android.presentation.core.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.AppNavigator
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.elta.android.presentation.core.ui.state_view.StateView
import com.elta.android.presentation.utils.makeSnackBar
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.base.PmActivity
import me.dmdev.rxpm.bindTo
import javax.inject.Inject

abstract class BaseActivity<T : BasePm> :
    PmActivity<T>(),
    HasSupportFragmentInjector,
    RouterProvider {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var factory: PmFactory

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    override lateinit var router: FlowRouter

    protected abstract val binding: ViewBinding

    protected abstract val screenLayout: Int

    protected abstract val classToken: Class<T>

    protected open val navigator: Navigator = AppNavigator(this)

    protected val currentFragment: BaseFragment<*, *>?
        get() = supportFragmentManager.findFragmentById(R.id.containerView) as? BaseFragment<*, *>

    private var errorStateView: StateView? = null
    private var emptyStateView: StateView? = null
    private var progressView: View? = null
    private var homeButtonView: View? = null
    protected val compositeUnbind = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(screenLayout)

        errorStateView = findViewById<View>(R.id.errorStateView) as? StateView
        emptyStateView = findViewById<View>(R.id.emptyStateView) as? StateView
        progressView = findViewById(R.id.progressView)
        homeButtonView = findViewById(R.id.homeButtonView)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        presentationModel.sendDateChangedEvent()
    }

    override fun onBindPresentationModel(pm: T) {
        errorStateView?.let { stateView -> pm.errorControl.bind(stateView, compositeUnbind) }
        emptyStateView?.let { stateView -> pm.emptyControl.bind(stateView, compositeUnbind) }
        progressView?.let { view -> pm.progressState.bindTo(view.visibility()) }
        homeButtonView?.clicks()?.subscribe { router.exit() }
        pm.showSnackBarCommand.bindTo { showSnackBar(it) }
    }

    override fun providePresentationModel(): T {
        val pm = factory.createViewModel(classToken)
        pm.router = router
        return pm
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var fragment = supportFragmentManager.findFragmentById(R.id.containerView)
        do {
            fragment?.onActivityResult(requestCode, resultCode, data)
            fragment = fragment?.childFragmentManager?.findFragmentById(R.id.containerView)
        } while (fragment != null)
    }

    private fun showSnackBar(data: SnackBarData) {
        findViewById<View>(android.R.id.content)?.let { content ->
            makeSnackBar(content, data).show()
        }
    }
}
