package ru.wlwidget.widget

import org.json.JSONObject
import java.time.Instant

interface StringStore {
    fun read(): String?
    fun write(value: String?)
}

class WidgetStateStore(private val store: StringStore) {
    fun load(): WidgetState {
        val raw = store.read() ?: return WidgetState.Idle
        val json = JSONObject(raw)
        return when (json.getString("type")) {
            "no_network" -> WidgetState.NoNetwork(Instant.parse(json.getString("at")))
            "ready" -> WidgetState.Ready(
                whitelistAvailable = json.getBoolean("whitelist"),
                ordinaryAvailable = json.getBoolean("ordinary"),
                blockedAvailable = json.getBoolean("blocked"),
                at = Instant.parse(json.getString("at")),
            )
            else -> WidgetState.Idle
        }
    }

    fun save(state: WidgetState) {
        when (state) {
            WidgetState.Idle, WidgetState.Checking -> Unit
            is WidgetState.NoNetwork -> store.write(
                JSONObject()
                    .put("type", "no_network")
                    .put("at", state.at.toString())
                    .toString(),
            )
            is WidgetState.Ready -> store.write(
                JSONObject()
                    .put("type", "ready")
                    .put("whitelist", state.whitelistAvailable)
                    .put("ordinary", state.ordinaryAvailable)
                    .put("blocked", state.blockedAvailable)
                    .put("at", state.at.toString())
                    .toString(),
            )
        }
    }
}
