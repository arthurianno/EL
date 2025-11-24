package com.elta.android.data.di

import com.elta.android.data.features.consultant.media.AudioPlayer
import com.elta.android.data.features.consultant.media.AudioPlayerImpl
import com.elta.android.data.features.consultant.media.AudioRecorder
import com.elta.android.data.features.consultant.media.AudioRecorderImpl
import com.elta.android.data.features.files.metadata.FileMetadataReader
import com.elta.android.data.features.files.metadata.FileMetadataReaderImpl
import com.elta.android.data.features.files.storage.FileStorage
import com.elta.android.data.features.files.storage.FileStorageImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton


@Module
interface MediaModule {

    @Binds
    @Singleton
    fun bindAudioRecorder(source: AudioRecorderImpl): AudioRecorder

    @Binds
    @Singleton
    fun bindFileStorage(source: FileStorageImpl): FileStorage

    @Binds
    @Singleton
    fun bindAudioPlayer(source: AudioPlayerImpl): AudioPlayer

    @Binds
    @Singleton
    fun bindFileMetadataReader(source: FileMetadataReaderImpl): FileMetadataReader

}
