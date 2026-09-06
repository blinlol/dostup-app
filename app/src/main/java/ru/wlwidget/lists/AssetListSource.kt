package ru.wlwidget.lists

import android.content.Context

class AssetListSource(context: Context) : ListSource {
    private val delegate: ListSource by lazy {
        val json = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        JsonListSource(json)
    }

    override fun groups(): ProbeGroups = delegate.groups()

    companion object {
        const val ASSET_NAME = "probe_lists.json"
    }
}
