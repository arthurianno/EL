package com.elta.android.presentation.core.navigation.support

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.github.terrakok.cicerone.Screen

/**
 * AppScreen is base class for description and creation application screen.<br></br>
 * NOTE: If you have described the creation of Intent then Activity will be started.<br></br>
 * Recommendation: Use Intents for launch external application.
 */
abstract class SupportAppScreen : Screen {
    private var fragment: Fragment? = null

    open fun getFragment(): Fragment? = fragment

    open fun getActivityIntent(context: Context): Intent? {
        return null
    }
}
