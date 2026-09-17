package ru.wlwidget.lists

import org.json.JSONArray
import org.json.JSONObject
import ru.wlwidget.widget.StringStore

class OverlayListSource(
    private val store: StringStore,
    private val bundled: ListSource,
) : ListSource {
    override fun groups(): ProbeGroups {
        val raw = store.read()
        if (raw.isNullOrBlank()) return bundled.groups()
        return try {
            JsonListSource(raw).groups()
        } catch (_: Exception) {
            bundled.groups()
        }
    }

    fun save(groups: ProbeGroups) {
        val trimmed = groups.trimmed()
        trimmed.validate()
        store.write(encode(trimmed))
    }

    fun clear() {
        store.write(null)
    }

    private fun encode(groups: ProbeGroups): String {
        val encodedGroups = JSONObject()
            .put("whitelist", JSONArray(groups.whitelist))
            .put("ordinary", JSONArray(groups.ordinary))
            .put("blocked", JSONArray(groups.blocked))
        return JSONObject()
            .put("version", 1)
            .put("groups", encodedGroups)
            .toString()
    }
}
