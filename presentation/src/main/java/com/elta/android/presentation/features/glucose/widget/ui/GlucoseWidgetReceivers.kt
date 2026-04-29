package com.elta.android.presentation.features.glucose.widget.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.elta.android.presentation.features.glucose.widget.worker.GlucoseWidgetUpdateWorker

private val GLUCOSE_WIDGET_RECEIVERS = arrayOf(
    GlucoseSmallAppWidgetReceiver::class.java,
    GlucoseMediumAppWidgetReceiver::class.java,
    GlucoseLargeAppWidgetReceiver::class.java
)

private fun hasAnyGlucoseWidgets(context: Context): Boolean {
    val widgetManager = AppWidgetManager.getInstance(context)
    return GLUCOSE_WIDGET_RECEIVERS.any { receiverClass ->
        val componentName = ComponentName(context, receiverClass)
        widgetManager.getAppWidgetIds(componentName).isNotEmpty()
    }
}

abstract class BaseGlucoseAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        GlucoseWidgetUpdateWorker.schedulePeriodic(context)
        GlucoseWidgetUpdateWorker.requestImmediateUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        GlucoseWidgetUpdateWorker.schedulePeriodic(context)
        GlucoseWidgetUpdateWorker.requestImmediateUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        if (!hasAnyGlucoseWidgets(context)) {
            GlucoseWidgetUpdateWorker.cancel(context)
        }
    }
}

class GlucoseSmallAppWidgetReceiver : BaseGlucoseAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlucoseSmallAppWidget()
}

class GlucoseMediumAppWidgetReceiver : BaseGlucoseAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlucoseMediumAppWidget()
}

class GlucoseLargeAppWidgetReceiver : BaseGlucoseAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlucoseLargeAppWidget()
}
