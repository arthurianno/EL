package com.elta.android.presentation.core.ui.fragment

import android.support.v4.app.Fragment

interface FragmentCreator {
    fun create(tag: String): Fragment
}