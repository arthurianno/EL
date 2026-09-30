package com.elta.android.presentation.features.language.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentLanguageSelectionBinding
import com.elta.android.presentation.features.language.model.AppLanguage
import com.elta.android.presentation.features.language.model.AppRegion
import com.elta.android.presentation.features.language.pm.LanguageSelectionPm
import com.elta.android.presentation.theme.EltaTheme
import com.elta.android.presentation.utils.bundle
import me.dmdev.rxpm.bindTo

private const val EXTRA_IS_FIRST_LAUNCH = "extra_is_first_launch"
private const val TAG = "LangFlow"

class LanguageSelectionFragment :
    BaseFragment<LanguageSelectionPm, FragmentLanguageSelectionBinding>(
        FragmentLanguageSelectionBinding::inflate
    ) {

    override val screenLayout: Int = R.layout.fragment_language_selection
    override val classToken: Class<LanguageSelectionPm> = LanguageSelectionPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider
    override val applyPlatformSystemWindowFitting: Boolean = false
    override val applyBottomSystemInsets: Boolean = false

    private val isFirstLaunch: Boolean by lazy {
        arguments?.getBoolean(EXTRA_IS_FIRST_LAUNCH, false) == true
    }

    private val regions: List<AppRegion> by lazy {
        if (isFirstLaunch) AppRegion.firstLaunchRegions() else AppRegion.settingsRegions()
    }

    private var selectedLanguage by mutableStateOf(AppLanguage.RU)
    private var selectedRegion by mutableStateOf(AppRegion.RUSSIA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationModel.setFirstLaunch(isFirstLaunch)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.root.setContent {
            EltaTheme {
                LocaleSelectionScreen(
                    language = selectedLanguage,
                    region = selectedRegion,
                    regions = regions,
                    isFirstLaunch = isFirstLaunch,
                    onLanguageSelected = { presentationModel.selectLanguageAction.consumer.accept(it) },
                    onRegionSelected = { presentationModel.selectRegionAction.consumer.accept(it) },
                    onComplete = { presentationModel.continueAction.consumer.accept(Unit) },
                    onExit = { presentationModel.closeAction.consumer.accept(Unit) }
                )
            }
        }
    }

    override fun onBindPresentationModel(pm: LanguageSelectionPm) {
        super.onBindPresentationModel(pm)

        pm.selectedLanguageState.bindTo { selectedLanguage = it }
        pm.selectedRegionState.bindTo { selectedRegion = it }

        pm.recreateActivityCommand.bindTo {
            val hostActivity = activity ?: return@bindTo
            hostActivity.window?.decorView?.post {
                hostActivity.window?.decorView?.post {
                    if (!hostActivity.isFinishing && !hostActivity.isDestroyed) {
                        Log.i(TAG, "activity.recreate() called")
                        hostActivity.recreate()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(isFirstLaunch: Boolean): LanguageSelectionFragment {
            return LanguageSelectionFragment().apply {
                arguments = bundle(EXTRA_IS_FIRST_LAUNCH to isFirstLaunch)
            }
        }
    }
}
