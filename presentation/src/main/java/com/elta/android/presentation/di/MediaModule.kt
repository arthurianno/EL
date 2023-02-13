package com.elta.android.presentation.di

import com.elta.android.presentation.core.media.AudioRecorder
import com.elta.android.presentation.core.media.AudioRecorderImpl
import dagger.Binds
import dagger.Module


@Module
interface MediaModule {

    @Binds
    fun bindAudioRecorder(source: AudioRecorderImpl): AudioRecorder
}