package com.elta.android.presentation.core.ui.fragment

import androidx.fragment.app.Fragment

@Deprecated("Класс не используется. Можно удалить")
interface FragmentCreator {
    fun create(tag: String): Fragment
}
