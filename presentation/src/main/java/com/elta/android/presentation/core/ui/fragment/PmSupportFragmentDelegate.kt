package com.elta.android.presentation.core.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import me.dmdev.rxpm.PmView
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.navigation.NavigationMessage
import me.dmdev.rxpm.navigation.NavigationMessageDispatcher
import me.dmdev.rxpm.navigation.NavigationMessageHandler
import me.dmdev.rxpm.navigation.NotHandledNavigationMessageException

class PmSupportFragmentDelegate<PM, F>(private val pmView: F)
        where PM : PresentationModel,
              F : Fragment, F : PmView<PM> {

    private lateinit var outlast: FragmentOutlast<PmWrapper<PM>>
    internal lateinit var pmBinder: PmBinder<PM>

    private lateinit var navigationMessagesDisposable: Disposable
    private val navigationMessageDispatcher = SupportFragmentNavigationMessageDispatcher(pmView)

    val presentationModel: PM by lazy(LazyThreadSafetyMode.NONE) { outlast.outlasting.presentationModel }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onCreate(savedInstanceState: Bundle?) {
        outlast = FragmentOutlast(
            pmView,
            Outlasting.Creator<PmWrapper<PM>> {
                PmWrapper(pmView.providePresentationModel())
            },
            savedInstanceState
        )
        presentationModel // Create lazy presentation model now
        pmBinder = PmBinder(presentationModel, pmView)
        navigationMessagesDisposable = presentationModel.navigationMessages.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                navigationMessageDispatcher.dispatch(it)
            }
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onStart() {
        outlast.onStart()
        pmBinder.bind()
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onResume() {
        outlast.onResume()
        pmBinder.bind()
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onSaveInstanceState(outState: Bundle) {
        outlast.onSaveInstanceState(outState)
        pmBinder.unbind()
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onPause() {
        // For symmetry, may be used in the future
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onStop() {
        pmBinder.unbind()
    }

    /**
     * You must call this method from the containing [Fragment]'s corresponding method.
     */
    fun onDestroy() {
        navigationMessagesDisposable.dispose()
        outlast.onDestroy()
    }
}

// internal class PmWrapper<out PM : PresentationModel>(val presentationModel: PM) :
//    me.jeevuz.outlast.Outlasting {
//
//    override fun onCreate() {
//        presentationModel.lifecycleConsumer.accept(PresentationModel.Lifecycle.CREATED)
//    }
//
//    override fun onDestroy() {
//        presentationModel.lifecycleConsumer.accept(PresentationModel.Lifecycle.DESTROYED)
//    }
// }

//internal class PmBinder<out PM : PresentationModel>(
//    private val pm: PM,
//    private val pmView: PmView<PM>
//) {
//
//    var viewBound = false
//        private set
//
//    var listener: Callbacks? = null
//
//    fun bind() {
//        if (!viewBound) {
//            pmView.onBindPresentationModel(pm)
//            pm.lifecycleConsumer.accept(PresentationModel.Lifecycle.BINDED)
//            viewBound = true
//            listener?.onBindPm()
//        }
//    }
//
//    fun unbind() {
//        if (viewBound) {
//            listener?.onUnbindPm()
//            pm.lifecycleConsumer.accept(PresentationModel.Lifecycle.UNBINDED)
//            pmView.onUnbindPresentationModel()
//            viewBound = false
//        }
//    }
//
//    internal interface Callbacks {
//        fun onBindPm()
//        fun onUnbindPm()
//    }
//}

internal class PmWrapper<out PM : PresentationModel>(val presentationModel: PM) : Outlasting {

    override fun onCreate() {
        presentationModel.lifecycleConsumer.accept(PresentationModel.Lifecycle.CREATED)
    }

    override fun onDestroy() {
        presentationModel.lifecycleConsumer.accept(PresentationModel.Lifecycle.DESTROYED)
    }
}

internal class SupportFragmentNavigationMessageDispatcher(
    fragment: Fragment
) : NavigationMessageDispatcher(fragment) {

    override fun getParent(node: Any?): Any? {
        return if (node is Fragment) {
            node.parentFragment ?: node.activity
        } else {
            null
        }
    }
}

//internal abstract class NavigationMessageDispatcher(private val firstNode: Any) {
//
//    fun dispatch(message: NavigationMessage) {
//
//        var node: Any? = firstNode
//
//        do {
//            if (node is NavigationMessageHandler && node.handleNavigationMessage(message)) {
//                return
//            }
//            node = getParent(node)
//        } while (node != null)
//
//        throw NotHandledNavigationMessageException()
//    }
//
//    abstract fun getParent(node: Any?): Any?
//}

// interface NavigationMessage
