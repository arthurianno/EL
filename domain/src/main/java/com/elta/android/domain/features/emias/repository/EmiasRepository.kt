package com.elta.android.domain.features.emias.repository

import com.elta.android.domain.features.emias.model.Emias
import com.elta.android.domain.features.emias.model.EmiasStatus
import io.reactivex.Completable

interface EmiasRepository {

    fun updateInfo(emias: Emias): Completable

    suspend fun getStatus(): Pair<EmiasStatus, Emias?>

    suspend fun unbindProfile()
}
