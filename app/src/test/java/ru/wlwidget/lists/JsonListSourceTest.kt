package ru.wlwidget.lists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class JsonListSourceTest {
    @Test
    fun bundledListsMeetSizeRule() {
        val json = File("src/main/assets/probe_lists.json").readText()
        val groups = JsonListSource(json).groups()
        listOf(groups.whitelist, groups.ordinary, groups.blocked).forEach { urls ->
            assertTrue(urls.size >= 5 && urls.size % 2 == 1)
            assertTrue(urls.all { it.startsWith("https://") })
        }
        assertEquals(5, groups.whitelist.size)
        assertEquals(5, groups.ordinary.size)
        assertEquals(5, groups.blocked.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingGroupFails() {
        JsonListSource(
            """{"version":1,"groups":{"whitelist":["https://a.com","https://b.com","https://c.com","https://d.com","https://e.com"],"ordinary":["https://a.com","https://b.com","https://c.com","https://d.com","https://e.com"]}}""",
        ).groups()
    }

    @Test(expected = IllegalArgumentException::class)
    fun evenSizedGroupFails() {
        JsonListSource(fileWithSizes(6, 5, 5)).groups()
    }

    @Test(expected = IllegalArgumentException::class)
    fun groupSmallerThanFiveFails() {
        JsonListSource(fileWithSizes(3, 5, 5)).groups()
    }

    private fun fileWithSizes(w: Int, n: Int, b: Int): String {
        fun urls(count: Int) = (1..count).joinToString(",") { "\"https://example$it.com\"" }
        return """{"version":1,"groups":{"whitelist":[${urls(w)}],"ordinary":[${urls(n)}],"blocked":[${urls(b)}]}}"""
    }
}
