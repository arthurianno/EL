@file:Suppress("MagicNumber")

package com.elta.android.data.features.user.api

import com.elta.android.common.utils.log
import com.elta.android.common.utils.timestamp
import com.elta.android.data.features.user.dto.DiabetesTypeNetworkEntity
import com.elta.android.data.features.user.dto.GenderTypeNetworkEntity
import com.elta.android.data.features.user.dto.GlucoseFormatNetworkEntity
import com.elta.android.data.features.user.dto.GlucoseLevelNetworkEntity
import com.elta.android.data.features.user.dto.HealthAppNetworkEntity
import com.elta.android.data.features.user.dto.HealthAppTypeNetworkEntity
import com.elta.android.data.features.user.dto.PersonNetworkEntity
import com.elta.android.data.features.user.dto.ProfileNetworkResponse
import com.elta.android.data.features.user.dto.ProfileSettingsNetworkResponse
import com.elta.android.data.features.user.dto.SocialNetworkDto
import com.elta.android.data.features.user.dto.SocialNetworkTypeNetworkEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class MockedProfileApi : ProfileApi {

    override fun updateProfile(profile: ProfileNetworkResponse): Completable =
        Completable.complete()

    override fun getProfile(): Single<ProfileNetworkResponse> =
        Observable.fromCallable {
            ProfileNetworkResponse(
                diabetes = DiabetesTypeNetworkEntity.FIRST,
                weight = 80.0,
                gender = GenderTypeNetworkEntity.MALE,
                person = PersonNetworkEntity(
                    firstName = "Анатолий",
                    lastName = "Савин"
                ),
                glucoseLevelsAverage = GlucoseLevelNetworkEntity(
                    1.2,
                    5.7
                ),
                glucoseLevelsBeforeEating = GlucoseLevelNetworkEntity(
                    1.2,
                    5.7
                ),
                glucoseLevelsAfterEating = GlucoseLevelNetworkEntity(
                    1.2,
                    5.7
                ),
                email = "test@gmail.com",
                socialNetworks = listOf(
                    SocialNetworkDto(type = SocialNetworkTypeNetworkEntity.FB, isLinked = true),
                    SocialNetworkDto(type = SocialNetworkTypeNetworkEntity.VK, isLinked = false),
                    SocialNetworkDto(type = SocialNetworkTypeNetworkEntity.OK, isLinked = false)
                ),
                healthApps = listOf(
                    HealthAppNetworkEntity(
                        type = HealthAppTypeNetworkEntity.GOOGLE_FIT,
                        isActive = true
                    ),
                    HealthAppNetworkEntity(
                        type = HealthAppTypeNetworkEntity.APPLE_HEALTH,
                        isActive = false
                    )
                ),
                timeStamp = timestamp()
            )
        }.log("Settings", "profile") { it.toString() }
            .singleOrError()

    override fun getProfileSettings(): Single<ProfileSettingsNetworkResponse> =
        Single.just(
            ProfileSettingsNetworkResponse(
                isOnboarded = false,
                glucoseFormat = GlucoseFormatNetworkEntity.CAPILLARY
            )
        )

    override fun updateProfileSettings(settings: ProfileSettingsNetworkResponse): Completable =
        Completable.complete()
}
