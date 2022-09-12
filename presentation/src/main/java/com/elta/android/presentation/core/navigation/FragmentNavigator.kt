package com.elta.android.presentation.core.navigation

import androidx.fragment.app.Fragment
import com.elta.android.presentation.R

open class FragmentNavigator(
    fragment: Fragment
) : ExtendedNavigator(fragment.requireActivity(), fragment.childFragmentManager, R.id.containerView)
