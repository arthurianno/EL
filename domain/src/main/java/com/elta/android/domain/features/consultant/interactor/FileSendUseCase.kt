package com.elta.android.domain.features.consultant.interactor

import android.net.Uri
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FileSendUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {

    operator fun invoke(file: Uri): Flow<WebimMessageSendStatus> =
        repository.sendPhoto(file)
}
