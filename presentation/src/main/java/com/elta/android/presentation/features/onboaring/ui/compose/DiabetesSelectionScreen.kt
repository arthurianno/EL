package com.elta.android.presentation.features.onboaring.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.R
import com.elta.android.presentation.features.onboaring.ui.adapter.items.DiabetesChoice
import com.elta.android.presentation.theme.EltaTheme

@Composable
internal fun DiabetesSelectionScreen(
    selected: DiabetesChoice?,
    onSelect: (DiabetesChoice) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingSelectionScreen(
        title = stringResource(R.string.on_boarding_header_user_diabetes_type),
        options = listOf(
            OnboardingOption(DiabetesChoice.FIRST.name, stringResource(R.string.diabetes_type_first)),
            OnboardingOption(DiabetesChoice.SECOND.name, stringResource(R.string.diabetes_type_second)),
            OnboardingOption(DiabetesChoice.SECOND_TABLETS.name, stringResource(R.string.diabetes_type_second_tablets)),
            OnboardingOption(DiabetesChoice.UNKNOWN.name, stringResource(R.string.onboarding_diabetes_unknown)),
            OnboardingOption(DiabetesChoice.NONE.name, stringResource(R.string.onboarding_diabetes_none))
        ),
        selectedKey = selected?.name,
        buttonText = stringResource(R.string.profile_settings_choose),
        buttonEnabled = selected != null,
        onSelect = { key -> onSelect(DiabetesChoice.valueOf(key)) },
        onContinue = onContinue
    )
}

@Preview(name = "Тип диабета", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DiabetesSelectionPreview() {
    EltaTheme { DiabetesSelectionScreen(null, onSelect = {}, onContinue = {}) }
}

@Preview(name = "Тип диабета выбран", showBackground = true, widthDp = 375, heightDp = 812, locale = "ru")
@Composable
private fun DiabetesSelectionSelectedPreview() {
    EltaTheme { DiabetesSelectionScreen(DiabetesChoice.FIRST, onSelect = {}, onContinue = {}) }
}
