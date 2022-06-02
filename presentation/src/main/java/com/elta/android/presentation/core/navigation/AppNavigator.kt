package com.elta.android.presentation.core.navigation

import androidx.fragment.app.FragmentActivity
import com.elta.android.presentation.R

class AppNavigator(
    activity: FragmentActivity,

) : ExtendedNavigator(activity, activity.supportFragmentManager, R.id.containerView)
