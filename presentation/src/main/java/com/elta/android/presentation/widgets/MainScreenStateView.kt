package com.elta.android.presentation.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.stateview.StateData
import com.elta.android.presentation.core.ui.stateview.StateView
import com.jakewharton.rxbinding2.view.visibility
import io.reactivex.Observable
import io.reactivex.functions.Consumer

class MainScreenStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), StateView {

    private val iconView: ImageView?
    private val titleView: TextView?
    private val descriptionView: TextView?

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_main_screen_state, this, true)
        iconView = findViewById(R.id.stateIconView)
        titleView = findViewById(R.id.stateTitleView)
        descriptionView = findViewById(R.id.stateDescriptionView)
    }

    override fun state(): Consumer<in StateData> = Consumer { data ->
        with(data) {
            titleView?.text = title
            descriptionView?.text = description
        }
    }

    fun updateFromConfig(title: String?, description: String?, imageUrl: String?) {
        // Обновляем title если есть
        title?.let { titleView?.text = it }

        // Обновляем description если есть
        description?.let { descriptionView?.text = it }

        // Обновляем фоновое изображение если есть URL
        imageUrl?.let { url ->
            iconView?.load(url) {
                crossfade(true)
                placeholder(R.drawable.ic_main_screen_bolb)
                error(R.drawable.ic_main_screen_bolb)
            }
        }
    }

    fun updateBackgroundImage(imageUrl: String?) {
        imageUrl?.let { url ->
            iconView?.load(url) {
                crossfade(true)
                placeholder(R.drawable.ic_main_screen_bolb)
                error(R.drawable.ic_main_screen_bolb)
            }
        }
    }

    override fun clicks(): Observable<Unit> = Observable.empty()

    override fun enable() = CONSUMER_STUB

    override fun visibility(): Consumer<in Boolean> = this.visibility(View.GONE)

    private companion object {
        val CONSUMER_STUB = Consumer<Boolean> {}
    }
}
