package com.elta.android.common.di.scope

import javax.inject.Scope

/**
 * A scoping annotation to permit objects whose lifetime should
 * conform to the life of the service to be memorized in the
 * correct component.
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceScope
