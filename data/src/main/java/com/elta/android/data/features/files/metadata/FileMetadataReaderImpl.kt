package com.elta.android.data.features.files.metadata

import android.media.MediaMetadataRetriever
import javax.inject.Inject


class FileMetadataReaderImpl @Inject constructor() : FileMetadataReader {

    override fun getMediaDuration(filePath: String): Int {
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(filePath)
            val durationData =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationData?.toInt() ?: 0
            retriever.close()
            duration
        } catch (e: Exception) {
            0
        } finally {
            retriever.release()
        }
    }
}
