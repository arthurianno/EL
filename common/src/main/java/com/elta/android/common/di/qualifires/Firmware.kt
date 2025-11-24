package com.elta.android.common.di.qualifires

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Firmware(val type: UpdateType)

enum class UpdateType {
    NordicDfu, BootMode
}
