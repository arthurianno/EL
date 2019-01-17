package com.elta.android.data.di

import com.elta.android.data.features.feature1.repository.TestDataRepository
import com.elta.android.domain.features.feature1.repository.TestRepository
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