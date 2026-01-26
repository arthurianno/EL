// BaseFragment.kt
package com.elta.android.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import coil.load
import coil.request.CachePolicy
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.R
import com.elta.android.presentation.core.navigation.FlowRouter
import com.elta.android.presentation.core.navigation.RouterProvider
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ScreenConfigurable
import com.elta.android.presentation.core.pm.factory.PmFactory
import com.elta.android.presentation.core.pm.widgets.SnackBarControl
import com.elta.android.presentation.core.pm.widgets.bind
import com.elta.android.presentation.core.ui.snackbarview.SnackBarData
import com.elta.android.presentation.core.ui.stateview.StateView
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.utils.applyInsetsToContentView
import com.elta.android.presentation.utils.findAndClearFocus
import com.elta.android.presentation.utils.hideKeyboardFun
import com.elta.android.presentation.utils.makeSnackBar
import com.elta.android.presentation.utils.visibility
import com.elta.android.presentation.widgets.dialogs.ProgressDialog
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.nullgr.core.ui.extensions.hideKeyboard
import com.nullgr.core.ui.extensions.setStatusBarColor
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import me.dmdev.rxpm.base.PmFragment
import me.dmdev.rxpm.bindTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal const val DEBOUNCE_MILLIS = 300L

internal typealias Inflater<B> = (LayoutInflater, ViewGroup?, Boolean) -> B

abstract class BaseFragment<T : BasePm, B : ViewBinding>(
    private val bindingInflater: Inflater<B>
) : PmFragment<T>() {

    @Inject
    lateinit var factory: PmFactory

    private var _binding: B? = null
    protected val binding
        get() = checkNotNull(_binding)

    protected abstract val screenLayout: Int

    protected val compositeUnbind = CompositeDisposable()
    protected val compositeDestroy = CompositeDisposable()

    protected abstract val classToken: Class<T>

    protected abstract val statusBarConfigProvider: StatusBarConfigProvider?

    protected open val backgroundColor: Int? = R.color.color_window_background

    open val progressDialog: ProgressDialog by lazy { ProgressDialog.newInstance() }
    open val router by lazy(LazyThreadSafetyMode.NONE) {
        ((parentFragment ?: activity) as RouterProvider).router as FlowRouter
    }

    private var errorStateView: StateView? = null
    private var emptyStateView: StateView? = null
    private var progressView: View? = null
    private var homeButtonView: View? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        addOnBackPressedCallback { router.exit() }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        _binding = null
        compositeDestroy.clear()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        errorStateView = view.findViewById<View>(R.id.errorStateView) as? StateView
        emptyStateView = view.findViewById<View>(R.id.emptyStateView) as? StateView
        progressView = view.findViewById(R.id.progressView)
        homeButtonView = view.findViewById(R.id.homeButtonView)
    }

    override fun onResume() {
        super.onResume()
        backgroundColor?.let { activity?.window?.setBackgroundDrawableResource(it) }
    }

    override fun onPause() {
        super.onPause()
        view?.hideKeyboardFun()
    }

    override fun onStart() {
        super.onStart()
        initStatusBarConfig()
    }

    @CallSuper
    override fun onBindPresentationModel(pm: T) {
        errorStateView?.let { stateView -> pm.errorControl.bind(stateView, compositeUnbind) }
        emptyStateView?.let { stateView -> pm.emptyControl.bind(stateView, compositeUnbind) }
        progressView?.let { view -> pm.progressState.bindTo(view.visibility()) }
        homeButtonView?.clicks()?.subscribe { router.exit() }
        pm.showSnackBarCommand.bindTo { showSnackbar(it) }
        pm.showToastCommand.bindTo { showToast(it) }
        pm.hideKeyBoardCommand.bindTo { requireActivity().hideKeyboard() }
        pm.clearFocusCommand.bindTo { requireActivity().findAndClearFocus() }
    }

    override fun providePresentationModel(): T {
        val pm = factory.createViewModel(classToken)
        pm.router = router
        return pm
    }

    open fun initStatusBarConfig() {
        statusBarConfigProvider.applyStatusBarConfig()
    }

    protected fun StatusBarConfigProvider?.applyStatusBarConfig() {
        this?.let {
            view?.applyInsetsToContentView(!it.drawUnderStatusBar)
            activity?.window?.setStatusBarColor(it.statusBarColor, it.lightStatusBar)
        }
    }

    protected fun bindProgressDialog(pm: T) {
        pm.progressState.observable
            .throttleLast(DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)
            .subscribe(progressDialog.visibility(childFragmentManager)) {}
    }

    private fun showToast(messageId: Int, showTime: Int = Toast.LENGTH_LONG) {
        Toast.makeText(requireContext(), getString(messageId), showTime).show()
    }

    private fun showSnackbar(data: SnackBarData) {
        view?.let { content ->
            makeSnackBar(content, data).show()
        }
    }

    fun <T> SnackBarControl<T>.bindTo(createSnackBar: (data: T, sc: SnackBarControl<T>) -> Snackbar) {
        bind({ data, sc -> createSnackBar(data, sc) }, compositeDestroy)
    }


    protected inner class ScreenConfigBinder(private val pm: T) {
        private var backgroundImageView: ImageView? = null
        private var titleView: TextView? = null
        private var descriptionView: TextView? = null
        private var rootView: View? = null
        private var defaultTitleRes: Int? = null
        private var defaultDescriptionRes: Int? = null
        private var defaultImageRes: Int? = null
        private var onConfigLoaded: ((ScreenEntity?) -> Unit)? = null

        fun withBackgroundImage(view: ImageView, defaultRes: Int? = null) = apply {
            this.backgroundImageView = view
            this.defaultImageRes = defaultRes
        }

        fun withTitle(view: TextView, defaultRes: Int? = null) = apply {
            this.titleView = view
            this.defaultTitleRes = defaultRes
        }

        fun withDescription(view: TextView, defaultRes: Int? = null) = apply {
            this.descriptionView = view
            this.defaultDescriptionRes = defaultRes
        }

        fun withRootView(view: View) = apply {
            this.rootView = view
        }

        fun onConfigLoaded(action: (ScreenEntity?) -> Unit) = apply {
            this.onConfigLoaded = action
        }

        fun bind() {
            // ВАЖНО: Сначала показываем дефолтные значения сразу!
            showDefaults()

            // Проверяем, что PM реализует ScreenConfigurable
            if (pm !is ScreenConfigurable) {
                Log.w("BaseFragment", "PM doesn't implement ScreenConfigurable, using defaults only")
                return
            }

            rootView?.visibility = View.INVISIBLE

            // Подписка на конфигурацию (если придет)
            pm.screenConfigState.bindTo { screenEntity ->
                if (screenEntity != null) {
                    // Обновляем тексты из конфигурации
                    titleView?.text = screenEntity.title
                        ?: defaultTitleRes?.let { getString(it) }

                    descriptionView?.text = screenEntity.description
                        ?: defaultDescriptionRes?.let { getString(it) }

                    onConfigLoaded?.invoke(screenEntity)
                } else {
                    // Конфигурация не пришла - оставляем дефолты (уже установлены)
                    Log.d("BaseFragment", "No screen config, using defaults")
                }
            }

            // Подписка на готовность картинки
            pm.imagePreloadState.bindTo { isReady ->
                val screenEntity = pm.screenConfigState.valueOrNull
                val imageUrl = screenEntity?.backgroundImageUrl

                if (isReady && imageUrl != null) {
                    // Загружаем картинку (из кеша или сети)
                    loadImageFromUrl(backgroundImageView, imageUrl, defaultImageRes)
                } else {
                    // URL картинки нет - дефолтная уже установлена!
                    Log.d("BaseFragment", "Image not ready or no URL, keeping default image")
                }

                // Всегда показываем экран после проверки картинки
                rootView?.visibility = View.VISIBLE
            }
        }

        // Устанавливаем дефолтные значения сразу при биндинге
        private fun showDefaults() {
            // Дефолтная картинка
            defaultImageRes?.let {
                backgroundImageView?.setImageResource(it)
                Log.d("BaseFragment", "Set default image: $it")
            }

            // Дефолтные тексты
            defaultTitleRes?.let {
                titleView?.text = getString(it)
            }

            defaultDescriptionRes?.let {
                descriptionView?.text = getString(it)
            }

            // Показываем экран сразу (fallback на случай если PM не ScreenConfigurable)
            rootView?.visibility = View.VISIBLE
        }

        private fun loadImageFromUrl(imageView: ImageView?, url: String, defaultRes: Int?) {
            imageView ?: return

            imageView.load(url) {
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                networkCachePolicy(CachePolicy.ENABLED) // Разрешаем загрузку из сети

                // При ошибке - вернуться к дефолтной
                defaultRes?.let { error(it) }

                listener(
                    onError = { _, result ->
                        Log.e("BaseFragment", "Error loading image from URL: ${result.throwable.message}")
                    },
                    onSuccess = { _, _ ->
                        Log.d("BaseFragment", "Successfully loaded image from URL: $url")
                    }
                )
            }
        }
    }

    protected fun bindScreenConfig(pm: T, builder: ScreenConfigBinder.() -> Unit) {
        ScreenConfigBinder(pm).apply(builder).bind()
    }
}