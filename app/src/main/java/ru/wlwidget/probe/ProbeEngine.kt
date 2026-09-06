package ru.wlwidget.probe

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HttpsGetTransport(
    private val timeoutMs: Int = 5_000,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : HttpTransport {
    override suspend fun probe(url: String): ProbeResult = withContext(io) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            instanceFollowRedirects = false
            requestMethod = "GET"
            useCaches = false
        }
        try {
            ProbeResult.Http(connection.responseCode)
        } catch (_: Exception) {
            ProbeResult.Fail
        } finally {
            connection.disconnect()
        }
    }
}

class ProbeEngine(
    private val transport: HttpTransport,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend fun probeGroup(urls: List<String>): GroupVerdict = coroutineScope {
        val threshold = urls.size / 2 + 1
        val outcomes = Channel<Boolean>(Channel.BUFFERED)
        val jobs = urls.map { url ->
            launch(dispatcher) {
                val ok = try {
                    transport.probe(url) is ProbeResult.Http
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    false
                }
                outcomes.send(ok)
            }
        }
        var ok = 0
        var fail = 0
        repeat(urls.size) {
            if (outcomes.receive()) ok++ else fail++
            if (ok >= threshold || fail >= threshold) {
                val cancelled = jobs.count { it.isActive }
                jobs.forEach { it.cancel() }
                outcomes.close()
                return@coroutineScope GroupVerdict(
                    available = ok >= threshold,
                    cancelledCount = cancelled,
                )
            }
        }
        outcomes.close()
        GroupVerdict(available = ok >= threshold, cancelledCount = 0)
    }
}
