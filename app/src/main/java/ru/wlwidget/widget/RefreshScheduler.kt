package ru.wlwidget.widget

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ru.wlwidget.WlApp

enum class RefreshTrigger(val key: String) {
    TAP("tap"),
    NETWORK("network"),
    PERIODIC("periodic"),
    ;

    companion object {
        fun fromKey(key: String?): RefreshTrigger =
            entries.firstOrNull { it.key == key } ?: TAP
    }
}

enum class RefreshEnqueuePolicy { REPLACE, KEEP, SKIP }

fun refreshEnqueuePolicy(trigger: RefreshTrigger, tapInFlight: Boolean): RefreshEnqueuePolicy =
    when (trigger) {
        RefreshTrigger.TAP -> RefreshEnqueuePolicy.REPLACE
        RefreshTrigger.NETWORK ->
            if (tapInFlight) RefreshEnqueuePolicy.SKIP else RefreshEnqueuePolicy.REPLACE
        RefreshTrigger.PERIODIC -> RefreshEnqueuePolicy.KEEP
    }

object RefreshScheduler {
    const val WORK_NAME = "widget-refresh"
    const val KEY_TRIGGER = "trigger"

    fun enqueue(context: Context, trigger: RefreshTrigger) {
        val app = context.applicationContext as WlApp
        val tapInFlight = app.container.stateStore.load() is WidgetState.Checking
        val workPolicy = when (refreshEnqueuePolicy(trigger, tapInFlight)) {
            RefreshEnqueuePolicy.SKIP -> return
            RefreshEnqueuePolicy.REPLACE -> ExistingWorkPolicy.REPLACE
            RefreshEnqueuePolicy.KEEP -> ExistingWorkPolicy.KEEP
        }
        val request = OneTimeWorkRequestBuilder<RefreshWorker>()
            .setInputData(workDataOf(KEY_TRIGGER to trigger.key))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, workPolicy, request)
    }

    fun enqueueUserCheckIfWidgetsExist(context: Context) {
        if (!AutoRefreshController.widgetsExist(context)) return
        val app = context.applicationContext as WlApp
        app.container.stateStore.save(WidgetState.Checking)
        WidgetBinder.updateAll(context, WidgetState.Checking)
        enqueue(context, RefreshTrigger.TAP)
    }
}
