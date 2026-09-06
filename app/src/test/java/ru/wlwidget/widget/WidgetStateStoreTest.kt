package ru.wlwidget.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class WidgetStateStoreTest {
    @Test
    fun restoresLastCompletedResultAfterProcessRecreate() {
        val memory = InMemoryStore()
        val first = WidgetStateStore(memory)
        val ready = WidgetState.Ready(
            whitelistAvailable = true,
            ordinaryAvailable = true,
            blockedAvailable = false,
            at = Instant.parse("2026-01-01T12:34:00Z"),
        )
        first.save(ready)
        first.save(WidgetState.Checking)

        val restored = WidgetStateStore(memory).load()
        assertEquals(ready, restored)
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
        assertTrue(!source.contains("MainActivity"))
        val provider = java.io.File("src/main/java/ru/wlwidget/widget/StatusWidgetProvider.kt").readText()
        assertTrue(provider.contains("ACTION_REFRESH"))
        assertTrue(provider.contains("OneTimeWorkRequestBuilder<RefreshWorker>"))
        assertTrue(!provider.contains("PeriodicWorkRequest"))
        assertTrue(!provider.contains("NetworkCallback"))
        assertTrue(!provider.contains("registerDefaultNetworkCallback"))
    }

    private class InMemoryStore : StringStore {
        private var value: String? = null
        override fun read(): String? = value
        override fun write(value: String?) {
            this.value = value
        }
    }
}
