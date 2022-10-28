package com.elta.android.presentation.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object DaggerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            return modelClass.newInstance()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
