package ru.wlwidget.probe

sealed class ProbeResult {
    data class Http(val statusCode: Int) : ProbeResult()
    data object Fail : ProbeResult()
}

fun interface HttpTransport {
    suspend fun probe(url: String): ProbeResult
}

data class GroupVerdict(
    val available: Boolean,
    val cancelledCount: Int,
)

data class CheckResult(
    val whitelist: GroupVerdict,
    val ordinary: GroupVerdict,
    val blocked: GroupVerdict,
)
