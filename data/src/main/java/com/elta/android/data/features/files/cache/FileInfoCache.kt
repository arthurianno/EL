package com.elta.android.data.features.files.cache

import com.elta.android.data.features.common.cache.BoxCache
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.files.cache.model.FileInfoDbEntity
import javax.inject.Inject

class FileInfoCache @Inject constructor(
    factory: BoxStoreFactory
) : BoxCache<FileInfoDbEntity>(factory) {
    override val classToken: Class<FileInfoDbEntity> = FileInfoDbEntity::class.java
}
