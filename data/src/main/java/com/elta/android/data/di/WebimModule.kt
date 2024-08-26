package com.elta.android.data.di

import com.elta.android.common.di.qualifires.WebimAnnotation
import com.elta.android.common.di.qualifires.WebimAnnotationType
import com.elta.android.data.features.consultant.cache.ConsultantMessageCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/*
* Чтобы создать тестовый аккаунт, заходим на сайт Webim, переходим в регистрацию, заполняяем данные любыми данными и
* пишем любой придуманный сайт. Например site123.ru. Создав аккаунт меняем здесь ACCOUNT_NAME на сайт с доменом без точки.
* Пример: site123ru
* Также надо будет закоментить строчку в WebimClient webimUser.toJsonObject(privateKey), так как она работает только с продовым сервером
 */
private const val ACCOUNT_NAME = "eltaltdru" // fixme раскоментить

private const val LOCATION_NAME = "mobile"

private const val PRIVATE_KEY = "7d112ff804823419b208678bd779f81f"

@Module
class WebimModule {

    @Provides
    @WebimAnnotation(WebimAnnotationType.Account)
    fun provideWebimAccountName(): String = ACCOUNT_NAME

    @Provides
    @WebimAnnotation(WebimAnnotationType.Location)
    fun provideWebimLocationName(): String = LOCATION_NAME

    @Provides
    @WebimAnnotation(WebimAnnotationType.PrivateKey)
    fun provideWebimPrivateKey(): String = PRIVATE_KEY

    @Provides
    @Singleton
    fun provideConsultantMessageCache(): ConsultantMessageCache =
        ConsultantMessageCache()
}
