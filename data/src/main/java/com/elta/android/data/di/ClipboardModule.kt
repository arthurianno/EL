package com.elta.android.data.di

import android.content.ClipboardManager
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ClipboardModule {
    @Provides
    @Singleton
    fun provideClipboardManager(context: Context): ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}
