package com.elta.android.presentation.features.calcutator.custom.viewmodel

import com.elta.android.domain.common.mapDistinct
import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.AddProductUseCase
import com.elta.android.domain.features.calculator.interactor.GetServingsProductUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseWidgetModel
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.DropdownFieldWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.IconOutlinedTextFieldWidgetModel
import com.elta.android.presentation.core.compose.widgets.textfields.InputTextFieldWidgetModel
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductAction
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductViewState
import com.elta.android.presentation.features.calcutator.custom.model.ProductUiEntity
import com.elta.android.presentation.features.calcutator.mappers.CONVERSION_FACTOR
import com.elta.android.presentation.features.calcutator.mappers.calculateBreadUnits
import com.elta.android.presentation.features.calcutator.mappers.format
import com.elta.android.presentation.features.calcutator.mappers.isValid
import com.elta.android.presentation.features.calcutator.mappers.notNullOrZero
import com.elta.android.presentation.features.calcutator.mappers.toDish
import com.elta.android.presentation.features.calcutator.mappers.toDomain
import com.elta.android.presentation.features.calcutator.mappers.toProduct
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.viewmodel.ZERO_COUNT
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.COMMA_CHAR
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.DOT_CHAR
import com.elta.android.presentation.utils.BREAD_UNIT_COUNT_INTEGER_PART
import com.elta.android.presentation.utils.BREAD_UNIT_INTEGER_LENGTH_REGEX
import com.elta.android.presentation.utils.BREAD_UNIT_VALUE_REGEX
import com.elta.android.presentation.utils.PORTION_COUNT_INTEGER_PART
import com.elta.android.presentation.utils.PORTION_INTEGER_LENGTH_REGEX
import com.elta.android.presentation.utils.PORTION_VALUE_REGEX
import com.elta.android.presentation.utils.createTextFilterForDoubleValue
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import kotlin.math.roundToInt


class CreateCustomProductViewModel @Inject constructor(
    private val getServingsProductUseCase: GetServingsProductUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler
) :
    BaseViewModel<CreateCustomProductViewState>() {

    val appTopBar = BaseAppTopBarWidgetModel()
    val downButton = DownButtonWidgetModel()

    val productNameField = InputTextFieldWidgetModel()

    val portionCountTextField = IconOutlinedTextFieldWidgetModel()
    val portionDescriptionTextField = DropdownFieldWidgetModel()

    val breadUnitsField = InputTextFieldWidgetModel()

    val warningExitDialog = BaseDialogWidgetModel<Unit>(
        positiveOnCLick = {
            router.exit()
        }
    )
    val errorDialog = BaseDialogWidgetModel<Unit>()

    override fun createInitState(): CreateCustomProductViewState {
        return CreateCustomProductViewState(
            isLoading = true,
            isError = false,
            servings = null,
            product = ProductUiEntity(
                breadUnits = 1.0
            ),
            dish = null,
            isShowCountHelpSnack = false,
        )
    }


    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBar,
        downButton,

        productNameField,

        portionCountTextField,
        portionDescriptionTextField,

        breadUnitsField,
    ).actionObserve()

    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> warningExit()
            DownButtonClick -> downButtonClick()
            CreateCustomProductAction.Retry -> loadServings()

        }
    }

    override fun reduceStateByAction(
        currentState: CreateCustomProductViewState,
        action: Action
    ): CreateCustomProductViewState {
        return when (action) {
            CreateCustomProductAction.PortionHelpClick -> showPortionHelp(!state.value.isShowCountHelpSnack)
            AppAction.FreeScreenTap -> showPortionHelp(false)
            else -> super.reduceStateByAction(currentState, action)
        }
    }

    init {

        launch {
            productNameField
                .state
                .mapDistinct { it.textField.text }
                .collect {
                    reduceState {
                        state.value.copy(
                            product = state.value.product.copy(
                                name = it
                            )
                        )
                    }
                }
        }

        launch {
            portionCountTextField
                .state
                .mapDistinct { it.textField.text.replace(COMMA_CHAR, DOT_CHAR) }
                .map { it.toDoubleOrNull() }
                .collect {
                    reduceState {
                        state.value.copy(
                            product = state.value.product.copy(
                                numberOfUnits = it
                            )
                        )
                    }
                }
        }

        launch {
            portionDescriptionTextField
                .state
                .mapDistinct { it.text }
                .collect {

                    val serving = state.value.servings?.firstOrNull { metricServingLink ->
                        metricServingLink.name == it
                    }

                    if (serving != null) {
                        reduceState {
                            state.value.copy(
                                product = state.value.product.copy(
                                    metricServingLink = serving
                                )
                            )
                        }

                    }
                }
        }

        launch {
            breadUnitsField
                .state
                .mapDistinct { it.textField.text.replace(COMMA_CHAR, DOT_CHAR) }
                .map { it.takeIf { it.isNotBlank() } }
                .map { it?.toDoubleOrNull() }
                .collect {
                    reduceState {
                        state.value.copy(
                            product = state.value.product.copy(
                                breadUnits = it,
                                carbohydrate = ((it ?: ZERO_COUNT) * CONVERSION_FACTOR).roundToInt()
                            )
                        )
                    }
                }
        }

        launch {
            state
                .mapDistinct { it.product }
                .filter { state.value.dish == null }
                .collectLatest { product ->
                    product.validateField()
                    downButton.setEnableState(product.isValid())
                }
        }

        initTextFilter()
    }

    private fun loadServings() {
        launch {
            getServingsProductUseCase()
                .catch {
                    handleError(it)
                    reduceState {
                        state.value.copy(isLoading = false, isError = true)
                    }
                }
                .onStart {
                    reduceState {
                        state.value.copy(isLoading = true, isError = false)
                    }
                }
                .onCompletion {
                    reduceState {
                        state.value.copy(isLoading = false)
                    }
                }
                .collect {
                    val selectedElement = it.firstOrNull()
                    val dropdownList = it.map { metricServingLink -> metricServingLink.name }

                    portionDescriptionTextField.setDropDownList(dropdownList)
                    portionDescriptionTextField.setText(selectedElement?.name)
                    breadUnitsField.setText(DEFAULT_BREAD_UNIT)

                    reduceState {
                        state.value.copy(
                            servings = it,
                            product = state.value.product.copy(
                                metricServingLink = selectedElement,
                                breadUnits = DEFAULT_BREAD_UNIT.toDoubleOrNull()
                            ),
                            isLoading = false,
                            isError = false
                        )
                    }
                }
        }
    }

    private fun ProductUiEntity.validateField() {
        portionCountTextField.setError(numberOfUnits == 0.0)
        breadUnitsField.setError(breadUnits == 0.0)
    }

    private fun showPortionHelp(visibilityState: Boolean): CreateCustomProductViewState = run {
        portionCountTextField.setError(visibilityState)
        portionDescriptionTextField.setError(visibilityState)
        state.value.copy(isShowCountHelpSnack = visibilityState)
    }

    private fun warningExit() {
        if (state.value.dish == null && state.value.product.productHasChanged()) {
            warningExitDialog.dialogOpen()
        } else {
            router.exit()
        }
    }

    private fun ProductUiEntity.productHasChanged(): Boolean = name?.isNotBlank() == true ||
            numberOfUnits.notNullOrZero() ||
            (breadUnits.notNullOrZero() && breadUnits?.format() != DEFAULT_BREAD_UNIT)

    private fun downButtonClick() {
        launch {
            val dish = state.value.dish
            if (dish != null) {
                router.replaceScreen(Screens.AddDishScreen(dish))
            } else {
                val product = state.value.product.toProduct()


                addProductUseCase(product)
                    .catch {
                        handleError(it)
                        setWidgetEnabled(true)
                        errorDialog.dialogOpen()
                        downButton.setLoading(false)
                    }
                    .onStart {
                        setWidgetEnabled(false)
                        downButton.setLoading(true)
                    }
                    .collect {
                        setWidgetEnabled(true)
                        downButton.setLoading(false)

                        val addedDish = product.toDish().toDomain()
                        addDishFragmentResult.onNext(addedDish)
                            .catch { error ->
                                handleError(error)
                                setWidgetEnabled(true)
                                errorDialog.dialogOpen()
                                downButton.setLoading(false)
                            }
                            .collect { router.backTo(Screens.CalculatorScreen) }
                    }
            }
        }
    }

    private fun setWidgetEnabled(isEnabled: Boolean) {
        downButton.setIgnoreClick(!isEnabled)

        productNameField.setEnabled(isEnabled)

        portionCountTextField.setEnabled(isEnabled)
        portionDescriptionTextField.setEnabled(isEnabled)

        breadUnitsField.setEnabled(isEnabled)
    }

    fun setDish(dish: DishUiEntity?) {

        if (dish != null) {
            val serving = dish.servings.firstOrNull()

            productNameField.setText(dish.name)

            serving?.let {
                portionCountTextField.setText(serving.numberOfUnits)
                portionDescriptionTextField.setText(serving.nameMetricServing)

                val xe = serving.carbohydrate.toDoubleOrNull()?.let { calculateBreadUnits(it) }
                    ?.toString().orEmpty()

                breadUnitsField.setText(xe)
            }

            reduceState {
                state.value.copy(
                    dish = dish,
                    isLoading = false,
                    isError = false
                )
            }

        } else {
            loadServings()
        }

    }

    fun setProductName(searchText: String) {
        productNameField.setText(searchText)
    }

    fun setEditableProduct(editableProduct: Boolean) {
        downButton.setEnableState(!editableProduct)
        productNameField.setEnabled(editableProduct)

        portionCountTextField.setEnabled(editableProduct)

        portionDescriptionTextField.setEnabled(editableProduct)

        breadUnitsField.setEnabled(editableProduct)
    }

    private fun initTextFilter() {
        portionCountTextField.textFilter = createTextFilterForDoubleValue(
            PORTION_INTEGER_LENGTH_REGEX,
            PORTION_VALUE_REGEX,
            PORTION_COUNT_INTEGER_PART
        )
        breadUnitsField.textFilter = createTextFilterForDoubleValue(
            BREAD_UNIT_INTEGER_LENGTH_REGEX,
            BREAD_UNIT_VALUE_REGEX,
            BREAD_UNIT_COUNT_INTEGER_PART
        )
    }
}


private const val DEFAULT_BREAD_UNIT = "1"


