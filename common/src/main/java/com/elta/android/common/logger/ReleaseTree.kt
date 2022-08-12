package com.elta.android.common.logger

import android.util.Log

class ReleaseTree : BaseTree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority == Log.INFO
}
