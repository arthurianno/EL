package com.elta.android.common.di.qualifires

import javax.inject.Qualifier

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class WebimAnnotation(val type: WebimAnnotationType)

enum class WebimAnnotationType {
    Account,
    Location,
    PrivateKey
}
