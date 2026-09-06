package ru.wlwidget

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArchitectureGuardTest {
    @Test
    fun widgetAndProbeDoNotReadListFiles() {
        val roots = listOf(
            File("src/main/java/ru/wlwidget/widget"),
            File("src/main/java/ru/wlwidget/probe"),
        )
        val hits = roots.flatMap { it.walkTopDown().filter { file -> file.extension == "kt" } }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    val forbidden = line.contains("assets.open") ||
                        line.contains("probe_lists.json") ||
                        line.contains("AssetManager")
                    if (forbidden) "${file.path}:${index + 1}:$line" else null
                }
            }
        assertTrue("Unexpected list-file I/O:\n${hits.joinToString("\n")}", hits.isEmpty())
    }

    @Test
    fun noPeriodicOrNetworkCallbackTriggers() {
        val kotlin = File("src/main/java").walkTopDown().filter { it.extension == "kt" }
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("CONNECTIVITY_CHANGE"))
        assertFalse(kotlin.any { it.readText().contains("PeriodicWorkRequest") })
        assertFalse(kotlin.any { it.readText().contains("registerDefaultNetworkCallback") })
        assertFalse(kotlin.any { it.readText().contains("registerNetworkCallback") })
        assertTrue(kotlin.any { it.readText().contains("OneTimeWorkRequestBuilder<RefreshWorker>") })
    }

    @Test
    fun launcherScreenHasInstructionsOnly() {
        val activity = File("src/main/java/ru/wlwidget/MainActivity.kt").readText()
        val layout = File("src/main/res/layout/activity_main.xml").readText()
        val strings = File("src/main/res/values/strings.xml").readText()
        assertTrue(activity.contains("activity_main"))
        assertFalse(activity.contains("settings") || activity.contains("Settings"))
        assertTrue(layout.contains("add_widget_instructions"))
        assertTrue(strings.contains("Как добавить виджет"))
        assertTrue(strings.contains("нет настроек"))
    }
}
