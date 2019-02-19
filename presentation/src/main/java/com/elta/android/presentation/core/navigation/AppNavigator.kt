package com.elta.android.presentation.core.navigation

import android.support.v4.app.FragmentActivity
import com.elta.android.presentation.R

class AppNavigator(
    activity: FragmentActivity
) : ExtendedNavigator(activity, activity.supportFragmentManager, R.id.containerView)