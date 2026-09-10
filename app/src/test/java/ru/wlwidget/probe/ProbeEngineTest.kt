package ru.wlwidget.probe

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.wlwidget.lists.ProbeGroups
import ru.wlwidget.widget.WidgetState

@OptIn(ExperimentalCoroutinesApi::class)
class ProbeEngineTest {
    private val five = listOf(
        "https://a.test",
        "https://b.test",
        "https://c.test",
        "https://d.test",
        "https://e.test",
    )

    @Test
    fun httpStatusIsSuccessTimeoutIsFailure() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val transport = HttpTransport { url ->
            if (url.endsWith("ok")) ProbeResult.Http(403) else {
                delay(5_000)
                ProbeResult.Fail
            }
        }
        val engine = ProbeEngine(transport, dispatcher)
        val ok = engine.probeGroup(
            listOf("https://ok", "https://ok", "https://ok", "https://slow", "https://slow"),
        )
        assertTrue(ok.available)
        val fail = engine.probeGroup(
            listOf("https://slow", "https://slow", "https://slow", "https://ok", "https://ok"),
        )
        assertFalse(fail.available)
    }

    @Test
    fun threeSuccessesOfFiveCancelRemainder() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val transport = HttpTransport { url ->
            if (url.contains("ok")) ProbeResult.Http(200) else {
                delay(10_000)
                ProbeResult.Http(200)
            }
        }
        val engine = ProbeEngine(transport, dispatcher)
        val verdict = engine.probeGroup(
            listOf("https://ok1", "https://ok2", "https://ok3", "https://hang1", "https://hang2"),
        )
        assertTrue(verdict.available)
        assertEquals(2, verdict.cancelledCount)
    }

    @Test
    fun threeFailuresOfFiveCancelRemainder() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val transport = HttpTransport { url ->
            if (url.contains("bad")) ProbeResult.Fail else {
                delay(10_000)
                ProbeResult.Http(200)
            }
        }
        val engine = ProbeEngine(transport, dispatcher)
        val verdict = engine.probeGroup(
            listOf("https://bad1", "https://bad2", "https://bad3", "https://hang1", "https://hang2"),
        )
        assertFalse(verdict.available)
        assertEquals(2, verdict.cancelledCount)
    }

    @Test
    fun refreshSkipsProbesWhenNoNetwork() = runTest {
        var probed = 0
        val dispatcher = StandardTestDispatcher(testScheduler)
        val engine = ProbeEngine(
            HttpTransport {
                probed += 1
                ProbeResult.Http(200)
            },
            dispatcher,
        )
        val pipeline = RefreshPipeline(
            network = { false },
            lists = { sampleGroups() },
            engine = engine,
            clock = { java.time.Instant.parse("2026-01-01T12:00:00Z") },
        )
        val state = pipeline.refresh()
        assertTrue(state is WidgetState.NoNetwork)
        assertEquals(0, probed)
    }

    @Test
    fun refreshReturnsThreeGroupVerdictsOnValidatedNetwork() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val engine = ProbeEngine(
            HttpTransport { url ->
                if (url.contains("blocked")) ProbeResult.Fail else ProbeResult.Http(200)
            },
            dispatcher,
        )
        val pipeline = RefreshPipeline(
            network = { true },
            lists = { sampleGroups() },
            engine = engine,
            clock = { java.time.Instant.parse("2026-01-01T12:00:00Z") },
        )
        val state = pipeline.refresh() as WidgetState.Ready
        assertTrue(state.whitelistAvailable)
        assertTrue(state.ordinaryAvailable)
        assertFalse(state.blockedAvailable)
    }

    private fun sampleGroups() = ProbeGroups(
        whitelist = five,
        ordinary = five.map { it.replace(".test", ".ordinary") },
        blocked = five.map { "https://blocked-$it" },
    )
}
