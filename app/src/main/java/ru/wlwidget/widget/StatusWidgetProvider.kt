package ru.wlwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import ru.wlwidget.WlApp

class StatusWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val container = app(context).container
        val state = container.stateStore.load()
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, WidgetBinder.buildRemoteViews(context, state, id))
        }
        if (appWidgetIds.isNotEmpty()) {
            container.autoRefresh.start()
        }
    }

    override fun onEnabled(context: Context) {
        app(context).container.autoRefresh.start()
    }

    override fun onDisabled(context: Context) {
        app(context).container.autoRefresh.stop()
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        if (intent.action != WidgetBinder.ACTION_REFRESH) return
        app(context).container.stateStore.save(WidgetState.Checking)
        WidgetBinder.updateAll(context, WidgetState.Checking)
        RefreshScheduler.enqueue(context, RefreshTrigger.TAP)
    }

    private fun app(context: Context): WlApp = context.applicationContext as WlApp
}
