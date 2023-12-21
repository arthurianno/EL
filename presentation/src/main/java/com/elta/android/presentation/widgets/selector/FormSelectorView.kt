package com.elta.android.presentation.widgets.selector

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ViewFormSelectorBinding
import com.elta.android.presentation.utils.checkMainThread
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import com.nullgr.core.ui.extensions.toggleView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.functions.Consumer

class FormSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var iconText: String? = null
        set(value) {
            field = value
            bindIconText()
        }
    var icon: Drawable? = null
        set(value) {
            field = value
            bindIcon()
        }
    var hint: CharSequence? = null
        set(value) {
            field = value
            bindValue()
        }
    private var isSingleLine: Boolean = false
    private var needDrawArrow: Boolean = true
    private var value: String? = null
        set(value) {
            field = value
            bindValue()
        }

    private val textColor: Int by lazy { ContextCompat.getColor(context, R.color.black_blue) }
    private val hintColor: Int by lazy { ContextCompat.getColor(context, R.color.shade_black2) }

    private val binding: ViewFormSelectorBinding by lazy {
        ViewFormSelectorBinding.bind(this)
    }

    init {
        inflate(context, R.layout.view_form_selector, this)
        readAttrs(attrs)
        initDefault()
    }

    fun setIconRes(@DrawableRes icon: Int) {
        this.icon = ContextCompat.getDrawable(context, icon)
        bindIcon()
    }

    fun click(): Observable<Unit> = SelectorClickObservable(this)

    fun value(): Consumer<SelectorOption> = Consumer {
        icon = it.icon
        value = it.text
    }

    fun isEmpty(): Boolean = value.isNullOrEmpty()

    private fun readAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.FormSelectorView, 0, 0)
            icon = array.getDrawable(R.styleable.FormSelectorView_fsv_icon)
            hint = array.getString(R.styleable.FormSelectorView_fsv_hint)
            value = array.getString(R.styleable.FormSelectorView_fsv_title)
            needDrawArrow = array.getBoolean(R.styleable.FormSelectorView_fsv_draw_arrow, true)
            isSingleLine = array.getBoolean(R.styleable.FormSelectorView_fsv_single_line, false)
            array.recycle()
        }
    }

    private fun initDefault() = with(binding) {
        selectorArrowView.toggleView(needDrawArrow)
        selectorTitleView.isSingleLine = isSingleLine
        bindIcon()
        bindValue()
    }

    private fun bindValue() = with(binding) {
        when {
            isEmpty() -> {
                selectorTitleView.text = hint
                selectorTitleView.setTextColor(hintColor)
            }

            else -> {
                selectorTitleView.text = value
                selectorTitleView.setTextColor(textColor)
            }
        }
    }

    private fun bindIcon() = with(binding) {
        iconView.toggleView(icon != null)
        icon?.let { selectorIconView.setImageDrawable(it) }
    }

    private fun bindIconText() {
        binding.iconText.text = iconText
        iconText?.let {
            binding.selectorTitleView.setTextColor(context.getColor(R.color.black_blue))
        }
    }

    private class SelectorClickObservable(
        private val view: FormSelectorView
    ) : Observable<Unit>() {

        override fun subscribeActual(observer: Observer<in Unit>) {
            if (!checkMainThread(observer)) {
                return
            }
            val listener = Listener(view, observer)
            observer.onSubscribe(listener)
            view.setOnClickListener(listener.onClickListener)
        }

        class Listener(
            private val view: View,
            observer: Observer<in Unit>
        ) : MainThreadDisposable() {

            val onClickListener = OnClickListener {
                if (!isDisposed) {
                    observer.onNext(Unit)
                }
            }

            override fun onDispose() {
                view.setOnClickListener(null)
            }
        }
    }
}
