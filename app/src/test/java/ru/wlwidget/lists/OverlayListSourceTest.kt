package ru.wlwidget.lists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import ru.wlwidget.widget.StringStore

class OverlayListSourceTest {
    private val bundledGroups = ProbeGroups(five("bundled-w"), five("bundled-o"), five("bundled-b"))
    private val overlayGroups = ProbeGroups(five("saved-w"), five("saved-o"), five("saved-b"))

    @Test
    fun missingOverlayUsesBundledGroups() {
        val source = OverlayListSource(InMemoryStore(), ListSource { bundledGroups })
        assertEquals(bundledGroups, source.groups())
    }

    @Test
    fun emptyOverlayUsesBundledGroups() {
        val store = InMemoryStore().also { it.write("") }
        val source = OverlayListSource(store, ListSource { bundledGroups })
        assertEquals(bundledGroups, source.groups())
    }

    @Test
    fun corruptOverlayUsesBundledGroups() {
        val store = InMemoryStore().also { it.write("{not-json") }
        val source = OverlayListSource(store, ListSource { bundledGroups })
        assertEquals(bundledGroups, source.groups())
    }

    @Test
    fun invalidOverlayUsesBundledGroups() {
        val store = InMemoryStore().also {
            it.write(
                """{"version":1,"groups":{"whitelist":["https://a.example"],"ordinary":["https://b.example","https://c.example","https://d.example","https://e.example","https://f.example"],"blocked":["https://g.example","https://h.example","https://i.example","https://j.example","https://k.example"]}}""",
            )
        }
        val source = OverlayListSource(store, ListSource { bundledGroups })
        assertEquals(bundledGroups, source.groups())
    }

    @Test
    fun validOverlayReplacesBundledGroups() {
        val source = OverlayListSource(InMemoryStore(), ListSource { bundledGroups })
        source.save(overlayGroups)
        assertEquals(overlayGroups, source.groups())
    }

    @Test
    fun clearReturnsToBundledGroups() {
        val source = OverlayListSource(InMemoryStore(), ListSource { bundledGroups })
        source.save(overlayGroups)
        source.clear()
        assertEquals(bundledGroups, source.groups())
    }

    @Test
    fun invalidSaveDoesNotChangeGroups() {
        val source = OverlayListSource(InMemoryStore(), ListSource { bundledGroups })
        source.save(overlayGroups)
        assertThrows(IllegalArgumentException::class.java) {
            source.save(overlayGroups.copy(whitelist = overlayGroups.whitelist.take(4)))
        }
        assertEquals(overlayGroups, source.groups())
    }

    @Test
    fun saveThenResetRoundTripsToBundled() {
        val source = OverlayListSource(InMemoryStore(), ListSource { bundledGroups })
        source.save(overlayGroups)
        assertEquals(overlayGroups, source.groups())
        source.clear()
        assertEquals(bundledGroups, source.groups())
    }

    private fun five(prefix: String) = (1..5).map { "https://$prefix$it.example" }

    private class InMemoryStore : StringStore {
        private var value: String? = null
        override fun read(): String? = value
        override fun write(value: String?) {
            this.value = value
        }
    }
}
