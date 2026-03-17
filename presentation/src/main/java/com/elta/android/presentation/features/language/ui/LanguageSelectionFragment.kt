package com.elta.android.presentation.features.language.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentLanguageSelectionBinding
import com.elta.android.presentation.features.language.model.AppLanguage
import com.elta.android.presentation.features.language.pm.LanguageSelectionPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            val isFirstLaunch = arguments?.getBoolean(EXTRA_IS_FIRST_LAUNCH, false) == true
            val parentBackStackCount = parentFragmentManager.backStackEntryCount
            Log.i(
                TAG,
                "onBackPressed(firstLaunch=$isFirstLaunch, parentBackStackCount=$parentBackStackCount)"
            )

            if (isFirstLaunch) {
                Log.i(TAG, "onBackPressed: first launch mode -> router.exit()")
                router.exit()
                return@addOnBackPressedCallback
            }

            if (parentBackStackCount > 0) {
                Log.i(TAG, "onBackPressed: settings mode with back stack -> router.exit()")
                router.exit()
            } else {
                Log.w(
                    TAG,
                    "onBackPressed: settings mode but empty back stack, fallback to ProfileSettings root"
                )
                router.newRootScreen(Screens.ProfileSettings)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isFirstLaunch = arguments?.getBoolean(EXTRA_IS_FIRST_LAUNCH, false) == true
        Log.i(TAG, "LanguageSelectionFragment.onCreate(isFirstLaunch=$isFirstLaunch)")
        presentationModel.setFirstLaunch(isFirstLaunch)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isFirstLaunch = arguments?.getBoolean(EXTRA_IS_FIRST_LAUNCH, false) == true
        Log.i(TAG, "LanguageSelectionFragment.onViewCreated(isFirstLaunch=$isFirstLaunch)")
        binding.greetingView.visibility = if (isFirstLaunch) View.VISIBLE else View.GONE
        binding.titleView.text = getString(
            if (isFirstLaunch) {
                R.string.language_selection_title
            } else {
                R.string.language_settings_title
            }
        )
        binding.continueButtonView.setText(
            if (isFirstLaunch) {
                R.string.on_boarding_next_button_title
            } else {
                R.string.profile_settings_save_changes
            }
        )
        binding.continueButtonView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        Log.i(
            TAG,
            "LanguageSelectionFragment.onResume(parentBackStackCount=${parentFragmentManager.backStackEntryCount})"
        )
    }

    override fun onBindPresentationModel(pm: LanguageSelectionPm) {
        super.onBindPresentationModel(pm)
        binding.russianLanguageView.clicks()
            .doOnNext { Log.i(TAG, "UI click: Russian language") }
            .map { AppLanguage.RU }
            .bindTo(pm.selectLanguageAction)
        binding.englishLanguageView.clicks()
            .doOnNext { Log.i(TAG, "UI click: English language") }
            .map { AppLanguage.EN }
            .bindTo(pm.selectLanguageAction)
        binding.continueButtonView.clicks()
            .doOnNext { Log.i(TAG, "UI click: Save/Continue") }
            .bindTo(pm.continueAction)

        pm.selectedLanguageState.bindTo { selectedLanguage ->
            Log.i(TAG, "selectedLanguageState=${selectedLanguage.code}")
            val isRussianSelected = selectedLanguage == AppLanguage.RU
            updateLanguageOption(
                view = binding.russianLanguageView,
                isSelected = isRussianSelected
            )
            updateLanguageOption(
                view = binding.englishLanguageView,
                isSelected = !isRussianSelected
            )
        }
        pm.showContinueButtonState.bindTo { showButton ->
            Log.i(TAG, "showContinueButtonState=$showButton")
            binding.continueButtonView.visibility = if (showButton) View.VISIBLE else View.GONE
        }
        pm.recreateActivityCommand.bindTo {
            Log.i(TAG, "recreateActivityCommand received, scheduling safe activity.recreate()")
            val hostActivity = activity
            if (hostActivity == null) {
                Log.w(TAG, "recreateActivityCommand: activity is null, skip")
                return@bindTo
            }

            // Run after next UI frame(s) to avoid lifecycle races on some vendor ROMs.
            hostActivity.window?.decorView?.post {
                hostActivity.window?.decorView?.post {
                    if (hostActivity.isFinishing || hostActivity.isDestroyed) {
                        Log.w(
                            TAG,
                            "recreateActivityCommand: activity is finishing=${hostActivity.isFinishing}, destroyed=${hostActivity.isDestroyed}, skip"
                        )
                        return@post
                    }
                    Log.i(TAG, "activity.recreate() called")
                    hostActivity.recreate()
                }
            }
        }
    }

    private fun updateLanguageOption(view: View, isSelected: Boolean) {
        val optionView = view as? androidx.appcompat.widget.AppCompatTextView ?: return
        Log.i(
            TAG,
            "updateLanguageOption(viewId=${optionView.resources.getResourceEntryName(optionView.id)}, isSelected=$isSelected)"
        )
        optionView.setBackgroundResource(
            if (isSelected) {
                R.drawable.bg_language_option_selected
            } else {
                R.drawable.bg_language_option_default
            }
        )
        optionView.setTextColor(
            ContextCompat.getColor(
                optionView.context,
                if (isSelected) R.color.white else R.color.black
            )
        )
    }

    companion object {
        fun newInstance(isFirstLaunch: Boolean): LanguageSelectionFragment {
            return LanguageSelectionFragment().apply {
                arguments = bundle(EXTRA_IS_FIRST_LAUNCH to isFirstLaunch)
            }
        }
    }
}
