package com.elta.android.common.logger

import android.util.Log
import timber.log.Timber

/**
 * A [Timber.Tree] for release builds. Prints messages only with [Log.INFO] priority.
 */
class ReleaseTree : Timber.DebugTree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority == Log.INFO
}