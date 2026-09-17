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
        val app = File("src/main/java/ru/wlwidget/WlApp.kt").readText()
        assertTrue(app.contains("OverlayListSource"))
        assertTrue(app.contains("lists = listSource"))
        assertTrue(app.contains("AssetListSource"))
    }

    @Test
    fun autoRefreshTriggersAreWired() {
        val kotlin = File("src/main/java").walkTopDown().filter { it.extension == "kt" }
            .associate { it.path to it.readText() }
        val all = kotlin.values.joinToString("\n")
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val scheduler = kotlin.entries.first { it.key.endsWith("RefreshScheduler.kt") }.value
        val periodic = kotlin.entries.first { it.key.endsWith("PeriodicRefreshWorker.kt") }.value
        val auto = kotlin.entries.first { it.key.endsWith("AutoRefreshController.kt") }.value
        val provider = kotlin.entries.first { it.key.endsWith("StatusWidgetProvider.kt") }.value
        val app = kotlin.entries.first { it.key.endsWith("WlApp.kt") }.value

        assertFalse(manifest.contains("CONNECTIVITY_CHANGE"))
        assertTrue(all.contains("PeriodicWorkRequestBuilder<PeriodicRefreshWorker>"))
        assertTrue(periodic.contains("WORK_NAME = \"periodic-refresh\""))
        assertTrue(periodic.contains("INTERVAL_MINUTES = 15L"))
        assertTrue(scheduler.contains("WORK_NAME = \"widget-refresh\""))
        assertTrue(scheduler.contains("OneTimeWorkRequestBuilder<RefreshWorker>"))
        assertTrue(auto.contains("registerDefaultNetworkCallback"))
        assertTrue(auto.contains("unregisterNetworkCallback"))
        assertTrue(auto.contains("ExistingPeriodicWorkPolicy.KEEP"))
        val debounceFn = auto.substringAfter("fun debouncePathChange")
        val debounce = debounceFn.indexOf("handler.removeCallbacks(enqueueNetwork)")
        val delayed = debounceFn.indexOf("handler.postDelayed(enqueueNetwork, PATH_CHANGE_DEBOUNCE_MS)")
        assertTrue(debounce >= 0 && delayed > debounce)
        assertTrue(auto.contains("PATH_CHANGE_DEBOUNCE_MS = 2_000L"))
        assertTrue(auto.contains("onAvailable") && auto.contains("onLost"))
        assertTrue(provider.contains("onEnabled") && provider.contains("autoRefresh.start()"))
        assertTrue(provider.contains("onDisabled") && provider.contains("autoRefresh.stop()"))
        assertTrue(provider.contains("cancelUniqueWork").not())
        assertTrue(auto.contains("cancelUniqueWork(PeriodicRefreshWorker.WORK_NAME)"))
        assertTrue(app.contains("AutoRefreshController.widgetsExist") && app.contains("autoRefresh.start()"))
    }

    @Test
    fun launcherScreenHasInstructionsOnly() {
        val activity = File("src/main/java/ru/wlwidget/MainActivity.kt").readText()
        val layout = File("src/main/res/layout/activity_main.xml").readText()
        val row = File("src/main/res/layout/item_probe_url.xml").readText()
        val strings = File("src/main/res/values/strings.xml").readText()
        val binder = File("src/main/java/ru/wlwidget/widget/WidgetBinder.kt").readText()
        assertTrue(activity.contains("activity_main"))
        assertTrue(activity.contains("overlay.groups()"))
        assertTrue(activity.contains("overlay.save"))
        assertTrue(activity.contains("overlay.clear()"))
        assertTrue(activity.contains("AlertDialog"))
        assertTrue(activity.contains("add_widget_instructions"))
        assertTrue(layout.contains("whitelist_urls") && layout.contains("ordinary_urls") && layout.contains("blocked_urls"))
        assertTrue(layout.contains("whitelist_add") && layout.contains("ordinary_add") && layout.contains("blocked_add"))
        assertTrue(layout.contains("button_save") && layout.contains("button_reset"))
        assertTrue(layout.contains("button_help") && layout.contains("layout_gravity=\"top|end\""))
        assertTrue(row.contains("url_input") && row.contains("url_remove"))
        assertTrue(strings.contains("Как добавить виджет"))
        assertTrue(strings.contains("Сохранить"))
        assertTrue(strings.contains("Сбросить к встроенным"))
        assertFalse(strings.contains("нет настроек"))
        assertFalse(binder.contains("MainActivity"))
        assertFalse(activity.contains("pipeline.refresh()"))
    }

    @Test
    fun refreshPipelineIsTheOnlyRefreshEntry() {
        val pipeline = File("src/main/java/ru/wlwidget/probe/RefreshPipeline.kt").readText()
        val refreshWorker = File("src/main/java/ru/wlwidget/widget/RefreshWorker.kt").readText()
        val periodic = File("src/main/java/ru/wlwidget/widget/PeriodicRefreshWorker.kt").readText()
        val scheduler = File("src/main/java/ru/wlwidget/widget/RefreshScheduler.kt").readText()
        val activity = File("src/main/java/ru/wlwidget/MainActivity.kt").readText()
        assertTrue(pipeline.contains("suspend fun refresh()"))
        assertTrue(refreshWorker.contains("pipeline.refresh()"))
        assertFalse(periodic.contains("pipeline.refresh()"))
        assertFalse(activity.contains("pipeline.refresh()"))
        assertTrue(periodic.contains("RefreshScheduler.enqueue"))
        assertTrue(periodic.contains("RefreshTrigger.PERIODIC"))
        assertTrue(scheduler.contains("RefreshTrigger.TAP"))
        assertFalse(scheduler.contains("SAVE"))
        assertTrue(scheduler.contains("enum class RefreshTrigger"))
        assertTrue(scheduler.contains("TAP(") && scheduler.contains("NETWORK(") && scheduler.contains("PERIODIC("))
    }

    @Test
    fun saveStartsTapCheckOnlyWhenWidgetsExist() {
        val scheduler = File("src/main/java/ru/wlwidget/widget/RefreshScheduler.kt").readText()
        val activity = File("src/main/java/ru/wlwidget/MainActivity.kt").readText()
        val helper = scheduler.substringAfter("fun enqueueUserCheckIfWidgetsExist")
        val widgetsExist = helper.indexOf("AutoRefreshController.widgetsExist")
        val persistOnly = helper.indexOf("return")
        val saveChecking = helper.indexOf("stateStore.save(WidgetState.Checking)")
        val paintChecking = helper.indexOf("WidgetBinder.updateAll(context, WidgetState.Checking)")
        val enqueueTap = helper.indexOf("enqueue(context, RefreshTrigger.TAP)")
        assertTrue(widgetsExist >= 0 && persistOnly > widgetsExist)
        assertTrue(saveChecking > persistOnly && paintChecking > saveChecking && enqueueTap > paintChecking)
        assertFalse(helper.contains("pipeline.refresh()"))
        val overlaySave = activity.indexOf("overlay.save")
        val enqueueHelper = activity.indexOf("RefreshScheduler.enqueueUserCheckIfWidgetsExist")
        assertTrue(overlaySave >= 0 && enqueueHelper > overlaySave)
        assertFalse(activity.contains("pipeline.refresh()"))
    }
}
