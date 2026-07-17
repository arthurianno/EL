package com.elta.android.domain.features.consultant.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.consultant.model.BotNode

interface ConsultantRepository : BaseRepository {
    suspend fun getRootNode(): BotNode
    suspend fun getNodeById(id: String): BotNode?
}
