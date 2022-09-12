package com.elta.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.navigation.FragmentNavigator
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import me.dmdev.rxpm.passTo
import javax.inject.Inject

abstract class BaseFlowFragment<T : BasePm, B : ViewBinding>(
    bindingInflater: Inflater<B>
) : BaseFragment<T, B>(bindingInflater), RouterProvider {

    @Inject
    lateinit var globalRouter: FlowRouter

    private val cicerone by lazy { Cicerone.create(FlowRouter(globalRouter)) }
    private val navigatorHolder: NavigatorHolder by lazy { cicerone.getNavigatorHolder() }
    private lateinit var navigator: Navigator

    private val currentFragment: BaseFragment<*, *>?
        get() = childFragmentManager.findFragmentById(R.id.containerView) as? BaseFragment<*, *>

    override val router: FlowRouter by lazy { cicerone.router }

    override val statusBarConfigProvider: StatusBarConfigProvider? = null

    override val backgroundColor: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator = FragmentNavigator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (childFragmentManager.fragments.isEmpty()) {
            (presentationModel as? BaseFlowPm)?.launchScreenAction?.let {
                Unit.passTo(it)
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
        if (maybeChildrenHandleBack()) {
            currentFragment?.handleBack()
        } else {
            router.finishFlow()
        }
    }

    @Suppress("UnnecessaryParentheses")
    private fun maybeChildrenHandleBack(): Boolean {
        currentFragment?.let {
            return childFragmentManager.backStackEntryCount > 0 ||
                (it is BaseFlowFragment && it.childFragmentManager.backStackEntryCount > 0)
        }
        return false
    }
}
