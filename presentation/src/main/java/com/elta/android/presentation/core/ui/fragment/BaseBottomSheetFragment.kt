package com.elta.android.presentation.core.ui.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.factory.PmFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


abstract class BaseBottomSheetFragment<T : BasePm> : PmBottomSheetFragment<T>() {

    @Inject
    lateinit var factory: PmFactory

    /**
     * Layout res which will be inflated in Fragment in onCreateView method
     */
    protected abstract val screenLayout: Int

    /**
     * Style res which will be used as window animations.
     */
    protected open val bottomSheetAnimationStyleRes: Int? = null

    protected abstract val classToken: Class<T>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        bottomSheetAnimationStyleRes?.let {
            dialog.window?.attributes?.windowAnimations = it
        }
        dialog.setOnShowListener { innerDialog ->
            val bottomSheet = (innerDialog as? BottomSheetDialog)?.findViewById(
                android.support.design.R.id.design_bottom_sheet
            ) as? FrameLayout?
            bottomSheet?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        activity?.layoutInflater?.inflate(screenLayout, container, false)

    override fun providePresentationModel(): T = factory.createViewModel(classToken)
}