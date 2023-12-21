package com.elta.android.presentation.core.compose.widgets.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.theme.GetLocalProperties

@Composable
fun HelpText(searchText: String) {
    GetLocalProperties { _, _, colors, _, _ ->
        val textId = if (searchText.isNotBlank()) R.string.calculator_search_result
        else R.string.calculator_last_search

        Text(text = stringResource(textId), color = colors.shadeBlack2)
        VSpacerSmall()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LastWords(
    lastWords: List<String>,
    lastWordClicked: (String) -> Unit
) {
    GetLocalProperties { dimens, _, _, _, _ ->
        val keyboardController = LocalSoftwareKeyboardController.current
        LazyColumn {
            items(items = lastWords) { word ->
                Text(text = word, modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        lastWordClicked.invoke(word)
                        keyboardController?.hide()
                    }
                    .padding(vertical = dimens.lastWordVertical))
            }
        }
    }
}