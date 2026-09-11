package ru.wlwidget.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshSchedulerTest {
    @Test
    fun tapAlwaysReplaces() {
        assertEquals(
            RefreshEnqueuePolicy.REPLACE,
            refreshEnqueuePolicy(RefreshTrigger.TAP, tapInFlight = false),
        )
        assertEquals(
            RefreshEnqueuePolicy.REPLACE,
            refreshEnqueuePolicy(RefreshTrigger.TAP, tapInFlight = true),
        )
    }

    @Test
    fun networkSkipsWhenTapInFlightOtherwiseReplaces() {
        assertEquals(
            RefreshEnqueuePolicy.SKIP,
            refreshEnqueuePolicy(RefreshTrigger.NETWORK, tapInFlight = true),
        )
        assertEquals(
            RefreshEnqueuePolicy.REPLACE,
            refreshEnqueuePolicy(RefreshTrigger.NETWORK, tapInFlight = false),
        )
    }

    @Test
    fun periodicKeepsEvenWhenTapInFlight() {
        assertEquals(
            RefreshEnqueuePolicy.KEEP,
            refreshEnqueuePolicy(RefreshTrigger.PERIODIC, tapInFlight = false),
        )
        assertEquals(
            RefreshEnqueuePolicy.KEEP,
            refreshEnqueuePolicy(RefreshTrigger.PERIODIC, tapInFlight = true),
        )
    }
}
