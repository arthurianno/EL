package com.elta.android.data.features.files.repository

import com.elta.android.data.features.files.source.FileInfoSource
import com.elta.android.data.features.files.toDb
import com.elta.android.data.features.files.toDomain
import com.elta.android.domain.common.repository.FileInfoRepository
import com.elta.android.domain.common.model.FileInfo
import javax.inject.Inject

class FileInfoDataRepository @Inject constructor(
    private val source: FileInfoSource
) : FileInfoRepository {
    override fun getAll(): List<FileInfo> =
        source.getAll()
            .map { it.toDomain() }

    override fun update(filesInfo: List<FileInfo>) {
        val databaseEntity = filesInfo.map { it.toDb() }
        source.update(databaseEntity)
    }

    override fun clear(ids: List<Long>) {
        source.clear(ids)
    }
}
