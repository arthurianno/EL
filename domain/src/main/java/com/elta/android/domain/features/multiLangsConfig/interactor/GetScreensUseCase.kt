package com.elta.android.domain.features.multiLangsConfig.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.multiLangsConfig.repository.MultilangConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAllScreensUseCase @Inject constructor(private val repository: MultilangConfigRepository) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        repository.getAllScreens()
    }
}
