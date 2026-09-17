package ru.wlwidget.lists

import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class ProbeGroupsTest {
    @Test
    fun bundledAssetStillValidates() {
        val json = File("src/main/assets/probe_lists.json").readText()
        JsonListSource(json).groups().validate()
    }

    @Test
    fun httpsPathAndWhitespaceAreAccepted() {
        val groups = ProbeGroups(
            whitelist = five("w").mapIndexed { index, url ->
                if (index == 0) "  https://example.com/health  " else url
            },
            ordinary = five("o"),
            blocked = five("b"),
        )
        groups.validate()
    }

    @Test
    fun httpUrlIsRefused() {
        assertThrows(IllegalArgumentException::class.java) {
            ProbeGroups(
                whitelist = listOf(
                    "http://example.com",
                    "https://b.example",
                    "https://c.example",
                    "https://d.example",
                    "https://e.example",
                ),
                ordinary = five("o"),
                blocked = five("b"),
            ).validate()
        }
    }

    @Test
    fun emptyHostIsRefused() {
        assertThrows(IllegalArgumentException::class.java) {
            ProbeGroups(
                whitelist = listOf(
                    "https://",
                    "https://b.example",
                    "https://c.example",
                    "https://d.example",
                    "https://e.example",
                ),
                ordinary = five("o"),
                blocked = five("b"),
            ).validate()
        }
    }

    @Test
    fun inGroupDuplicateIsRefused() {
        assertThrows(IllegalArgumentException::class.java) {
            ProbeGroups(
                whitelist = listOf(
                    "https://a.example",
                    " https://a.example ",
                    "https://c.example",
                    "https://d.example",
                    "https://e.example",
                ),
                ordinary = five("o"),
                blocked = five("b"),
            ).validate()
        }
    }

    @Test
    fun evenSizeIsRefused() {
        assertThrows(IllegalArgumentException::class.java) {
            ProbeGroups(
                whitelist = five("w") + "https://extra.example",
                ordinary = five("o"),
                blocked = five("b"),
            ).validate()
        }
    }

    @Test
    fun sizeBelowFiveIsRefused() {
        assertThrows(IllegalArgumentException::class.java) {
            ProbeGroups(
                whitelist = five("w").take(3),
                ordinary = five("o"),
                blocked = five("b"),
            ).validate()
        }
    }

    private fun five(prefix: String) = (1..5).map { "https://$prefix$it.example" }
}
