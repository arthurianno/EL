package com.elta.android.presentation.features.profile.settings.global.ui.builder

import com.elta.android.common.utils.CommonFormats.FORMAT_SIMPLE_DATE
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.appsettings.model.BackendVariant
import com.elta.android.domain.features.user.interactor.googleFitApp
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.BuildConfig
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingRadioButtonItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHealthAppItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.utils.createFullName
import com.elta.android.presentation.utils.toString
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import org.threeten.bp.LocalDate
import javax.inject.Inject

class ProfileSettingsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun buildItems(profile: Profile, backendVariant: BackendVariant) =
        arrayListOf<ListItem>().apply {
            with(profile) {
                add(ProfileSettingsHeaderItem(resources.getString(R.string.profile_personal_information)))
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_acc_name,
                        title = createFullName(resources.getString(R.string.profile_full_name_placeholder)),
                        type = ProfileSettingsItem.Type.NAME
                    )
                )
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_settings_gender,
                        title = this.gender.toString(resources),
                        type = ProfileSettingsItem.Type.GENDER
                    )
                )
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_calendar,
                        title = createDataString(profile.birthDate),
                        type = if (profile.birthDate == null) ProfileSettingsItem.Type.BIRTH_DATE_PLACEHOLDER
                        else ProfileSettingsItem.Type.BIRTH_DATE
                    )
                )

                add(ProfileSettingsHeaderItem(resources.getString(R.string.profile_security)))
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_key_pass,
                        title = resources.getString(R.string.profile_change_password),
                        type = ProfileSettingsItem.Type.PASSWORD
                    )
                )
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_mail,
                        title = email.orEmpty(),
                        type = ProfileSettingsItem.Type.EMAIL
                    )
                )

                if (BuildConfig.DEBUG) {
                    add(
                        ProfileSettingsItem(
                            icon = R.drawable.ic_mail,
                            title = "Copy Token",
                            type = ProfileSettingsItem.Type.TOKEN
                        )
                    )
                    add(
                        ProfileSettingRadioButtonItem(
                            type = backendVariant
                        )
                    )
                }

                add(ProfileSettingsHeaderItem(resources.getString(R.string.profile_additional_settings)))
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_notification,
                        title = resources.getString(R.string.profile_notification),
                        type = ProfileSettingsItem.Type.NOTIFICATION
                    )
                )
                if (BuildConfig.SHOW_LANGUAGE_SELECTION) {
                    add(
                        ProfileSettingsItem(
                            icon = R.drawable.ic_language,
                            title = resources.getString(R.string.profile_language),
                            type = ProfileSettingsItem.Type.LANGUAGE
                        )
                    )
                }
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_glucose_format,
                        title = resources.getString(R.string.profile_glucose_format),
                        type = ProfileSettingsItem.Type.GLUCOSE_FORMAT
                    )
                )
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_doc,
                        title = resources.getString(R.string.profile_legal_information),
                        type = ProfileSettingsItem.Type.LEGAL_INFO
                    )
                )
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_emias,
                        title = resources.getString(R.string.profile_emias_account),
                        type = ProfileSettingsItem.Type.EMIAS_ACCOUNT
                    )
                )
                createHealthAppItem(profile)?.let { add(it) }
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_trash_box,
                        title = resources.getString(R.string.profile_delete),
                        type = ProfileSettingsItem.Type.DELETE_PROFILE
                    )
                )
                add(
                    ProfileSettingsItem(
                        icon = R.drawable.ic_app_info,
                        title = resources.getString(
                            R.string.profile_app_version,
                            BuildConfig.APP_VERSION
                        ),
                        type = ProfileSettingsItem.Type.APP_VERSION
                    )
                )
            }
        }

    private fun createDataString(date: LocalDate?): String =
        date?.toStringWithFormat(FORMAT_SIMPLE_DATE)
            ?: resources.getString(R.string.profile_birth_date_placeholder)

    private fun createHealthAppItem(profile: Profile): ListItem? =
        profile.googleFitApp()?.let {
            ProfileSettingsHealthAppItem(
                icon = R.drawable.ic_google_fit,
                title = resources.getString(R.string.profile_settings_google_fit),
                isActive = it.isActive,
                type = it.type
            )
        }
}
