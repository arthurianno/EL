package com.elta.android.domain.features.consultant.interactor

import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class WebimSessionUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {

    fun onResume() {
        repository.webimResume()
    }

    fun onPause() {
        repository.webimPause()
    }

    fun onDestroy() {
        repository.webimDestroy()
    }
}
