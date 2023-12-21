package com.elta.android.domain.features.diary.medicines.interactor

import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.medicines.repository.MedicamentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentlyUsedMedicamentsUseCase @Inject constructor(
    private val repository: MedicamentRepository
) {
    operator fun invoke(): Flow<List<Pair<Medicament, String?>>> {
        return repository.getRecentlyUsed()
    }
}