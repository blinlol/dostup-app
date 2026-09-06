package ru.wlwidget.lists

data class ProbeGroups(
    val whitelist: List<String>,
    val ordinary: List<String>,
    val blocked: List<String>,
) {
    fun validate() {
        mapOf(
            "whitelist" to whitelist,
            "ordinary" to ordinary,
            "blocked" to blocked,
        ).forEach { (name, urls) ->
            require(urls.size >= 5 && urls.size % 2 == 1) {
                "Group $name must have an odd count of at least 5, was ${urls.size}"
            }
            require(urls.all { it.startsWith("https://") }) {
                "Group $name must contain only https URLs"
            }
        }
    }
}

fun interface ListSource {
    fun groups(): ProbeGroups
}
