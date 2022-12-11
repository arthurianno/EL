package com.elta.android.data.features.calculator.api

import com.elta.android.data.features.calculator.model.FoodBrandResponse
import com.elta.android.data.features.calculator.model.FoodGenericResponse
import com.elta.android.data.features.calculator.model.FoodsSearchResponse
import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query

interface FatSecretApi {

    @POST("server.api")
    fun getFoodGeneric(
        @Query("food_id") foodId: String,
        @Query("method") method: String,
        @Query("format") format: String,
        @Query("oauth_consumer_key") oauthConsumerKey: String?,
        @Query("oauth_signature_method") oauthSignatureMethod: String?,
        @Query("oauth_timestamp") oauthTimestamp: String?,
        @Query("oauth_nonce") oauthNonce: String?,
        @Query("oauth_version") oauthVersion: String?,
        @Query("oauth_signature") oauthSignature: String?
    ): Observable<FoodGenericResponse>

    @POST("server.api")
    fun getFoodBrand(
        @Query("food_id") foodId: String,
        @Query("method") method: String,
        @Query("format") format: String,
        @Query("oauth_consumer_key") oauthConsumerKey: String?,
        @Query("oauth_signature_method") oauthSignatureMethod: String?,
        @Query("oauth_timestamp") oauthTimestamp: String?,
        @Query("oauth_nonce") oauthNonce: String?,
        @Query("oauth_version") oauthVersion: String?,
        @Query("oauth_signature") oauthSignature: String?
    ): Observable<FoodBrandResponse>

    @POST("server.api")
    fun getFoods(
        @Query("format") format: String,
        @Query("method") method: String,
        @Query("oauth_consumer_key") oauthConsumerKey: String? = null,
        @Query("oauth_nonce") oauthNonce: String? = null,
        @Query("oauth_signature_method") oauthSignatureMethod: String? = null,
        @Query("oauth_timestamp") oauthTimestamp: String? = null,
        @Query("oauth_version") oauthVersion: String? = null,
        @Query("search_expression") searchWord: String,
        @Query("oauth_signature") oauthSignature: String?
    ): Observable<FoodsSearchResponse>
}
