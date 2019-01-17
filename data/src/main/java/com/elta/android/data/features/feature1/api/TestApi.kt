package com.elta.android.data.features.feature1.api

import io.reactivex.Completable
import retrofit2.http.GET

interface TestApi {

    @GET("/")
    fun getTestModel(): Completable
}
