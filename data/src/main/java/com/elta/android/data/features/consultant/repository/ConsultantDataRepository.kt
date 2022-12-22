package com.elta.android.data.features.consultant.repository

import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import ru.webim.android.sdk.WebimSession
import javax.inject.Inject

class ConsultantDataRepository @Inject constructor(
    private val webimSession: WebimSession
) : ConsultantRepository {
    override fun webimResume() {
        webimSession.resume()
    }

    override fun webimPause() {
        webimSession.pause()
    }

    override fun webimDestroy() {
        webimSession.destroy()
    }
}
