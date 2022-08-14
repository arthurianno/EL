package com.elta.android.presentation.core.ui.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.core.ui.bottom_sheet.BottomSheetDialog
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.visibility
import com.elta.android.presentation.widgets.dialogs.ProgressDialog
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.base.PmDialogFragment
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseBottomSheetFragment<T : BasePm, B : ViewBinding>(
    private val bindingInflater: Inflater<B>
) : PmDialogFragment<T>() {

    @Inject
    lateinit var factory: PmFactory

    private var _binding: B? = null
    protected val binding: B
        get() = checkNotNull(_binding)
    protected val compositeUnbind = CompositeDisposable()

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
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun providePresentationModel(): T = factory.createViewModel(classToken)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    protected fun bindProgressDialog(pm: T) {
        pm.progressState.observable
            .throttleLast(BaseFragment.DEBOUNCE, TimeUnit.MILLISECONDS)
            .subscribe(progressDialog.visibility(childFragmentManager)) {}
    }
}
