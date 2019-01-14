package com.nullgr.android.presentation.core.ui.fragment

import android.content.Context
import com.nullgr.android.presentation.core.navigation.FlowNavigator
import com.nullgr.android.presentation.core.navigation.FlowRouter
import com.nullgr.android.presentation.core.navigation.RouterProvider
import com.nullgr.android.presentation.core.pm.BasePm
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

abstract class BaseFlowFragment<T : BasePm> : BaseFragment<T>(), RouterProvider {

    @Inject
    lateinit var globalRouter: Router

    private val cicerone by lazy { Cicerone.create(FlowRouter(globalRouter)) }
    private val navigatorHolder: NavigatorHolder by lazy { cicerone.navigatorHolder }
    private lateinit var navigator: Navigator

    override val router: FlowRouter by lazy { cicerone.router }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        navigator = FlowNavigator(this)
    }

    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }
}