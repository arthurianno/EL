package com.elta.android.presentation.core.navigation

import android.support.v4.app.Fragment
import com.elta.android.presentation.R

open class FragmentNavigator(
    fragment: Fragment
) : ExtendedNavigator(fragment.activity, fragment.childFragmentManager, R.id.containerView)