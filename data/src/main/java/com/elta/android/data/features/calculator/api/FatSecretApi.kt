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
        @Query("oauth_signature") oauthSignature: String?,
        @Query(REGION_PARAMETER) region: String,
        @Query(LANGUAGE_PARAMETER) language: String,
    ): Observable<FoodGenericResponse>

    @POST("server.api")
    fun getFoodBrand(
        @Query("food_id") foodId: String,
        @Query("method") method: String,
        @Query(REGION_PARAMETER) region: String,
        @Query(LANGUAGE_PARAMETER) language: String,
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
        @Query(FORMAT_PARAMETER) format: String,
        @Query(METHOD_PARAMETER) method: String,
        @Query(REGION_PARAMETER) region: String,
        @Query(LANGUAGE_PARAMETER) language: String,
        @Query(OAUTH_CONSUMER_KEY_PARAMETER) oauthConsumerKey: String? = null,
        @Query(OAUTH_NONCE_PARAMETER) oauthNonce: String? = null,
        @Query(OAUTH_SIGNATURE_METHOD_PARAMETER) oauthSignatureMethod: String? = null,
        @Query(OAUTH_TIMESTAMP_PARAMETER) oauthTimestamp: String? = null,
        @Query(OAUTH_VERSION_PARAMETER) oauthVersion: String? = null,
        @Query(SEARCH_EXPRESSION_PARAMETER) searchExpression: String,
        @Query("oauth_signature") oauthSignature: String?
    ): Observable<FoodsSearchResponse>
}

const val REGION_PARAMETER = "region"
const val LANGUAGE_PARAMETER = "language"
const val FORMAT_PARAMETER = "format"
const val METHOD_PARAMETER = "method"
const val OAUTH_CONSUMER_KEY_PARAMETER = "oauth_consumer_key"
const val OAUTH_NONCE_PARAMETER = "oauth_nonce"
const val OAUTH_SIGNATURE_METHOD_PARAMETER = "oauth_signature_method"
const val OAUTH_TIMESTAMP_PARAMETER = "oauth_timestamp"
const val OAUTH_VERSION_PARAMETER = "oauth_version"
const val SEARCH_EXPRESSION_PARAMETER = "search_expression"

