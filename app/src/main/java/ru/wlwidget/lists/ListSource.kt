package ru.wlwidget.lists

import java.net.URI

data class ProbeGroups(
    val whitelist: List<String>,
    val ordinary: List<String>,
    val blocked: List<String>,
) {
    fun trimmed(): ProbeGroups = ProbeGroups(
        whitelist = whitelist.map { it.trim() },
        ordinary = ordinary.map { it.trim() },
        blocked = blocked.map { it.trim() },
    )

    fun validate() {
        val groups = trimmed()
        mapOf(
            "whitelist" to groups.whitelist,
            "ordinary" to groups.ordinary,
            "blocked" to groups.blocked,
        ).forEach { (name, urls) ->
            require(urls.size >= 5 && urls.size % 2 == 1) {
                "Group $name must have an odd count of at least 5, was ${urls.size}"
            }
            require(urls.all { isHttpsUrlWithHost(it) }) {
                "Group $name must contain only https URLs with a host"
            }
            require(urls.size == urls.toSet().size) {
                "Group $name must not contain duplicate URLs"
            }
        }
    }
}

internal fun isHttpsUrlWithHost(url: String): Boolean {
    if (!url.startsWith("https://")) return false
    return try {
        val host = URI(url).host
        !host.isNullOrBlank()
    } catch (_: Exception) {
        false
    }
}

fun interface ListSource {
    fun groups(): ProbeGroups
}
