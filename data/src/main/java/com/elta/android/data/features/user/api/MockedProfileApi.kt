@file:Suppress("MagicNumber")

package com.elta.android.data.features.user.api

import com.elta.android.common.utils.log
import com.elta.android.common.utils.timestamp
import com.elta.android.data.features.user.dto.DiabetTypeDto
import com.elta.android.data.features.user.dto.GenderTypeDto
import com.elta.android.data.features.user.dto.GlucoseLevelDto
import com.elta.android.data.features.user.dto.HealthAppDto
import com.elta.android.data.features.user.dto.HealthAppTypeDto
import com.elta.android.data.features.user.dto.PersonDto
import com.elta.android.data.features.user.dto.ProfileDto
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.data.features.user.dto.SocialNetworkTypeDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

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
                socialNetworks = listOf(
                    SocialNetworkDto(type = SocialNetworkTypeDto.FB, isLinked = true),
                    SocialNetworkDto(type = SocialNetworkTypeDto.VK, isLinked = false),
                    SocialNetworkDto(type = SocialNetworkTypeDto.OK, isLinked = false)
                ),
                healthApps = listOf(
                    HealthAppDto(type = HealthAppTypeDto.GOOGLE_FIT, isActive = true),
                    HealthAppDto(type = HealthAppTypeDto.APPLE_HEALTH, isActive = false)
                ),
                timeStamp = timestamp()
            )
        }.log("Settings", "profile") { it.toString() }
            .singleOrError()
}