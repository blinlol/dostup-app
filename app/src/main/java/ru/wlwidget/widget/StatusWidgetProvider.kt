package ru.wlwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ru.wlwidget.WlApp

class StatusWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val state = app(context).container.stateStore.load()
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, WidgetBinder.buildRemoteViews(context, state, id))
        }
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        if (intent.action != WidgetBinder.ACTION_REFRESH) return
        app(context).container.stateStore.save(WidgetState.Checking)
        WidgetBinder.updateAll(context, WidgetState.Checking)
        WorkManager.getInstance(context).enqueueUniqueWork(
            RefreshWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<RefreshWorker>().build(),
        )
    }

    private fun app(context: Context): WlApp = context.applicationContext as WlApp
}
