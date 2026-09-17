package ru.wlwidget

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import ru.wlwidget.lists.OverlayListSource
import ru.wlwidget.lists.ProbeGroups
import ru.wlwidget.widget.RefreshScheduler

class MainActivity : AppCompatActivity() {
    private lateinit var overlay: OverlayListSource
    private lateinit var whitelistUrls: LinearLayout
    private lateinit var ordinaryUrls: LinearLayout
    private lateinit var blockedUrls: LinearLayout
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overlay = (application as WlApp).container.listSource
        whitelistUrls = findViewById(R.id.whitelist_urls)
        ordinaryUrls = findViewById(R.id.ordinary_urls)
        blockedUrls = findViewById(R.id.blocked_urls)
        saveButton = findViewById(R.id.button_save)

        findViewById<Button>(R.id.whitelist_add).setOnClickListener { addRow(whitelistUrls, "") }
        findViewById<Button>(R.id.ordinary_add).setOnClickListener { addRow(ordinaryUrls, "") }
        findViewById<Button>(R.id.blocked_add).setOnClickListener { addRow(blockedUrls, "") }
        saveButton.setOnClickListener { save() }
        findViewById<Button>(R.id.button_reset).setOnClickListener { reset() }
        findViewById<ImageButton>(R.id.button_help).setOnClickListener { showHelp() }

        fill(overlay.groups())
    }

    private fun fill(groups: ProbeGroups) {
        fillGroup(whitelistUrls, groups.whitelist)
        fillGroup(ordinaryUrls, groups.ordinary)
        fillGroup(blockedUrls, groups.blocked)
        updateSaveEnabled()
    }

    private fun fillGroup(container: LinearLayout, urls: List<String>) {
        container.removeAllViews()
        urls.forEach { addRow(container, it) }
    }

    private fun addRow(container: LinearLayout, url: String) {
        val row = layoutInflater.inflate(R.layout.item_probe_url, container, false)
        val input = row.findViewById<EditText>(R.id.url_input)
        input.setText(url)
        input.doAfterTextChanged { updateSaveEnabled() }
        row.findViewById<ImageButton>(R.id.url_remove).setOnClickListener {
            container.removeView(row)
            updateSaveEnabled()
        }
        container.addView(row)
        updateSaveEnabled()
    }

    private fun save() {
        val draft = readDraft()
        try {
            overlay.save(draft)
        } catch (_: IllegalArgumentException) {
            return
        }
        fill(overlay.groups())
        RefreshScheduler.enqueueUserCheckIfWidgetsExist(this)
    }

    private fun reset() {
        overlay.clear()
        fill(overlay.groups())
    }

    private fun showHelp() {
        AlertDialog.Builder(this)
            .setTitle(R.string.help_title)
            .setMessage(R.string.add_widget_instructions)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun updateSaveEnabled() {
        saveButton.isEnabled = try {
            readDraft().validate()
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun readDraft(): ProbeGroups = ProbeGroups(
        whitelist = urlsFrom(whitelistUrls),
        ordinary = urlsFrom(ordinaryUrls),
        blocked = urlsFrom(blockedUrls),
    )

    private fun urlsFrom(container: LinearLayout): List<String> =
        (0 until container.childCount).map { index ->
            container.getChildAt(index).findViewById<EditText>(R.id.url_input).text.toString()
        }
}
