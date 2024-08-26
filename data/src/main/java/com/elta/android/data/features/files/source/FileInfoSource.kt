package com.elta.android.data.features.files.source

import com.elta.android.data.features.files.cache.model.FileInfoDbEntity

interface FileInfoSource {
    fun getAll(): List<FileInfoDbEntity>
    fun update(filesInfo: List<FileInfoDbEntity>)
    fun clear(ids: List<Long>)
}
