package com.elta.android.presentation.core.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.elta.android.presentation.core.ui.bottom_sheet.BottomSheetDialog
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.PmView
import me.dmdev.rxpm.PresentationModel

abstract class PmBottomSheetFragment<PM : PresentationModel> :
    AppCompatDialogFragment(),
    PmView<PM> {

//    private val delegate by lazy(LazyThreadSafetyMode.NONE) { PmSupportFragmentDelegate(this) }

    val compositeUnbind = CompositeDisposable()

//    override val presentationModel get() = delegate.presentationModel

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        delegate.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
//        BottomSheetDialog(checkNotNull(context), theme)
//
//    override fun onStart() {
//        super.onStart()
//        delegate.onStart()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        delegate.onResume()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        delegate.onSaveInstanceState(outState)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        delegate.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        delegate.onStop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        delegate.onDestroy()
//    }
}
