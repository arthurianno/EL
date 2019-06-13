package com.elta.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.utils.hideKeyboardFun
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

abstract class BaseBottomSheetFragment<T : BasePm> : PmBottomSheetFragment<T>() {

    @Inject
    lateinit var factory: PmFactory

    protected abstract val screenLayout: Int
    protected abstract val classToken: Class<T>

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onPause() {
        super.onPause()
        view?.hideKeyboardFun()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(screenLayout, container, false)

    override fun providePresentationModel(): T = factory.createViewModel(classToken)
}