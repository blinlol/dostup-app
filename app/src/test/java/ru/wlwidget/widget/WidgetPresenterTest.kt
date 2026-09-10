package ru.wlwidget.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WidgetPresenterTest {
    private val at = Instant.parse("2026-01-01T09:15:00Z")
    private val expectedTime = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(at)

    @Test
    fun allGroupsAvailable() {
        val ui = WidgetPresenter.present(
            WidgetState.Ready(true, true, true, at),
        )
        assertEquals(CellTone.Available, ui.whitelist.tone)
        assertEquals(CellTone.Available, ui.ordinary.tone)
        assertEquals(CellTone.Available, ui.blocked.tone)
        assertEquals(expectedTime, ui.statusLine)
        assertEquals(false, ui.showSpinner)
    }

    @Test
    fun onlyWhitelistAvailable() {
        val ui = WidgetPresenter.present(
            WidgetState.Ready(true, false, false, at),
        )
        assertEquals(CellTone.Available, ui.whitelist.tone)
        assertEquals(CellTone.Unavailable, ui.ordinary.tone)
        assertEquals(CellTone.Unavailable, ui.blocked.tone)
    }

    @Test
    fun ordinaryWithoutBlocked() {
        val ui = WidgetPresenter.present(
            WidgetState.Ready(true, true, false, at),
        )
        assertEquals(CellTone.Available, ui.whitelist.tone)
        assertEquals(CellTone.Available, ui.ordinary.tone)
        assertEquals(CellTone.Unavailable, ui.blocked.tone)
    }

    @Test
    fun noNetworkIsDistinctFromThreeUnavailable() {
        val noNetwork = WidgetPresenter.present(WidgetState.NoNetwork(at))
        assertEquals(CellTone.Idle, noNetwork.whitelist.tone)
        assertEquals(CellTone.Idle, noNetwork.ordinary.tone)
        assertEquals(CellTone.Idle, noNetwork.blocked.tone)
        assertTrue(noNetwork.statusLine.startsWith(WidgetPresenter.STATUS_NO_NETWORK))
        assertTrue(noNetwork.statusLine.contains(expectedTime))
        assertEquals(false, noNetwork.showSpinner)

        val allFailed = WidgetPresenter.present(WidgetState.Ready(false, false, false, at))
        assertEquals(CellTone.Unavailable, allFailed.whitelist.tone)
        assertEquals(CellTone.Unavailable, allFailed.ordinary.tone)
        assertEquals(CellTone.Unavailable, allFailed.blocked.tone)
        assertEquals(expectedTime, allFailed.statusLine)
        assertEquals(false, allFailed.showSpinner)
    }

    @Test
    fun checkingShowsInProgress() {
        val ui = WidgetPresenter.present(WidgetState.Checking)
        assertEquals(WidgetPresenter.STATUS_CHECKING, ui.statusLine)
        assertEquals(CellTone.Idle, ui.whitelist.tone)
        assertEquals(true, ui.showSpinner)
        assertEquals(false, WidgetPresenter.present(WidgetState.Idle).showSpinner)
    }

    @Test
    fun layoutHasThreeEqualWeightedCells() {
        val xml = File("src/main/res/layout/widget_status.xml").readText()
        assertTrue(xml.contains("cell_whitelist"))
        assertTrue(xml.contains("cell_ordinary"))
        assertTrue(xml.contains("cell_blocked"))
        assertEquals(3, listOf("cell_whitelist", "cell_ordinary", "cell_blocked").count { xml.contains(it) })
        assertTrue(xml.contains("android:layout_weight=\"1\""))
        assertTrue(xml.contains("Белый") || xml.contains("label_whitelist"))
        assertTrue(xml.contains("label_ordinary"))
        assertTrue(xml.contains("label_blocked"))
        val spinnerAt = xml.indexOf("status_spinner")
        val statusAt = xml.indexOf("status_line")
        assertTrue(spinnerAt >= 0 && statusAt > spinnerAt)
        assertTrue(xml.contains("android:indeterminate=\"true\""))
        assertTrue(xml.contains("android:visibility=\"gone\""))
    }
}
