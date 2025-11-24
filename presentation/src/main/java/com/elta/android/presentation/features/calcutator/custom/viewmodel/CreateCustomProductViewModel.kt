package com.elta.android.presentation.features.calcutator.custom.viewmodel

import com.elta.android.domain.common.mapDistinct
import com.elta.android.domain.features.calculator.interactor.AddDishFragmentResultHandler
import com.elta.android.domain.features.calculator.interactor.AddProductUseCase
import com.elta.android.domain.features.calculator.interactor.GetServingsProductUseCase
import com.elta.android.domain.features.calculator.interactor.ReplaceProductUseCase
import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
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
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductFlow
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductFlow.Companion.isCreating
import com.elta.android.presentation.features.calcutator.custom.model.CreateCustomProductViewState
import com.elta.android.presentation.features.calcutator.custom.model.ProductUiEntity
import com.elta.android.presentation.features.calcutator.mappers.CARBOHYDRATE_DEFAULT
import com.elta.android.presentation.features.calcutator.mappers.ZERO_COUNT_DOUBLE
import com.elta.android.presentation.features.calcutator.mappers.ZERO_COUNT_INT
import com.elta.android.presentation.features.calcutator.mappers.getCreateCustomProductFlow
import com.elta.android.presentation.features.calcutator.mappers.isCarbohydrateValid
import com.elta.android.presentation.features.calcutator.mappers.isValid
import com.elta.android.presentation.features.calcutator.mappers.notNullOrZero
import com.elta.android.presentation.features.calcutator.mappers.productHasChanged
import com.elta.android.presentation.features.calcutator.mappers.toDish
import com.elta.android.presentation.features.calcutator.mappers.toProduct
import com.elta.android.presentation.features.calcutator.mappers.toUi
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.model.ServingUiEntity
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.COMMA_CHAR
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.DOT_CHAR
import com.elta.android.presentation.utils.ENERGY_COUNT_INTEGER_PART
import com.elta.android.presentation.utils.ENERGY_INTEGER_LENGTH_REGEX
import com.elta.android.presentation.utils.ENERGY_VALUE_REGEX
import com.elta.android.presentation.utils.PORTION_COUNT_INTEGER_PART
import com.elta.android.presentation.utils.PORTION_INTEGER_LENGTH_REGEX
import com.elta.android.presentation.utils.PORTION_VALUE_REGEX
import com.elta.android.presentation.utils.createTextFilterForDoubleValue
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class CreateCustomProductViewModel @Inject constructor(
    private val getServingsProductUseCase: GetServingsProductUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val replaceProductUseCase: ReplaceProductUseCase,
    private val addDishFragmentResult: AddDishFragmentResultHandler,
    private val appMetricTracker: AppMetricTracker
) : BaseViewModel<CreateCustomProductViewState>() {
    override fun createInitState(): CreateCustomProductViewState = CreateCustomProductViewState(
        createCustomProductFlow = CreateCustomProductFlow.CREATING,
        isLoading = true,
        isError = false,
        servings = null,
        product = ProductUiEntity(),
        dish = null,
        isShowServingCountHelpSnack = false,
        isShowCarbohydrateCountHelpSnack = false,
        calculatorFlow = CalculatorFlow.PRODUCT_ONLY
    )

    val appTopBar = BaseAppTopBarWidgetModel()

    val downButton = DownButtonWidgetModel()
    val productNameField = InputTextFieldWidgetModel()
    val portionCountTextField = IconOutlinedTextFieldWidgetModel()
    val portionDescriptionTextField = DropdownFieldWidgetModel()

    val specialCarbohydrateField = InputTextFieldWidgetModel()
    val caloriesField = InputTextFieldWidgetModel()
    val proteinField = InputTextFieldWidgetModel()
    val fatField = InputTextFieldWidgetModel()
    val carbohydrateField = InputTextFieldWidgetModel()
    val warningExitDialog = BaseDialogWidgetModel<Unit>(positiveOnCLick = { router.exit() })
    val errorDialog = BaseDialogWidgetModel<Unit>()

    override val widgets: List<BaseWidgetModel<*>> = listOf(
        appTopBar,
        downButton,
        productNameField,
        portionCountTextField,
        portionDescriptionTextField,
        specialCarbohydrateField,
        caloriesField,
        proteinField,
        fatField,
        carbohydrateField
    ).actionObserve()

    init {
        appMetricTracker.trackEvent(AppMetricEvent.ViewScreenFoodNew)
        collectPortionField()
        collectInputField()
        collectExtraBlock()
        initTextFilter()

        launch {
            state
                .mapDistinct { it.product }
                .collectLatest { product ->
                    when (state.value.createCustomProductFlow) {
                        CreateCustomProductFlow.EDITING -> {
                            portionCountTextField.setError(!product.numberOfUnits.notNullOrZero())
                            specialCarbohydrateField.setError(!product.carbohydrate.notNullOrZero())
                        }
                        CreateCustomProductFlow.CREATING -> {
                            portionCountTextField.setError(product.numberOfUnits == ZERO_COUNT_DOUBLE)
                            specialCarbohydrateField.setError(product.carbohydrate == ZERO_COUNT_INT)
                        }
                        CreateCustomProductFlow.VIEWING -> {}
                    }
                    downButton.setEnableState(product.isValid(state.value.calculatorFlow))
                }
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> warningExit()
            DownButtonClick -> downButtonClick()
            CreateCustomProductAction.Retry -> launch { loadServings() }
        }
    }

    override fun reduceStateByAction(
        currentState: CreateCustomProductViewState, action: Action
    ): CreateCustomProductViewState {
        return when (action) {
            CreateCustomProductAction.PortionHelpClick -> showPortionHelp(!state.value.isShowServingCountHelpSnack)
            AppAction.FreeScreenTap -> showPortionHelp(false)
            else -> super.reduceStateByAction(currentState, action)
        }
    }

    fun setParams(dish: DishUiEntity?, flow: CalculatorFlow?) {
        val calculatorFlow = flow ?: CalculatorFlow.BREAD_UNITS
        launch {
            reduceState {
                state.value.copy(
                    calculatorFlow = calculatorFlow
                )
            }
            if (dish != null) setupDish(dish, calculatorFlow)
            else
                loadServings()
            configureProductField()
        }
    }

    private suspend fun setupDish(dish: DishUiEntity, calculatorFlow: CalculatorFlow) {
        getServingsProductUseCase()
            .collect { servings ->
                reduceState {
                    state.value.copy(
                        dish = dish,
                        servings = servings,
                        isShowCarbohydrateCountHelpSnack = !dish.servingSelect.isCarbohydrateValid() &&
                                calculatorFlow == CalculatorFlow.BREAD_UNITS,
                        product = state.value.product.copy(
                            carbohydrate = dish.servingSelect.carbohydrate?.toIntOrNull() ?: 0,
                            numberOfUnits = dish.servingSelect.numberOfUnits.toDoubleOrNull()
                                ?: 0.0,
                            metricServingLink = MetricServingLink(
                                dish.servingSelect.idMetricServing,
                                dish.servingSelect.nameMetricServing
                            )
                        ),
                        isLoading = false,
                        isError = false,
                        createCustomProductFlow = getCreateCustomProductFlow(calculatorFlow, dish)
                    )
                }
                setupFields(calculatorFlow)
            }
    }

    private fun setupFields(calculatorFlow: CalculatorFlow) {
        val state = state.value
        productNameField.setText(state.dish?.name.orEmpty())
        portionDescriptionTextField.setDropDownList(state.servings?.map { it.name })

        if (calculatorFlow == CalculatorFlow.BREAD_UNITS) {
            specialCarbohydrateField.setText(CARBOHYDRATE_DEFAULT.toString())
            specialCarbohydrateField.setError(state.isShowCarbohydrateCountHelpSnack)
        }

        state.dish?.let { initServing(it.servingSelect) }
    }

    private fun initServing(serving: ServingUiEntity) {
        specialCarbohydrateField.setEnabled(!serving.isCarbohydrateValid())
        specialCarbohydrateField.setText(serving.carbohydrate.orEmpty())
        carbohydrateField.setText(serving.carbohydrate.orEmpty())

        portionCountTextField.setText(serving.numberOfUnits)
        portionDescriptionTextField.setText(serving.nameMetricServing)
    }

    private fun configureProductField() {
        val isCreateProduct = state.value.createCustomProductFlow.isCreating()
        downButton.setEnableState(!isCreateProduct)
        productNameField.setEnabled(isCreateProduct)
        portionCountTextField.setEnabled(isCreateProduct)
        portionDescriptionTextField.setEnabled(isCreateProduct)
    }

    private fun collectInputField() {

        launch {
            productNameField.state.mapDistinct { it.textField.text }.collect {
                reduceState {
                    state.value.copy(
                        product = state.value.product.copy(name = it)
                    )
                }
            }
        }

        launch {
            specialCarbohydrateField.state.map { it.textField.text.toIntOrNull() }
                .collect { carbohydrate ->
                    if (state.value.calculatorFlow == CalculatorFlow.BREAD_UNITS) {
                        reduceState {
                            state.value.copy(
                                product = state.value.product.copy(carbohydrate = carbohydrate)
                            )
                        }
                    }
                }
        }

        launch {
            caloriesField.state.mapDistinct { it.textField.text }
                .map { it.toIntOrNull() }.collect {
                    reduceState {
                        state.value.copy(
                            product = state.value.product.copy(
                                calories = it,
                            )
                        )
                    }
                }
        }

        launch {
            proteinField.state.mapDistinct { it.textField.text }
                .map { it.toIntOrNull() }.collect {
                    reduceState {
                        state.value.copy(
                            product = state.value.product.copy(protein = it)
                        )
                    }
                }
        }

        launch {
            fatField.state.mapDistinct { it.textField.text }
                .map { it.toIntOrNull() }.collect {
                    reduceState {
                        state.value.copy(
                            product = state.value.product.copy(fat = it)
                        )
                    }
                }
        }

        launch {
            carbohydrateField.state.mapDistinct { it.textField.text }
                .map { it.toIntOrNull() }.collect {
                    if (state.value.calculatorFlow == CalculatorFlow.PRODUCT_ONLY) {
                        reduceState {
                            state.value.copy(product = state.value.product.copy(carbohydrate = it))
                        }
                    }
                }
        }
    }

    private fun collectPortionField() {
        launch {
            portionCountTextField.state.mapDistinct {
                it.textField.text.replace(
                    COMMA_CHAR,
                    DOT_CHAR
                )
            }.map { it.toDoubleOrNull() }.collect {
                reduceState {
                    state.value.copy(
                        product = state.value.product.copy(numberOfUnits = it)
                    )
                }
            }
        }

        launch {
            portionDescriptionTextField.state.mapDistinct { it.text }.collect {
                val serving = state.value.servings?.firstOrNull { metricServingLink ->
                    metricServingLink.name == it
                }

                if (serving != null) {
                    reduceState {
                        state.value.copy(
                            product = state.value.product.copy(metricServingLink = serving)
                        )
                    }

                }
            }
        }
    }

    private fun collectExtraBlock() {
        launch {
            carbohydrateField.state.mapDistinct { it.isFocused }
                .collect { isFocused -> carbohydrateField.showDescription(isFocused) }
        }
        launch {
            caloriesField.state.mapDistinct { it.isFocused }
                .collect { isFocused -> caloriesField.showDescription(isFocused) }
        }
        launch {
            proteinField.state.mapDistinct { it.isFocused }
                .collect { isFocused -> proteinField.showDescription(isFocused) }
        }
        launch {
            fatField.state.mapDistinct { it.isFocused }
                .collect { isFocused -> fatField.showDescription(isFocused) }
        }
    }

    private suspend fun loadServings() {

        getServingsProductUseCase()
            .catch {
                handleError(it)
                reduceState { state.value.copy(isLoading = false, isError = true) }
                downButton.visibilityState(false)
            }.onStart {
                reduceState { state.value.copy(isLoading = true, isError = false) }
                downButton.visibilityState(false)
            }.onCompletion {
                reduceState { state.value.copy(isLoading = false) }
                val state = state.value
                downButton.visibilityState(!(state.isLoading || state.isError))
            }.collect { servings ->
                val selectedElement = servings.firstOrNull()
                val dropdownList = servings.map { it.name }

                specialCarbohydrateField.setEnabled(true)

                portionDescriptionTextField.setDropDownList(dropdownList)
                portionDescriptionTextField.setText(selectedElement?.name)

                reduceState {
                    state.value.copy(
                        servings = servings,
                        product = state.value.product.copy(
                            metricServingLink = selectedElement,
                        ),
                        isError = false,
                        createCustomProductFlow = CreateCustomProductFlow.CREATING
                    )
                }
            }
    }

    private fun showPortionHelp(visibilityState: Boolean): CreateCustomProductViewState = run {
        portionCountTextField.setError(visibilityState)
        portionDescriptionTextField.setError(visibilityState)
        state.value.copy(
            isShowServingCountHelpSnack = visibilityState, isShowCarbohydrateCountHelpSnack = false
        )
    }

    private fun warningExit() {
        val state = state.value
        val firstMetricServingLink = state.servings?.firstOrNull()?.name
        if (state.dish == null && state.product.productHasChanged(firstMetricServingLink)) {
            warningExitDialog.dialogOpen()
        } else {
            router.exit()
        }
    }

    private fun downButtonClick() {
        appMetricTracker.trackEvent(AppMetricEvent.TapButtonFoodNewSave)
        launch {
            val dish = state.value.dish
            val product = state.value.product.toProduct(dish)

            when {
                state.value.createCustomProductFlow == CreateCustomProductFlow.CREATING -> {
                    addProductUseCase(product).catch {
                        handleError(it)
                        setWidgetEnabled(true)
                        errorDialog.dialogOpen()
                        downButton.setLoading(false)
                    }.onStart {
                        setWidgetEnabled(false)
                        downButton.setLoading(true)
                    }.collect { _ ->
                        setWidgetEnabled(true)
                        downButton.setLoading(false)
                        addDishFragmentResult
                            .onNext(product.toDish())
                            .catch { error ->
                                handleError(error)
                                setWidgetEnabled(true)
                                errorDialog.dialogOpen()
                                downButton.setLoading(false)
                            }
                            .collect { router.backTo(Screens.CalculatorScreen(state.value.calculatorFlow)) }
                    }

                }

                state.value.createCustomProductFlow == CreateCustomProductFlow.EDITING && dish != null -> {
                    replaceProductUseCase(product)
                        .catch {
                            handleError(it)
                            setWidgetEnabled(true)
                            errorDialog.dialogOpen()
                            downButton.setLoading(false)
                        }.onStart {
                            setWidgetEnabled(false)
                            downButton.setLoading(true)
                        }.collect { addedDish ->
                            setWidgetEnabled(true)
                            downButton.setLoading(false)
                            router.replaceScreen(
                                Screens.AddDishScreen(
                                    addedDish.toUi(),
                                    state.value.calculatorFlow
                                )
                            )
                        }
                }

                state.value.createCustomProductFlow == CreateCustomProductFlow.VIEWING && dish != null -> {
                    router.replaceScreen(Screens.AddDishScreen(dish, state.value.calculatorFlow))
                }
            }
        }
    }

    private fun setWidgetEnabled(isEnabled: Boolean) {
        downButton.setIgnoreClick(!isEnabled)
        productNameField.setEnabled(isEnabled)
        portionCountTextField.setEnabled(isEnabled)
        portionDescriptionTextField.setEnabled(isEnabled)
        specialCarbohydrateField.setEnabled(isEnabled)
    }

    private fun initTextFilter() {
        val energyInfoFilters = createTextFilterForDoubleValue(
            ENERGY_INTEGER_LENGTH_REGEX, ENERGY_VALUE_REGEX, ENERGY_COUNT_INTEGER_PART
        )

        specialCarbohydrateField.textFilter = energyInfoFilters
        carbohydrateField.textFilter = energyInfoFilters
        caloriesField.textFilter = energyInfoFilters
        proteinField.textFilter = energyInfoFilters
        fatField.textFilter = energyInfoFilters

        portionCountTextField.textFilter = createTextFilterForDoubleValue(
            PORTION_INTEGER_LENGTH_REGEX, PORTION_VALUE_REGEX, PORTION_COUNT_INTEGER_PART
        )
    }
}
