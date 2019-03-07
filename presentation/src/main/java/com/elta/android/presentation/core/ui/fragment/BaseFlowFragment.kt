package com.elta.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.navigation.FragmentNavigator
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import javax.inject.Inject

abstract class BaseFlowFragment<T : BasePm> : BaseFragment<T>(), RouterProvider {

    @Inject
    lateinit var globalRouter: FlowRouter

    private val cicerone by lazy { Cicerone.create(FlowRouter(globalRouter)) }
    private val navigatorHolder: NavigatorHolder by lazy { cicerone.navigatorHolder }
    private lateinit var navigator: Navigator

    private val currentFragment: BaseFragment<*>?
        get() = childFragmentManager.findFragmentById(R.id.containerView) as? BaseFragment<*>

    override val router: FlowRouter by lazy { cicerone.router }

    override val statusBarConfigProvider: StatusBarConfigProvider? = null

    override val backgroundColor: Int? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        navigator = FragmentNavigator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (childFragmentManager.fragments.isEmpty()) {
            (presentationModel as? BaseFlowPm)?.launchScreenAction?.let {
                passTo(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun handleBack() {
        if (currentFragment != null) {
            currentFragment?.handleBack()
        } else {
            router.finishFlow()
        }
    }
}