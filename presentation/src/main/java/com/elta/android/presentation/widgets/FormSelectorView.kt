package com.elta.android.presentation.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elta.android.presentation.R
import com.elta.android.presentation.utils.checkMainThread
import com.nullgr.core.ui.extensions.toggleView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import kotlinx.android.synthetic.main.view_form_selector.view.*
import java.util.function.Consumer

class FormSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var icon: Drawable? = null
    private var hint: String? = null
    private var needDrawArrow: Boolean = true
    private var value: String? = null

    private val textColor: Int by lazy { ContextCompat.getColor(context, R.color.black_blue) }
    private val hintColor: Int by lazy { ContextCompat.getColor(context, R.color.shade_black2) }

    init {
        inflate(context, R.layout.view_form_selector, this)
        readAttrs(attrs)
        initDefault()
    }

    fun click(): Observable<Unit> = SelectorClickObservable(this)

    fun value(): Consumer<Pair<Drawable?, String>> = Consumer {
        icon = it.first
        value = it.second
    }

    fun isEmpty(): Boolean = value.isNullOrEmpty()

    private fun readAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.FormSelectorView, 0, 0)
            icon = array.getDrawable(R.styleable.FormSelectorView_fsv_icon)
            hint = array.getString(R.styleable.FormSelectorView_fsv_hint)
            value = array.getString(R.styleable.FormSelectorView_fsv_title)
            needDrawArrow = array.getBoolean(R.styleable.FormSelectorView_fsv_draw_arrow, true)
            array.recycle()
        }
    }

    private fun initDefault() {
        selectorArrowView.toggleView(needDrawArrow)
        bindIcon()
        bindValue()
    }

    private fun bindValue() {
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

    private fun bindIcon() {
        selectorIconView.toggleView(icon != null)
        icon?.let { selectorIconView.setImageDrawable(it) }
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