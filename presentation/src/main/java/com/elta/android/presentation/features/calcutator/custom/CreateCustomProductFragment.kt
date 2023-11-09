package com.elta.android.presentation.features.calcutator.custom

import android.os.Bundle
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
import com.elta.android.presentation.features.calcutator.custom.component.CreateCustomDishes
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CreateCustomProductViewModel
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity

@OptIn(ExperimentalComposeUiApi::class)
class CreateCustomProductFragment : BaseComposeFragment<CreateCustomProductViewModel>() {

    override val viewModel: CreateCustomProductViewModel by viewModels { viewModelFactory }

    companion object {
        fun newInstance(dish: DishUiEntity?, productName: String?): Fragment {
            return CreateCustomProductFragment().apply {

                val bundle = Bundle().apply {
                    dish?.let { putParcelable(EXTRA_DISH, it) }
                    productName?.let { putString(EXTRA_PRODUCT_NAME, it) }
                }
                arguments = bundle
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dish = arguments?.getParcelable<DishUiEntity>(EXTRA_DISH)
        val productName = arguments?.getString(EXTRA_PRODUCT_NAME)

        with(viewModel) {
            setDish(dish)
            productName?.let { setProductName(it) }

            val editableProduct = dish == null
            val downButtonTextId = if (editableProduct)
                R.string.custom_products_save_and_add
            else
                R.string.custom_products_add
            downButton.setText(resources.getString(downButtonTextId))

            setEditableProduct(editableProduct)
        }

    }

    override fun CreateCustomProductViewModel.init() {
        appTopBar.setTitle(resources.getString(R.string.custom_products_new_product))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        warningExitDialog.initDialog(
            title = getString(R.string.exit_dialog_title),
            message = getString(R.string.exit_dialog_message),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.no_text)
        )
        errorDialog.initDialog(
            title = getString(R.string.custom_product_save_error_title),
            message = getString(R.string.custom_product_save_error_message),
            positiveButtonText = getString(R.string.ok_text)
        )

        productNameField.setHint(resources.getString(R.string.custom_product_name))

        breadUnitsField.setHint(resources.getString(R.string.custom_product_xe))
        breadUnitsField.setDescription(resources.getString(R.string.custom_product_xe_description))
        breadUnitsField.setHeader(resources.getString(R.string.custom_product_count_xe))

    }

    @Composable
    override fun Content(viewModel: CreateCustomProductViewModel) {
        CreateCustomDishes(viewModel = viewModel)
    }

    @Composable
    override fun Dialogs(viewModel: CreateCustomProductViewModel) {
        BaseDialog(widgetModel = viewModel.warningExitDialog)
        BaseDialog(widgetModel = viewModel.errorDialog)
    }

    @Preview
    @Composable
    private fun PreviewContent() {
        Content(viewModel = viewModel())
    }

}

private const val EXTRA_DISH = "extra_dish"
private const val EXTRA_PRODUCT_NAME = "extra_product_name"
