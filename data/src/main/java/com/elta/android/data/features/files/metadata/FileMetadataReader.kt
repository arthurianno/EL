package com.elta.android.data.features.files.metadata

interface FileMetadataReader {
    fun getMediaDuration(filePath: String): Int
}
