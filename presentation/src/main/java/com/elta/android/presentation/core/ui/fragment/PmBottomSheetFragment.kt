package com.elta.android.presentation.core.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import com.elta.android.presentation.core.ui.bottom_sheet.BottomSheetDialog
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.AndroidPmView
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.delegate.PmSupportFragmentDelegate

abstract class PmBottomSheetFragment<PM : PresentationModel> : AppCompatDialogFragment(), AndroidPmView<PM> {

    private val delegate by lazy(LazyThreadSafetyMode.NONE) { PmSupportFragmentDelegate(this) }

    final override val compositeUnbind = CompositeDisposable()

    final override val presentationModel get() = delegate.presentationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(checkNotNull(context), theme)

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }
}