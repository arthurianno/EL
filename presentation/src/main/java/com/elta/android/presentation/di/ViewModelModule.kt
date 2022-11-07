package com.elta.android.presentation.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elta.android.presentation.core.compose.viewmodel.ViewModelFactory
import com.elta.android.presentation.core.compose.viewmodel.ViewModelKey
import com.elta.android.presentation.features.calcutator.viewmodel.AddDishViewModel
import com.elta.android.presentation.features.calcutator.viewmodel.CalculatorViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindsViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(CalculatorViewModel::class)
    abstract fun bindCalculatorViewModel(calculatorViewModel: CalculatorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddDishViewModel::class)
    abstract fun bindDishViewModel(addDishViewModel: AddDishViewModel): ViewModel
}
