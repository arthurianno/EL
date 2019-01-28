package com.elta.android.data.di

import com.elta.android.data.features.auth.repository.AuthDataRepository
import com.elta.android.data.features.auth.repository.SocialDataRepository
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.auth.repository.SocialRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
@Suppress("TooManyFunctions")
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repo: AuthDataRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthSocialRepository(repo: SocialDataRepository): SocialRepository
}