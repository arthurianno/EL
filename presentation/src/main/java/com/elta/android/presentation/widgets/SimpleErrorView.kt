package com.elta.android.presentation.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.state_view.StateData
import com.elta.android.presentation.core.ui.state_view.StateView
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import net.cachapa.expandablelayout.ExpandableLayout

class SimpleErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), StateView {

    private val errorView: ExpandableLayout
    private val iconView: ImageView
    private val descriptionView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_simple_error, this, true)

        errorView = findViewById(R.id.simpleErrorView)
        iconView = findViewById(R.id.simpleErrorIconView)
        descriptionView = findViewById(R.id.simpleErrorDescriptionView)
    }

    override fun state(): Consumer<in StateData> = Consumer { state ->
        state.icon?.let { iconView.setImageResource(it) }
        state.description?.let { descriptionView.text = it }
    }

    override fun clicks(): Observable<Unit> = Observable.empty()

    override fun enable(): Consumer<in Boolean> = CONSUMER_STUB

    override fun visibility(): Consumer<in Boolean> = Consumer { visible ->
        errorView.setExpanded(visible, true)
    }

    private companion object {
        val CONSUMER_STUB = Consumer<Boolean> {}
    }
}
