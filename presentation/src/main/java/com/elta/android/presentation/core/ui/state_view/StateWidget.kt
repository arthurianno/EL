package com.elta.android.presentation.core.ui.state_view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.visibility
import com.elta.android.presentation.R
import io.reactivex.Observable
import io.reactivex.functions.Consumer

class StateWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), StateView {

    private val iconView: ImageView
    private val titleView: TextView
    private val descriptionView: TextView
    private val buttonView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_state, this, true)

        iconView = findViewById(R.id.stateIconView)
        titleView = findViewById(R.id.stateTitleView)
        descriptionView = findViewById(R.id.stateDescriptionView)
        buttonView = findViewById(R.id.stateButtonView)

        attrs?.let { set ->
            val array = context.obtainStyledAttributes(set, R.styleable.StateWidget, 0, 0)
            val icon = array.getDrawable(R.styleable.StateWidget_state_icon)
            val title = array.getString(R.styleable.StateWidget_state_title)
            val description = array.getString(R.styleable.StateWidget_state_description)
            val button = array.getString(R.styleable.StateWidget_state_button)
            val bg = array.getDrawable(R.styleable.StateWidget_android_background)

            icon?.let { iconView.setImageDrawable(it) }

            titleView.text = title
            descriptionView.text = description
            buttonView.text = button

            bg?.let { background = it }

            array.recycle()
        }
    }

    override fun state(): Consumer<in StateData> = Consumer { data ->
        with(data) {
            icon?.let { iconView.setImageResource(it) }
            titleView.text = title
            descriptionView.text = description
            buttonView.text = button
        }
    }

    override fun clicks(): Observable<Unit> = buttonView.clicks()

    override fun enable(): Consumer<in Boolean> = Consumer { enable ->
        buttonView.isEnabled = enable
    }

    override fun visibility(): Consumer<in Boolean> = this.visibility(View.GONE)
}