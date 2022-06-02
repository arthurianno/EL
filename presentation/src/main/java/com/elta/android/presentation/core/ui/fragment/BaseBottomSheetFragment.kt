package com.elta.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.visibility
import com.elta.android.presentation.widgets.dialogs.ProgressDialog
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseBottomSheetFragment<T : BasePm> : PmBottomSheetFragment<T>() {

    @Inject
    lateinit var factory: PmFactory

    protected abstract val screenLayout: Int
    protected abstract val classToken: Class<T>
    open val progressDialog: ProgressDialog by lazy { ProgressDialog.newInstance() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onPause() {
        super.onPause()
        view?.hideKeyboardFun()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(screenLayout, container, false)

    override fun providePresentationModel(): T = factory.createViewModel(classToken)

    protected fun bindProgressDialog(pm: T) {
        pm.progressState.observable
            .throttleLast(BaseFragment.DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe(progressDialog.visibility(childFragmentManager))
    }
}
