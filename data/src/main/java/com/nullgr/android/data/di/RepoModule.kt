package com.nullgr.android.data.di

import com.nullgr.android.data.features.feature1.repository.TestDataRepository
import com.nullgr.android.domain.features.feature1.repository.TestRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
@Suppress("TooManyFunctions")
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindTestRepository(repo: TestDataRepository): TestRepository
}