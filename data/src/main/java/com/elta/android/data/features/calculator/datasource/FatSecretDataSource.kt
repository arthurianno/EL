package com.elta.android.data.features.calculator.datasource

import com.elta.android.common.di.qualifires.FatSecret
import com.elta.android.common.di.qualifires.FeatSecretAnnotationType
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
import javax.inject.Inject

private const val FATSECRET_GRAND_TYPE = "client_credentials"
private const val FATSECRET_SCOPE = "basic"

class FatSecretDataSource @Inject constructor(
    @FatSecret(FeatSecretAnnotationType.ClientId) private val clientId: String,
    @FatSecret(FeatSecretAnnotationType.ClientSecret) private val clientSecret: String,
    private val storage: FatSecretStorage,
    private val api: FatSecretApi,
    private val tokenApi: FatSecretTokenApi
) {

    fun getFood(id: String, type: DishType): Flow<Dish> =
        when (type) {
            DishType.Generic -> runFlowWithCatchToken {
                api.getFoodGeneric(foodId = id).asFlow()
            }.map { it.food.toDomain() }

            DishType.Brand -> runFlowWithCatchToken {
                api.getFoodBrand(foodId = id).asFlow()
            }.map { it.food.toDomain() }
        }

    fun getFoods(name: String): Flow<List<Dish>> =
        runFlowWithCatchToken {
            api.getFoods(searchWord = name)
                .asFlow()
        }.map { it.foods.food?.compactFoodsToDomain() ?: emptyList() }

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
}
