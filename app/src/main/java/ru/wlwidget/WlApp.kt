package ru.wlwidget

import android.app.Application
import android.content.Context
import kotlinx.coroutines.Dispatchers
import ru.wlwidget.lists.AssetListSource
import ru.wlwidget.probe.AndroidNetworkStatus
import ru.wlwidget.probe.Clock
import ru.wlwidget.probe.HttpsGetTransport
import ru.wlwidget.probe.ProbeEngine
import ru.wlwidget.probe.RefreshPipeline
import ru.wlwidget.widget.AutoRefreshController
import ru.wlwidget.widget.StringStore
import ru.wlwidget.widget.WidgetStateStore
import java.time.Instant

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val listSource = AssetListSource(appContext)
    val pipeline = RefreshPipeline(
        network = AndroidNetworkStatus(appContext),
        lists = listSource,
        engine = ProbeEngine(HttpsGetTransport(), Dispatchers.IO),
        clock = Clock { Instant.now() },
    )
    val stateStore = WidgetStateStore(
        object : StringStore {
            private val prefs = appContext.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
            override fun read(): String? = prefs.getString(KEY, null)
            override fun write(value: String?) {
                prefs.edit().putString(KEY, value).apply()
            }
        },
    )
    val autoRefresh = AutoRefreshController(appContext)

    private companion object {
        const val KEY = "last"
    }
}

class WlApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        if (AutoRefreshController.widgetsExist(this)) {
            container.autoRefresh.start()
        }
    }
}
