package com.elta.android.presentation.di

import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elta.android.presentation.core.compose.viewmodel.ViewModelFactory
import com.elta.android.presentation.core.compose.viewmodel.ViewModelKey
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CreateCustomProductViewModel
import com.elta.android.presentation.features.calcutator.products.viewmodel.CalculatorViewModel
import com.elta.android.presentation.features.calcutator.custom.viewmodel.CustomProductsViewModel
import com.elta.android.presentation.features.calcutator.products.viewmodel.DishDetailViewModel
import com.elta.android.presentation.features.consultant.viewmodel.ConsultantViewModel
import com.elta.android.presentation.features.devices.search.viewmodel.GlucometerSearchViewModel
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.viewmodels.GlucoseSettingViewModel
import com.elta.android.presentation.features.profile.settings.glucoseformat.viewmodel.GlucoseFormatViewModel
import com.elta.android.presentation.features.profile.settings.reminders.all.viewmodels.RemindersViewModel
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectHelpViewModel
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectStartViewModel
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectTypeViewModel
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectingViewModel
import com.elta.android.presentation.features.sync.connect.viewmodel.HowToConnectViewModel
import com.elta.android.presentation.features.sync.connect.viewmodel.ScannerDmcViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.FlowPreview

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindsViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @OptIn(FlowPreview::class)
    @Binds
    @IntoMap
    @ViewModelKey(CalculatorViewModel::class)
    abstract fun bindCalculatorViewModel(viewModel: CalculatorViewModel): ViewModel

    @OptIn(FlowPreview::class)
    @Binds
    @IntoMap
    @ViewModelKey(CustomProductsViewModel::class)
    abstract fun bindCustomDishesViewModel(viewModel: CustomProductsViewModel): ViewModel

    @OptIn(FlowPreview::class)
    @Binds
    @IntoMap
    @ViewModelKey(CreateCustomProductViewModel::class)
    abstract fun bindCreateCustomDishViewModel(viewModel: CreateCustomProductViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DishDetailViewModel::class)
    abstract fun bindDishDetailViewModel(viewModel: DishDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GlucometerSearchViewModel::class)
    abstract fun bindGlucometerSearchViewModel(viewModel: GlucometerSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConnectTypeViewModel::class)
    abstract fun bindConnectTypeViewModel(viewModel: ConnectTypeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConnectHelpViewModel::class)
    abstract fun bindConnectHelpViewModel(viewModel: ConnectHelpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HowToConnectViewModel::class)
    @ExperimentalCameraProviderConfiguration
    abstract fun bindHowToConnectViewModel(viewModel: HowToConnectViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConnectingViewModel::class)
    abstract fun bindConnectingViewModel(viewModel: ConnectingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConnectStartViewModel::class)
    abstract fun bindConnectStartViewModel(viewModel: ConnectStartViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScannerDmcViewModel::class)
    abstract fun bindScannerDmcViewModel(viewModel: ScannerDmcViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConsultantViewModel::class)
    abstract fun bindConsultantViewModel(viewModel: ConsultantViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GlucoseFormatViewModel::class)
    abstract fun bindGlucoseFormatViewModel(viewModel: GlucoseFormatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RemindersViewModel::class)
    abstract fun bindRemindersViewModel(viewModel: RemindersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GlucoseSettingViewModel::class)
    abstract fun bindGlucoseSettingViewModel(viewModel: GlucoseSettingViewModel): ViewModel
}
