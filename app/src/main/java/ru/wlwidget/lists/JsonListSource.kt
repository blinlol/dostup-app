package ru.wlwidget.lists

import org.json.JSONObject

class JsonListSource(json: String) : ListSource {
    private val groups: ProbeGroups = parse(json)

    override fun groups(): ProbeGroups = groups

    private fun parse(json: String): ProbeGroups {
        val root = JSONObject(json)
        val rawGroups = root.getJSONObject("groups")
        fun array(name: String): List<String> {
            require(rawGroups.has(name)) { "Missing group $name" }
            val array = rawGroups.getJSONArray(name)
            return (0 until array.length()).map { array.getString(it) }
        }
        return ProbeGroups(
            whitelist = array("whitelist"),
            ordinary = array("ordinary"),
            blocked = array("blocked"),
        ).also { it.validate() }
    }
}
