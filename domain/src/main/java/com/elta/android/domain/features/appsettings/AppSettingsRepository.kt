package com.elta.android.domain.features.appsettings

import com.elta.android.domain.features.appsettings.model.BackendVariant
import io.reactivex.Completable
import io.reactivex.Single

interface AppSettingsRepository {
    fun getBackendVariant(): Single<BackendVariant>
    fun changeBackendVariant(server: BackendVariant): Completable
    fun deleteDbFiles(): Completable
    var shouldManualGlucoseDialogShow: Boolean
}
