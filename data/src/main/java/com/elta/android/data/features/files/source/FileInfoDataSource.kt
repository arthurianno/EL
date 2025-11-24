package com.elta.android.data.features.files.source

import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.files.cache.FileInfoCache
import com.elta.android.data.features.files.cache.model.FileInfoDbEntity
import javax.inject.Inject

class FileInfoDataSource @Inject constructor(
    private val cache: FileInfoCache
): FileInfoSource {
    override fun getAll(): List<FileInfoDbEntity> =
        cache.getAll(CommonConditions.All)

    override fun update(filesInfo: List<FileInfoDbEntity>) {
        cache.add(filesInfo)
    }

    override fun clear(ids: List<Long>) {
        cache.delete(CommonConditions.ByIds(ids))
    }
}
