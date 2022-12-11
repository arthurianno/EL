package com.elta.android.data.features.calculator.datasource

import android.util.Base64
import com.elta.android.common.di.qualifires.FatSecret
import com.elta.android.common.di.qualifires.FatSecretAnnotationType
import com.elta.android.common.errors.FatSecretErrors
import com.elta.android.data.features.calculator.api.FatSecretApi
import com.elta.android.data.features.calculator.api.FatSecretTokenApi
import com.elta.android.data.features.calculator.mapper.compactFoodsToDomain
import com.elta.android.data.features.calculator.mapper.toDomain
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
private const val METHOD_SEARCH_FOOD = "foods.search"
private const val FATSECRET_GRAND_TYPE = "client_credentials"
private const val FATSECRET_SCOPE = "basic"
private const val POST_METHOD = "POST"
private const val SIGNATURE_METHOD = "HMAC-SHA1"
private const val HMAC_SHA1_ALGORITHM = "HmacSHA1"
private const val OAUTH_VERSION = "1.0"
private const val STRING_SEPARATOR = "&"
private const val FOOD_ID_PARAMETER = "food_id"
private const val METHOD_PARAMETER = "method"
private const val FORMAT_PARAMETER = "format"
private const val SEARCH_EXPRESSION_PARAMETER = "search_expression"
private const val OAUTH_CONSUMER_KEY_PARAMETER = "oauth_consumer_key"
private const val OAUTH_SIGNATURE_METHOD_PARAMETER = "oauth_signature_method"
private const val OAUTH_TIMESTAMP_PARAMETER = "oauth_timestamp"
private const val OAUTH_NONCE_PARAMETER = "oauth_nonce"
private const val OAUTH_VERSION_PARAMETER = "oauth_version"
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

    fun getFood(id: String, type: DishType): Flow<Dish> {
        val timeStamp = getTimeStamp()
        val nonce = UUID.randomUUID().toString()
        val oauthSignature = mapOf(
            FOOD_ID_PARAMETER to id,
            METHOD_PARAMETER to METHOD_GET_FOOD,
            FORMAT_PARAMETER to FORMAT_RESPONSE,
            OAUTH_TIMESTAMP_PARAMETER to timeStamp,
            OAUTH_NONCE_PARAMETER to nonce
        ).createBaseString()
            .hmacSha1Signature()
            .takeIf { !isOAuth2 }
        return when (type) {
            DishType.Generic -> runFlowWithCatchToken {
                api.getFoodGeneric(
                    foodId = id,
                    method = METHOD_GET_FOOD,
                    format = FORMAT_RESPONSE,
                    oauthSignature = oauthSignature,
                    oauthConsumerKey = consumerKey.takeIf { !isOAuth2 },
                    oauthSignatureMethod = SIGNATURE_METHOD.takeIf { !isOAuth2 },
                    oauthTimestamp = timeStamp.takeIf { !isOAuth2 },
                    oauthNonce = nonce.takeIf { !isOAuth2 },
                    oauthVersion = OAUTH_VERSION.takeIf { !isOAuth2 }
                ).asFlow()
            }.map { it.food.toDomain() }

            DishType.Brand -> runFlowWithCatchToken {
                api.getFoodBrand(
                    foodId = id,
                    method = METHOD_GET_FOOD,
                    format = FORMAT_RESPONSE,
                    oauthSignature = oauthSignature,
                    oauthConsumerKey = consumerKey.takeIf { !isOAuth2 },
                    oauthSignatureMethod = SIGNATURE_METHOD.takeIf { !isOAuth2 },
                    oauthTimestamp = timeStamp.takeIf { !isOAuth2 },
                    oauthNonce = nonce.takeIf { !isOAuth2 },
                    oauthVersion = OAUTH_VERSION.takeIf { !isOAuth2 }
                ).asFlow()
            }.map { it.food.toDomain() }
        }
    }

    fun getFoods(name: String): Flow<List<Dish>> {
        val timeStamp = getTimeStamp()
        val nonce = UUID.randomUUID().toString()
        val oauthSignature = mapOf(
            SEARCH_EXPRESSION_PARAMETER to name,
            METHOD_PARAMETER to METHOD_SEARCH_FOOD,
            FORMAT_PARAMETER to FORMAT_RESPONSE,
            OAUTH_TIMESTAMP_PARAMETER to timeStamp,
            OAUTH_NONCE_PARAMETER to nonce
        )
            .createBaseString()
            .hmacSha1Signature()
            .takeIf { !isOAuth2 }
        return runFlowWithCatchToken {
            api.getFoods(
                searchWord = name,
                method = METHOD_SEARCH_FOOD,
                format = FORMAT_RESPONSE,
                oauthSignature = oauthSignature,
                oauthConsumerKey = consumerKey.takeIf { !isOAuth2 },
                oauthSignatureMethod = SIGNATURE_METHOD.takeIf { !isOAuth2 },
                oauthTimestamp = timeStamp.takeIf { !isOAuth2 },
                oauthNonce = nonce.takeIf { !isOAuth2 },
                oauthVersion = OAUTH_VERSION.takeIf { !isOAuth2 }
            )
                .asFlow()
        }.map { it.foods.food?.compactFoodsToDomain() ?: emptyList() }
    }

    private fun <T> runFlowWithCatchToken(apiMethod: () -> Flow<T>) =
        apiMethod()
            .catch {
                if (it is FatSecretErrors.TokenError) {
                    emitAll(
                        refreshToken().flatMapLatest {
                            apiMethod()
                        }
                    )
                }
            }

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
}
