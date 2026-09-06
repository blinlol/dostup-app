package ru.wlwidget.widget

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object WidgetPresenter {
    private val timeFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    const val STATUS_CHECKING = "Проверяем…"
    const val STATUS_NO_NETWORK = "Нет сети"
    const val STATUS_TAP_TO_CHECK = "Нажмите для проверки"

    fun present(state: WidgetState): WidgetUi = when (state) {
        WidgetState.Idle, WidgetState.Checking -> WidgetUi(
            whitelist = CellUi(CellTone.Idle),
            ordinary = CellUi(CellTone.Idle),
            blocked = CellUi(CellTone.Idle),
            statusLine = if (state is WidgetState.Checking) STATUS_CHECKING else STATUS_TAP_TO_CHECK,
        )
        is WidgetState.NoNetwork -> WidgetUi(
            whitelist = CellUi(CellTone.Idle),
            ordinary = CellUi(CellTone.Idle),
            blocked = CellUi(CellTone.Idle),
            statusLine = "${STATUS_NO_NETWORK} ${timeFormat.format(state.at)}",
        )
        is WidgetState.Ready -> WidgetUi(
            whitelist = CellUi(tone(state.whitelistAvailable)),
            ordinary = CellUi(tone(state.ordinaryAvailable)),
            blocked = CellUi(tone(state.blockedAvailable)),
            statusLine = timeFormat.format(state.at),
        )
    }

    private fun tone(available: Boolean): CellTone =
        if (available) CellTone.Available else CellTone.Unavailable
}
