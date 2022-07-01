package com.elta.android.presentation.core.navigation

import androidx.appcompat.app.AppCompatActivity
import com.elta.android.presentation.R

class AppNavigator(activity: AppCompatActivity) :
    ExtendedNavigator(activity, activity.supportFragmentManager, R.id.containerView)
