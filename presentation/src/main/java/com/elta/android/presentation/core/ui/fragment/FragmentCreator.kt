package com.elta.android.presentation.core.ui.fragment

import androidx.fragment.app.Fragment

interface FragmentCreator {
    fun create(tag: String): Fragment
}
