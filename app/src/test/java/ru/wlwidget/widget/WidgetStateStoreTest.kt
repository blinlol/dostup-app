package ru.wlwidget.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class WidgetStateStoreTest {
    @Test
    fun checkingOverlaysLastResultUntilTerminalSave() {
        val memory = InMemoryStore()
        val store = WidgetStateStore(memory)
        val ready = WidgetState.Ready(
            whitelistAvailable = true,
            ordinaryAvailable = true,
            blockedAvailable = false,
            at = Instant.parse("2026-01-01T12:34:00Z"),
        )
        store.save(ready)
        store.save(WidgetState.Checking)
        assertEquals(WidgetState.Checking, WidgetStateStore(memory).load())

        val noNetwork = WidgetState.NoNetwork(Instant.parse("2026-01-01T12:40:00Z"))
        store.save(noNetwork)
        assertEquals(noNetwork, WidgetStateStore(memory).load())
    }

    @Test
    fun missingCheckingFlagLoadsCompletedResult() {
        val memory = InMemoryStore()
        memory.write(
            """{"type":"ready","whitelist":true,"ordinary":false,"blocked":false,"at":"2026-01-01T12:34:00Z"}""",
        )
        val loaded = WidgetStateStore(memory).load()
        assertEquals(
            WidgetState.Ready(
                whitelistAvailable = true,
                ordinaryAvailable = false,
                blockedAvailable = false,
                at = Instant.parse("2026-01-01T12:34:00Z"),
            ),
            loaded,
        )
    }

    @Test
    fun restoresNoNetwork() {
        val memory = InMemoryStore()
        val store = WidgetStateStore(memory)
        val state = WidgetState.NoNetwork(Instant.parse("2026-01-01T08:00:00Z"))
        store.save(state)
        assertEquals(state, WidgetStateStore(memory).load())
    }

    @Test
    fun tapIntentTargetsProviderNotLauncher() {
        val source = java.io.File("src/main/java/ru/wlwidget/widget/WidgetBinder.kt").readText()
        assertTrue(source.contains("StatusWidgetProvider::class.java"))
        assertTrue(source.contains("status_spinner"))
        assertTrue(source.contains("setViewVisibility"))
        assertTrue(!source.contains("MainActivity"))
        val provider = java.io.File("src/main/java/ru/wlwidget/widget/StatusWidgetProvider.kt").readText()
        assertTrue(provider.contains("ACTION_REFRESH"))
        val saveChecking = provider.indexOf("stateStore.save(WidgetState.Checking)")
        val paintChecking = provider.indexOf("WidgetBinder.updateAll(context, WidgetState.Checking)")
        val enqueueTap = provider.indexOf("RefreshScheduler.enqueue(context, RefreshTrigger.TAP)")
        assertTrue(saveChecking >= 0 && paintChecking > saveChecking)
        assertTrue(enqueueTap > paintChecking)
        assertTrue(provider.contains("autoRefresh.start()"))
        assertTrue(provider.contains("autoRefresh.stop()"))
        assertTrue(provider.contains("onDisabled"))
    }

    @Test
    fun refreshWorkerPaintsCheckingOnlyForTap() {
        val worker = java.io.File("src/main/java/ru/wlwidget/widget/RefreshWorker.kt").readText()
        val tapGuard = worker.indexOf("trigger == RefreshTrigger.TAP")
        val paintChecking = worker.indexOf("WidgetBinder.updateAll(applicationContext, WidgetState.Checking)")
        val refresh = worker.indexOf("pipeline.refresh()")
        val save = worker.indexOf("stateStore.save(state)")
        val paintTerminal = worker.lastIndexOf("WidgetBinder.updateAll(applicationContext, state)")
        assertTrue(tapGuard >= 0 && paintChecking > tapGuard && refresh > paintChecking)
        assertTrue(save > refresh && paintTerminal > save)
        val scheduler = java.io.File("src/main/java/ru/wlwidget/widget/RefreshScheduler.kt").readText()
        assertTrue(scheduler.contains("OneTimeWorkRequestBuilder<RefreshWorker>"))
        assertTrue(scheduler.contains("KEY_TRIGGER"))
        val periodic = java.io.File("src/main/java/ru/wlwidget/widget/PeriodicRefreshWorker.kt").readText()
        val auto = java.io.File("src/main/java/ru/wlwidget/widget/AutoRefreshController.kt").readText()
        assertTrue(!periodic.contains("updateAll"))
        assertTrue(!auto.contains("WidgetState.Checking"))
        assertTrue(auto.contains("RefreshTrigger.NETWORK"))
        assertTrue(periodic.contains("RefreshTrigger.PERIODIC"))
    }

    private class InMemoryStore : StringStore {
        private var value: String? = null
        override fun read(): String? = value
        override fun write(value: String?) {
            this.value = value
        }
    }
}
