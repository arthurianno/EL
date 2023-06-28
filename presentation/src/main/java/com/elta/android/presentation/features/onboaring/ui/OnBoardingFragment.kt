package com.elta.android.presentation.features.onboaring.ui

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseRecyclerViewFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentOnboardingBinding
import com.elta.android.presentation.features.onboaring.pm.OnBoardingPm
import com.elta.android.presentation.features.onboaring.ui.adapter.OnBoardingAdapter
import com.elta.android.presentation.utils.animateText
import com.elta.android.presentation.utils.fadeVisibility
import com.elta.android.presentation.utils.pageScrolled
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.ui.extensions.hide
import javax.inject.Inject
import me.dmdev.rxpm.bindTo

private const val DISABLE_DELAY = 300L

@Suppress("LabeledExpression")
class OnBoardingFragment :
    BaseRecyclerViewFragment<OnBoardingPm, FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {

    @Inject
    lateinit var onBoardingAdapter: OnBoardingAdapter

    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { onBoardingAdapter }
    override val screenLayout: Int = R.layout.fragment_onboarding
    override val classToken: Class<OnBoardingPm> = OnBoardingPm::class.java

    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider
    private val snapHelper = PagerSnapHelper()

    private var lastX: Float = 0F

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.hide()
            menuButtonView.text = getString(R.string.on_boarding_toolbar_menu_button)
        }
        itemsView?.let {
            snapHelper.attachToRecyclerView(it)
            binding.indicatorsView.attachToRecyclerView(it)
            it.setOnTouchListener { _, event ->
                val action = event.action
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = event.x
                }
                return@setOnTouchListener event.x != lastX
            }
        }
    }

    override fun provideLayoutManager(): RecyclerView.LayoutManager =
        FixedLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL)

    override fun onBindPresentationModel(pm: OnBoardingPm) {
        super.onBindPresentationModel(pm)
        pm.currentPageState.bindTo { page -> itemsView?.smoothScrollToPosition(page) }
        pm.titleState.observable.skip(1)
            .subscribe { binding.onBoardingHeaderTextView.animateText(it) }
        pm.titleState.observable.take(1).subscribe(binding.onBoardingHeaderTextView.text())
        pm.toolbarMenuButtonIsVisibleState.observable.subscribe{ binding.toolbar.menuButtonView.isVisible = it }
        pm.previousPageVisibilityState.bindTo(binding.previewPageButtonView.fadeVisibility())
        pm.nextPageVisibilityState.bindTo(binding.nextPageButtonView.fadeVisibility())
        itemsView?.pageScrolled()?.bindTo(pm.pageChangedAction)

        binding.previewPageButtonView.clicks().bindTo(pm.previousPageAction)
        binding.nextPageButtonView.clicks().bindTo(pm.nextPageAction)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.skipPageAction)

        bindProgressDialog(pm)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            presentationModel.backHandleAction.consumer.accept(Unit)
        }
    }

    companion object {
        fun newInstance(): OnBoardingFragment = OnBoardingFragment()
    }
}
