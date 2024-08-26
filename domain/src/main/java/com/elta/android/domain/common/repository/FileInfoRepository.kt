package com.elta.android.domain.common.repository

import com.elta.android.domain.common.model.FileInfo

interface FileInfoRepository {
    fun getAll(): List<FileInfo>
    fun update(filesInfo: List<FileInfo>)
    fun clear(ids: List<Long>)
}
