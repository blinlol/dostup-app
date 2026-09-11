package ru.wlwidget.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PeriodicRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        RefreshScheduler.enqueue(applicationContext, RefreshTrigger.PERIODIC)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "periodic-refresh"
        const val INTERVAL_MINUTES = 15L
    }
}
