package com.elta.android.presentation.features.language.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elta.android.presentation.R
import com.elta.android.presentation.features.language.model.AppLanguage
import com.elta.android.presentation.features.language.model.AppRegion
import com.elta.android.presentation.features.onboaring.ui.compose.OnboardingOption
import com.elta.android.presentation.features.onboaring.ui.compose.OnboardingSelectionScreen
import com.elta.android.presentation.theme.LocalTypes
import com.elta.android.presentation.theme.EltaTheme

@Composable
internal fun LocaleSelectionScreen(
    language: AppLanguage,
    region: AppRegion,
    regions: List<AppRegion>,
    isFirstLaunch: Boolean = true,
    initialRegionStep: Boolean = false,
    onLanguageSelected: (AppLanguage) -> Unit,
    onRegionSelected: (AppRegion) -> Unit,
    onComplete: () -> Unit,
    onExit: () -> Unit = {}
) {
    var isRegionStep by rememberSaveable { mutableStateOf(initialRegionStep) }
    var showPicker by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (isRegionStep) isRegionStep = false else onExit()
    }

    Box {
        if (isRegionStep) {
            OnboardingSelectionScreen(
                title = stringResource(R.string.onboarding_region_title),
                subtitle = stringResource(R.string.onboarding_region_subtitle),
                notice = stringResource(R.string.onboarding_region_notice),
                options = listOf(
                    OnboardingOption("current", stringResource(region.displayNameResId)),
                    OnboardingOption("other", stringResource(R.string.onboarding_choose_other_region))
                ),
                selectedKey = "current",
                buttonText = stringResource(R.string.profile_settings_choose),
                buttonEnabled = true,
                onSelect = { if (it == "other") showPicker = true },
                onContinue = onComplete
            )
        } else {
            OnboardingSelectionScreen(
                title = stringResource(R.string.onboarding_language_title),
                subtitle = stringResource(R.string.onboarding_language_subtitle),
                options = listOf(
                    OnboardingOption("current", stringResource(if (language == AppLanguage.RU) R.string.language_russian else R.string.language_english)),
                    OnboardingOption("other", stringResource(R.string.onboarding_choose_other_language))
                ),
                selectedKey = "current",
                buttonText = stringResource(R.string.profile_settings_choose),
                buttonEnabled = true,
                onSelect = { if (it == "other") showPicker = true },
                onContinue = { isRegionStep = true }
            )
        }

        if (!isFirstLaunch) {
            IconButton(
                onClick = { if (isRegionStep) isRegionStep = false else onExit() },
                modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    painter = painterResource(if (isRegionStep) R.drawable.ic_back else R.drawable.ic_dialog_close),
                    contentDescription = stringResource(if (isRegionStep) R.string.on_boarding_prev_button_title else R.string.close),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showPicker) {
        val title = if (isRegionStep) R.string.onboarding_region_title else R.string.onboarding_language_title
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(stringResource(title)) },
            text = {
                Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    if (isRegionStep) {
                        regions.forEach { item ->
                            PickerItem(stringResource(item.displayNameResId)) {
                                onRegionSelected(item)
                                showPicker = false
                            }
                        }
                    } else {
                        AppLanguage.entries.forEach { item ->
                            PickerItem(stringResource(if (item == AppLanguage.RU) R.string.language_russian else R.string.language_english)) {
                                onLanguageSelected(item)
                                showPicker = false
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.cancel_text)) }
            }
        )
    }
}

@Composable
private fun PickerItem(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = LocalTypes.current.body1,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp)
    )
}

@Preview(name = "Язык приложения", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun LanguageOnboardingPreview() {
    EltaTheme {
        LocaleSelectionScreen(
            language = AppLanguage.RU,
            region = AppRegion.RUSSIA,
            regions = AppRegion.firstLaunchRegions(),
            onLanguageSelected = {},
            onRegionSelected = {},
            onComplete = {}
        )
    }
}

@Preview(name = "Страна проживания", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun RegionOnboardingPreview() {
    EltaTheme {
        LocaleSelectionScreen(
            language = AppLanguage.RU,
            region = AppRegion.RUSSIA,
            regions = AppRegion.firstLaunchRegions(),
            initialRegionStep = true,
            onLanguageSelected = {},
            onRegionSelected = {},
            onComplete = {}
        )
    }
}
