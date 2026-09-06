package ru.wlwidget.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.wlwidget.WlApp

class RefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as WlApp).container
        val state = container.pipeline.refresh()
        container.stateStore.save(state)
        WidgetBinder.updateAll(applicationContext, state)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "tap-refresh"
    }
}
