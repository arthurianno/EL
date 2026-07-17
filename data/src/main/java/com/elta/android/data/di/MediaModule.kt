package com.elta.android.data.di

import com.elta.android.data.features.consultant.repository.MediaDataRepository
import com.elta.android.data.features.files.metadata.FileMetadataReader
import com.elta.android.data.features.files.metadata.FileMetadataReaderImpl
import com.elta.android.data.features.files.storage.FileStorage
import com.elta.android.data.features.files.storage.FileStorageImpl
import com.elta.android.domain.common.repository.MediaRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface MediaModule {

    @Binds
    @Singleton
    fun bindFileStorage(source: FileStorageImpl): FileStorage

    @Binds
    @Singleton
    fun bindFileMetadataReader(source: FileMetadataReaderImpl): FileMetadataReader

    @Binds
    @Singleton
    fun bindMediaRepository(source: MediaDataRepository): MediaRepository
}
