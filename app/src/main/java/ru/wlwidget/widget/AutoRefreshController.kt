package ru.wlwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Handler
import android.os.Looper
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AutoRefreshController(private val appContext: Context) {
    private val connectivity =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val handler = Handler(Looper.getMainLooper())
    private var callbackRegistered = false

    private val enqueueNetwork = Runnable {
        RefreshScheduler.enqueue(appContext, RefreshTrigger.NETWORK)
    }

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = debouncePathChange()
        override fun onLost(network: Network) = debouncePathChange()
    }

    fun start() {
        schedulePeriodic()
        registerCallback()
    }

    fun stop() {
        handler.removeCallbacks(enqueueNetwork)
        if (callbackRegistered) {
            connectivity.unregisterNetworkCallback(callback)
            callbackRegistered = false
        }
        WorkManager.getInstance(appContext).cancelUniqueWork(PeriodicRefreshWorker.WORK_NAME)
    }

    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<PeriodicRefreshWorker>(
            PeriodicRefreshWorker.INTERVAL_MINUTES,
            TimeUnit.MINUTES,
        ).build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            PeriodicRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun registerCallback() {
        if (callbackRegistered) return
        connectivity.registerDefaultNetworkCallback(callback)
        callbackRegistered = true
    }

    private fun debouncePathChange() {
        handler.removeCallbacks(enqueueNetwork)
        handler.postDelayed(enqueueNetwork, PATH_CHANGE_DEBOUNCE_MS)
    }

    companion object {
        const val PATH_CHANGE_DEBOUNCE_MS = 2_000L

        fun widgetsExist(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, StatusWidgetProvider::class.java),
            )
            return ids.isNotEmpty()
        }
    }
}
