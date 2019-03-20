package com.elta.android.data.features.user.api

import com.elta.android.common.utils.log
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.user.api.request.ShortUserSettingsRequest
import com.elta.android.data.features.user.dto.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class MockedSettingsApi : SettingsApi {

    override fun updateUserSettings(request: ShortUserSettingsRequest): Completable =
        Completable.complete()

    override fun getUserSettings(): Single<ProfileDto> =
        Observable.fromCallable {
            ProfileDto(
                diabetType = DiabetTypeDto.LADA,
                weight = 80.0,
                gender = GenderTypeDto.MALE,
                person = PersonDto(
                    firstName = "Анатолий",
                    lastName = "Савин"
                ),
                glucoseLevel = GlucoseLevelDto(
                    1.2, 5.7
                ),
                email = "test@gmail.com",
                timeStamp = Date().time.toInt()
            )
        }.log("Settings", "profile") { it.toString() }
            .singleOrError()
}