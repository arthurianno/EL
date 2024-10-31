package com.elta.android.data.features.appsettings

import com.elta.android.data.features.common.storage.DbSyncStorage
import com.elta.android.data.features.common.storage.PreferencesHolder
import com.elta.android.domain.features.appsettings.AppSettingsRepository
import com.elta.android.domain.features.appsettings.model.BackendVariant
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AppSettingsDataRepository @Inject constructor(
    private val preferencesHolder: PreferencesHolder,
    private val syncStorage: DbSyncStorage
) : AppSettingsRepository {
    override fun getBackendVariant(): Single<BackendVariant> =
        Single.fromCallable {
            preferencesHolder.backendVariant?.let { BackendVariant.valueOf(it) } ?: BackendVariant.DEV
        }

    override fun changeBackendVariant(server: BackendVariant) =
        Completable.fromAction {
            preferencesHolder.backendVariant = server.name
        }

    override fun deleteDbFiles(): Completable = syncStorage.deleteDbFiles()

    override var shouldManualGlucoseDialogShow: Boolean
        get() = preferencesHolder.manualGlucoseRemind
        set(value) {
            preferencesHolder.manualGlucoseRemind = value
        }
}
