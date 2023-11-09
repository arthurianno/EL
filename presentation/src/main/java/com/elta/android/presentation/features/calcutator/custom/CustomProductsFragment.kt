package com.elta.android.presentation.features.calcutator.custom

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.calcutator.custom.component.CustomDishes
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CustomProductsViewModel
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
class CustomProductsFragment : BaseComposeFragment<CustomProductsViewModel>() {

    override val viewModel: CustomProductsViewModel by viewModels { viewModelFactory }

    override fun CustomProductsViewModel.init() {
        appTopBar.setTitle(resources.getString(R.string.custom_products_my_products))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        searchField.setHint(getString(R.string.custom_product_search_hint))

        confirmDialog.initDialog(
            title = resources.getString(R.string.custom_product_delete_dialog_title),
            message = resources.getString(R.string.custom_product_delete_dialog_description),
            positiveButtonText = resources.getString(R.string.yes_text),
            negativeButtonText = resources.getString(R.string.no_text)
        )
    }

    @Composable
    override fun Dialogs(viewModel: CustomProductsViewModel) {
        BaseDialog(widgetModel = viewModel.confirmDialog)
    }

    @Composable
    override fun Content(viewModel: CustomProductsViewModel) {
        CustomDishes(viewModel = viewModel)
    }

    @Preview
    @Composable
    private fun PreviewCustomDishes() {
        CustomDishes(viewModel = viewModel())
    }

    companion object {
        fun newInstance(): Fragment {
            return CustomProductsFragment()
        }
    }
}
