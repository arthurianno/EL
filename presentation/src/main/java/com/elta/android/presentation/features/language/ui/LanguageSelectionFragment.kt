package com.elta.android.presentation.features.language.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentLanguageSelectionBinding
import com.elta.android.presentation.features.language.model.AppLanguage
import com.elta.android.presentation.features.language.model.AppRegion
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

    private val isFirstLaunch: Boolean by lazy {
        arguments?.getBoolean(EXTRA_IS_FIRST_LAUNCH, false) == true
    }

    private val regions: List<AppRegion> by lazy {
        if (isFirstLaunch) AppRegion.firstLaunchRegions() else AppRegion.settingsRegions()
    }

    private var suppressSpinnerCallback = false
    private var ignoreInitialSpinnerSelection = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            Log.i(TAG, "onBackPressed: intercepted and suppressed (use X button or Continue)")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "LanguageSelectionFragment.onCreate(isFirstLaunch=$isFirstLaunch)")
        presentationModel.setFirstLaunch(isFirstLaunch)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(
                top = statusBars.top,
                bottom = if (isFirstLaunch) navBars.bottom else 0
            )
            Log.i(
                TAG,
                "WindowInsets: statusBarTop=${statusBars.top}, navBarBottom=${navBars.bottom}, isFirstLaunch=$isFirstLaunch, appliedBottom=${if (isFirstLaunch) navBars.bottom else 0}"
            )
            insets
        }
        Log.i(TAG, "LanguageSelectionFragment.onViewCreated(isFirstLaunch=$isFirstLaunch)")

        binding.greetingView.visibility = if (isFirstLaunch) View.VISIBLE else View.GONE
        binding.closeButtonView.visibility = if (isFirstLaunch) View.GONE else View.VISIBLE
        binding.languageSubtitleView.visibility = if (isFirstLaunch) View.GONE else View.VISIBLE

        binding.titleView.text = getString(
            if (isFirstLaunch) R.string.language_selection_title else R.string.language_settings_title
        )
        binding.continueButtonView.setText(
            if (isFirstLaunch) R.string.on_boarding_next_button_title else R.string.profile_settings_save_changes
        )
        binding.continueButtonView.visibility = View.VISIBLE

        setupRegionDropdown()
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "LanguageSelectionFragment.onResume(parentBackStackCount=${parentFragmentManager.backStackEntryCount})")
    }

    private fun setupRegionDropdown() {
        val displayNames = regions.map { getString(it.displayNameResId) }

        regions.forEachIndexed { index, region ->
            Log.i(TAG, "setupRegionDropdown: regions[$index]=${region.code}")
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.regionDropdownMenu.adapter = adapter
        binding.regionDropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressSpinnerCallback) {
                    Log.i(TAG, "regionSpinner: callback suppressed, position=$position")
                    return
                }

                if (ignoreInitialSpinnerSelection) {
                    ignoreInitialSpinnerSelection = false
                    Log.i(TAG, "regionSpinner: ignore initial auto-selection position=$position")
                    return
                }

                val selected = regions.getOrNull(position) ?: return
                Log.i(TAG, "regionSpinner: itemSelected position=$position, region=${selected.code}")
                presentationModel.selectRegionAction.consumer.accept(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    override fun onBindPresentationModel(pm: LanguageSelectionPm) {
        super.onBindPresentationModel(pm)

        binding.closeButtonView.clicks()
            .doOnNext { Log.i(TAG, "UI click: Close (X) button") }
            .bindTo(pm.closeAction)

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
            updateLanguageOption(binding.russianLanguageView, isRussianSelected)
            updateLanguageOption(binding.englishLanguageView, !isRussianSelected)
        }

        pm.selectedRegionState.bindTo { selectedRegion ->
            Log.i(TAG, "selectedRegionState=${selectedRegion.code}")

            val rawIndex = regions.indexOfFirst { it == selectedRegion }
            val index = rawIndex.coerceAtLeast(0)

            Log.i(TAG, "selectedRegionState: rawIndex=$rawIndex, appliedIndex=$index")

            suppressSpinnerCallback = true
            binding.regionDropdownMenu.setSelection(index, false)
            binding.regionDropdownMenu.post {
                suppressSpinnerCallback = false
                Log.i(TAG, "selectedRegionState: spinner callback re-enabled")
            }
        }

        pm.recreateActivityCommand.bindTo {
            Log.i(TAG, "recreateActivityCommand received, scheduling safe activity.recreate()")
            val hostActivity = activity
            if (hostActivity == null) {
                Log.w(TAG, "recreateActivityCommand: activity is null, skip")
                return@bindTo
            }
            hostActivity.window?.decorView?.post {
                hostActivity.window?.decorView?.post {
                    if (hostActivity.isFinishing || hostActivity.isDestroyed) {
                        Log.w(TAG, "recreateActivityCommand: activity is finishing/destroyed, skip")
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
            if (isSelected) R.drawable.bg_language_option_selected else R.drawable.bg_language_option_default
        )
        optionView.setTextColor(
            ContextCompat.getColor(optionView.context, if (isSelected) R.color.white else R.color.black)
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