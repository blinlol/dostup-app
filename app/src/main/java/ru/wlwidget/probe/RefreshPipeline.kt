package ru.wlwidget.probe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ru.wlwidget.lists.ListSource
import ru.wlwidget.widget.WidgetState
import java.time.Instant

fun interface NetworkStatus {
    fun hasValidatedDefault(): Boolean
}

fun interface Clock {
    fun now(): Instant
}

class AndroidNetworkStatus(context: Context) : NetworkStatus {
    private val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun hasValidatedDefault(): Boolean {
        val network = connectivity.activeNetwork ?: return false
        val caps = connectivity.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

class RefreshPipeline(
    private val network: NetworkStatus,
    private val lists: ListSource,
    private val engine: ProbeEngine,
    private val clock: Clock,
) {
    suspend fun refresh(): WidgetState {
        if (!network.hasValidatedDefault()) {
            return WidgetState.NoNetwork(clock.now())
        }
        val groups = lists.groups()
        val result = CheckResult(
            whitelist = engine.probeGroup(groups.whitelist),
            ordinary = engine.probeGroup(groups.ordinary),
            blocked = engine.probeGroup(groups.blocked),
        )
        return WidgetState.Ready(
            whitelistAvailable = result.whitelist.available,
            ordinaryAvailable = result.ordinary.available,
            blockedAvailable = result.blocked.available,
            at = clock.now(),
        )
    }
}
