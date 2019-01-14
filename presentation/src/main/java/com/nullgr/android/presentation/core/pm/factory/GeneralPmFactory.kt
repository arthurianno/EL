package com.nullgr.android.presentation.core.pm.factory

import me.dmdev.rxpm.PresentationModel
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class GeneralPmFactory @Inject constructor(
    private val providers: Map<Class<out PresentationModel>, @JvmSuppressWildcards Provider<PresentationModel>>
) : PmFactory {
    override fun <T : PresentationModel> createViewModel(modelClass: Class<T>): T {
        val provider = providers[modelClass]
        return when {
            provider != null -> provider.get() as T
            else -> throw PmProviderNotFoundException(modelClass)
        }
    }
}