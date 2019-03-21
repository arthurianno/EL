package com.elta.android.data.features.user.api

import com.elta.android.common.utils.log
import com.elta.android.data.features.user.dto.DiabetTypeDto
import com.elta.android.data.features.user.dto.GenderTypeDto
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.PersonDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.nullgr.core.date.toTimestamp
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date

class MockedProfileApi : ProfileApi {

    override fun updateUserSettings(profile: ProfileDto): Completable =
        Completable.complete()

    override fun getUserSettings(): Single<ProfileDto> =
        Observable.fromCallable {
            ProfileDto(
                diabetes = DiabetTypeDto.LADA,
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
                timeStamp = Date().toTimestamp()
            )
        }.log("Settings", "profile") { it.toString() }
            .singleOrError()
}