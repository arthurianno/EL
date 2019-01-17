package com.elta.android.presentation.core.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.BackHandler
import com.elta.android.presentation.core.navigation.FixedNavigator
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.state_view.StateView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import me.dmdev.rxpm.base.PmSupportActivity
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@Suppress("TooManyFunctions")
@SuppressLint("MissingSuperCall")
abstract class BaseActivity<T : BasePm> : PmSupportActivity<T>(),
    HasSupportFragmentInjector,
    BackHandler,
    RouterProvider {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var factory: PmFactory

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    override lateinit var router: Router

    protected abstract val screenLayout: Int

    protected abstract val classToken: Class<T>

    protected open val navigator: Navigator = FixedNavigator(this)

    private val currentFragment: BaseFragment<*>?
        get() = supportFragmentManager.findFragmentById(R.id.containerView) as? BaseFragment<*>

    private var errorStateView: StateView? = null
    private var emptyStateView: StateView? = null
    private var progressView: View? = null
    private var homeButtonView: View? = null

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

    override fun onBackPressed() {
        if (currentFragment?.handleBack() == false) {
            passTo(presentationModel.backAction)
        }
    }

    override fun onBindPresentationModel(pm: T) {
        errorStateView?.let { stateView -> pm.errorControl.bind(stateView, compositeUnbind) }
        emptyStateView?.let { stateView -> pm.emptyControl.bind(stateView, compositeUnbind) }
        progressView?.let { view -> pm.progressState.bindTo(view.visibility()) }
        homeButtonView?.clicks()?.bindTo(pm.backAction)
    }

    override fun providePresentationModel(): T {
        val pm = factory.createViewModel(classToken)
        pm.router = router
        return pm
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

    override fun handleBack(): Boolean {
        passTo(presentationModel.backAction.consumer)
        return true
    }
}