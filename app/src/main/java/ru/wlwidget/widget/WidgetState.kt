package ru.wlwidget.widget

import java.time.Instant

sealed class WidgetState {
    data object Idle : WidgetState()
    data object Checking : WidgetState()
    data class NoNetwork(val at: Instant) : WidgetState()
    data class Ready(
        val whitelistAvailable: Boolean,
        val ordinaryAvailable: Boolean,
        val blockedAvailable: Boolean,
        val at: Instant,
    ) : WidgetState()
}

enum class CellTone { Available, Unavailable, Idle }

data class CellUi(val tone: CellTone)

data class WidgetUi(
    val whitelist: CellUi,
    val ordinary: CellUi,
    val blocked: CellUi,
    val statusLine: String,
)
