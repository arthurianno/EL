package com.elta.android.presentation.features.calcutator.custom

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.calcutator.custom.component.CreateCustomDishes
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductFlow
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CreateCustomProductViewModel
import com.elta.android.presentation.features.calcutator.mappers.isCarbohydrateValid
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity

@OptIn(ExperimentalComposeUiApi::class)
class CreateCustomProductFragment : BaseComposeFragment<CreateCustomProductViewModel>() {

    override val viewModel: CreateCustomProductViewModel by viewModels { viewModelFactory }

    companion object {
        fun newInstance(
            dish: DishUiEntity?,
            productName: String?,
            calculatorFlow: CalculatorFlow
        ): Fragment {
            return CreateCustomProductFragment().apply {

                val bundle = Bundle().apply {
                    dish?.let { putParcelable(EXTRA_DISH, it) }
                    productName?.let { putString(EXTRA_PRODUCT_NAME, it) }
                    putParcelable(EXTRA_CALCULATOR_FLOW_DATA, calculatorFlow)
                }
                arguments = bundle
            }
        }

        private const val EXTRA_CALCULATOR_FLOW_DATA = "extra_calculator_flow_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dish = arguments?.getParcelable<DishUiEntity>(EXTRA_DISH)
        val productName = arguments?.getString(EXTRA_PRODUCT_NAME)
        val calculatorFlow = arguments?.getParcelable<CalculatorFlow>(EXTRA_CALCULATOR_FLOW_DATA)

        with(viewModel) {
            setParams(dish, calculatorFlow)
            productName?.let {
                productNameField.setText(it)
            }

            val isNewProduct = dish == null
            val downButtonTextId = getDownButtonTextId(dish, calculatorFlow)
            downButton.setText(resources.getString(downButtonTextId))

            val headerTextId = when {
                isNewProduct -> R.string.custom_products_new_product
                else -> R.string.custom_products_product
            }
            appTopBar.setTitle(resources.getString(headerTextId))
        }
    }

    override fun CreateCustomProductViewModel.init() {

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

        specialCarbohydrateField.setHint(resources.getString(R.string.custom_product_carbohydrates_hint))
        specialCarbohydrateField.setDescription(resources.getString(R.string.custom_product_carbohydrates_description))
        specialCarbohydrateField.setHeader(resources.getString(R.string.custom_product_count_carbohydrates))

        carbohydrateField.setHint(resources.getString(R.string.custom_product_extra_carbohydrate_hint))
        caloriesField.setHint(resources.getString(R.string.custom_product_extra_calories_hint))
        proteinField.setHint(resources.getString(R.string.custom_product_extra_protein))
        fatField.setHint(resources.getString(R.string.custom_product_extra_fat_hint))

        carbohydrateField.setDescription(resources.getString(R.string.custom_product_range_description))
        caloriesField.setDescription(resources.getString(R.string.custom_product_range_description))
        proteinField.setDescription(resources.getString(R.string.custom_product_range_description))
        fatField.setDescription(resources.getString(R.string.custom_product_range_description))

        carbohydrateField.showDescription(false)
        caloriesField.showDescription(false)
        proteinField.showDescription(false)
        fatField.showDescription(false)
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

private fun getDownButtonTextId(dish: DishUiEntity?, calculatorFlow: CalculatorFlow?): Int {
    return when {
        dish == null -> R.string.custom_products_save_and_add_to_list
        calculatorFlow == CalculatorFlow.BREAD_UNITS && !dish.servingSelect.isCarbohydrateValid() -> R.string.custom_products_add_and_save
        else -> R.string.custom_products_add
    }
}



private const val EXTRA_DISH = "extra_dish"
private const val EXTRA_PRODUCT_NAME = "extra_product_name"
