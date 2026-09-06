package ru.wlwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import ru.wlwidget.R

object WidgetBinder {
    const val ACTION_REFRESH = "ru.wlwidget.action.REFRESH"

    fun buildRemoteViews(context: Context, state: WidgetState, widgetId: Int): RemoteViews {
        val ui = WidgetPresenter.present(state)
        val views = RemoteViews(context.packageName, R.layout.widget_status)
        bindCell(views, R.id.cell_whitelist, ui.whitelist)
        bindCell(views, R.id.cell_ordinary, ui.ordinary)
        bindCell(views, R.id.cell_blocked, ui.blocked)
        views.setTextViewText(R.id.status_line, ui.statusLine)
        views.setOnClickPendingIntent(R.id.widget_root, refreshIntent(context, widgetId))
        return views
    }

    fun updateAll(context: Context, state: WidgetState) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, StatusWidgetProvider::class.java))
        ids.forEach { id ->
            manager.updateAppWidget(id, buildRemoteViews(context, state, id))
        }
    }

    private fun bindCell(views: RemoteViews, cellId: Int, cell: CellUi) {
        views.setInt(cellId, "setBackgroundResource", background(cell.tone))
    }

    private fun background(tone: CellTone): Int = when (tone) {
        CellTone.Available -> R.drawable.cell_available
        CellTone.Unavailable -> R.drawable.cell_unavailable
        CellTone.Idle -> R.drawable.cell_idle
    }

    fun refreshIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = Intent(context, StatusWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            data = Uri.parse("wlwidget://refresh/$widgetId")
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
