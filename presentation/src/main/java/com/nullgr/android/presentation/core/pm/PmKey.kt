package com.nullgr.android.presentation.core.pm

import dagger.MapKey
import me.dmdev.rxpm.PresentationModel
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class PmKey(val value: KClass<out PresentationModel>)