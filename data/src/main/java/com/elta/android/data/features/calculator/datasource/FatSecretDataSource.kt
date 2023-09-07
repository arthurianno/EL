package com.elta.android.data.features.calculator.datasource

import android.util.Base64
import com.elta.android.common.di.qualifires.FatSecret
import com.elta.android.common.di.qualifires.FatSecretAnnotationType
import com.elta.android.common.errors.FatSecretErrors
import com.elta.android.common.errors.ServiceUnavailableError
import com.elta.android.data.features.calculator.api.FORMAT_PARAMETER
import com.elta.android.data.features.calculator.api.FatSecretApi
import com.elta.android.data.features.calculator.api.FatSecretTokenApi
import com.elta.android.data.features.calculator.api.LANGUAGE_PARAMETER
import com.elta.android.data.features.calculator.api.MAX_RESULTS_PARAMETER
import com.elta.android.data.features.calculator.api.METHOD_PARAMETER
import com.elta.android.data.features.calculator.api.OAUTH_CONSUMER_KEY_PARAMETER
import com.elta.android.data.features.calculator.api.OAUTH_NONCE_PARAMETER
import com.elta.android.data.features.calculator.api.OAUTH_SIGNATURE_METHOD_PARAMETER
import com.elta.android.data.features.calculator.api.OAUTH_TIMESTAMP_PARAMETER
import com.elta.android.data.features.calculator.api.OAUTH_VERSION_PARAMETER
import com.elta.android.data.features.calculator.api.PAGE_NUMBER_PARAMETER
import com.elta.android.data.features.calculator.api.REGION_PARAMETER
import com.elta.android.data.features.calculator.api.SEARCH_EXPRESSION_PARAMETER
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.data.features.calculator.model.SearchFoodsResponse
import com.elta.android.data.features.calculator.storage.FatSecretStorage
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import java.net.URLEncoder
import java.util.Date
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject


private const val FORMAT_RESPONSE = "json"
private const val METHOD_GET_FOOD = "food.get.v2"
private const val METHOD_SEARCH_FOOD = "foods.search.v2"
private const val FATSECRET_GRAND_TYPE = "client_credentials"
private const val FATSECRET_SCOPE = "basic"
private const val POST_METHOD = "POST"
private const val SIGNATURE_METHOD = "HMAC-SHA1"
private const val HMAC_SHA1_ALGORITHM = "HmacSHA1"
private const val OAUTH_VERSION = "1.0"
private const val REGION = "RU"
private const val LANGUAGE = "ru"
private const val STRING_SEPARATOR = "&"
private const val FOOD_ID_PARAMETER = "food_id"

private const val OAUTH_BASE_URL = "https://platform.fatsecret.com/rest/server.api"

class FatSecretDataSource @Inject constructor(
    @FatSecret(FatSecretAnnotationType.ClientId) private val clientId: String,
    @FatSecret(FatSecretAnnotationType.ClientSecret) private val clientSecret: String,
    @FatSecret(FatSecretAnnotationType.ConsumerKey) private val consumerKey: String,
    @FatSecret(FatSecretAnnotationType.ConsumerSecret) private val consumerSecret: String,
    @FatSecret(FatSecretAnnotationType.IsOAuth2) private val isOAuth2: Boolean,
    private val storage: FatSecretStorage,
    private val api: FatSecretApi,
    private val tokenApi: FatSecretTokenApi
) {

    private val oAuth1BaseParameters = mapOf(
        OAUTH_CONSUMER_KEY_PARAMETER to consumerKey,
        OAUTH_SIGNATURE_METHOD_PARAMETER to SIGNATURE_METHOD,
        OAUTH_VERSION_PARAMETER to OAUTH_VERSION
    )

    private val searchFoodBaseParameters = mapOf(
        METHOD_PARAMETER to METHOD_SEARCH_FOOD,
        REGION_PARAMETER to REGION,
        LANGUAGE_PARAMETER to LANGUAGE,
        FORMAT_PARAMETER to FORMAT_RESPONSE,
    )

    private val foodBaseParameters = mapOf(
        METHOD_PARAMETER to METHOD_GET_FOOD,
        REGION_PARAMETER to REGION,
        LANGUAGE_PARAMETER to LANGUAGE,
        FORMAT_PARAMETER to FORMAT_RESPONSE,
    )

    fun getFood(id: String, type: DishType): Flow<Dish> {
        val timeStamp = getTimeStamp()
        val nonce = UUID.randomUUID().toString()

        val parameters = foodBaseParameters + mapOf(
            FOOD_ID_PARAMETER to id,
            OAUTH_TIMESTAMP_PARAMETER to timeStamp,
            OAUTH_NONCE_PARAMETER to nonce
        )
        val baseString = parameters.createBaseString()
        val oauthSignature = baseString.hmacSha1Signature()

        return when (type) {
            DishType.Generic -> runFlowWithCatchToken {
                api.getFoodGeneric(
                    foodId = id,
                    method = METHOD_GET_FOOD,
                    language = LANGUAGE,
                    region = REGION,
                    format = FORMAT_RESPONSE,
                    oauthSignature = oauthSignature.takeIsAuth1(),
                    oauthConsumerKey = consumerKey.takeIsAuth1(),
                    oauthSignatureMethod = SIGNATURE_METHOD.takeIsAuth1(),
                    oauthTimestamp = timeStamp.takeIsAuth1(),
                    oauthNonce = nonce.takeIsAuth1(),
                    oauthVersion = OAUTH_VERSION.takeIsAuth1()
                ).asFlow()
            }.map { it.food.toDomain() }

            DishType.Brand -> runFlowWithCatchToken {
                api.getFoodBrand(
                    foodId = id,
                    method = METHOD_GET_FOOD,
                    language = LANGUAGE,
                    region = REGION,
                    format = FORMAT_RESPONSE,
                    oauthSignature = oauthSignature,
                    oauthConsumerKey = consumerKey.takeIsAuth1(),
                    oauthSignatureMethod = SIGNATURE_METHOD.takeIsAuth1(),
                    oauthTimestamp = timeStamp.takeIsAuth1(),
                    oauthNonce = nonce.takeIsAuth1(),
                    oauthVersion = OAUTH_VERSION.takeIsAuth1()
                ).asFlow()
            }.map { it.food.toDomain() }
        }
    }

    suspend fun searchDishes(
        name: String,
        pageNumber: Int,
        maxResults: Int
    ): SearchFoodsResponse {
        val timeStamp = getTimeStamp()
        val nonce = UUID.randomUUID().toString()

        val parameters = searchFoodBaseParameters + mapOf(
            SEARCH_EXPRESSION_PARAMETER to name.encode(),
            OAUTH_TIMESTAMP_PARAMETER to timeStamp,
            OAUTH_NONCE_PARAMETER to nonce,
            PAGE_NUMBER_PARAMETER to pageNumber.toString(),
            MAX_RESULTS_PARAMETER to maxResults.toString(),
        )

        val baseString = parameters.createBaseString()

        val oauthSignature = baseString.hmacSha1Signature()

        return api.searchFoods(
            pageNumber = pageNumber,
            maxResults = maxResults,
            format = FORMAT_RESPONSE,
            method = METHOD_SEARCH_FOOD,
            region = REGION,
            language = LANGUAGE,
            oauthConsumerKey = consumerKey.takeIsAuth1(),
            oauthNonce = nonce.takeIsAuth1(),
            oauthSignatureMethod = SIGNATURE_METHOD.takeIsAuth1(),
            oauthTimestamp = timeStamp.takeIsAuth1(),
            oauthVersion = OAUTH_VERSION.takeIsAuth1(),
            searchExpression = name,
            oauthSignature = oauthSignature
        )
    }

    private fun <T> runFlowWithCatchToken(apiMethod: () -> Flow<T>) =
        apiMethod()
            .catch {
                when (it) {
                    is FatSecretErrors.TokenError -> emitAll(
                        refreshToken().flatMapLatest {
                            apiMethod()
                        }
                    )
                    is ServiceUnavailableError -> emitAll(apiMethod())
                }
            }

    //for auth 2
    private fun refreshToken(): Flow<String> =
        tokenApi.getNewToken(
            grantType = FATSECRET_GRAND_TYPE,
            clientId = clientId,
            clientSecret = clientSecret,
            scope = FATSECRET_SCOPE
        )
            .asFlow()
            .map {
                it.accessToken.also { newToken ->
                    storage.token = newToken
                }
            }

    private fun Map<String, String>.createBaseString(): String =
        this.toMutableMap()
            .apply { putAll(oAuth1BaseParameters) }
            .toSortedMap()
            .map { "${it.key}=${it.value}" }
            .joinToString(separator = STRING_SEPARATOR)
            .let { listOf(POST_METHOD, OAUTH_BASE_URL.encode(), it.encode()) }
            .joinToString(separator = STRING_SEPARATOR)

    private fun String.hmacSha1Signature(): String {
        val singKey =
            SecretKeySpec((consumerSecret + STRING_SEPARATOR).toByteArray(), HMAC_SHA1_ALGORITHM)
        val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
        mac.init(singKey)
        return String(Base64.encode(mac.doFinal(this.toByteArray()), Base64.DEFAULT)).trim()
    }

    private fun String.encode(): String =
        URLEncoder.encode(this, "utf-8")
            .replace("+", "%20")
            .replace("!", "%21")
            .replace("*", "%2A")
            .replace("\\", "%27")
            .replace("(", "%28")
            .replace(")", "%29")

    private fun getTimeStamp(): String = Date().time.div(1000).toString()

    private fun <T> T.takeIsAuth1(): T? =
        this.takeIf { !isOAuth2 }
}
