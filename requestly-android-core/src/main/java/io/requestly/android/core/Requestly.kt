package io.requestly.android.core

import android.app.Application
import android.content.Context
import android.util.Log
import io.requestly.android.core.internal.support.ListNotificationHelper
import io.requestly.android.core.modules.logs.lib.lynx.main.LynxConfig
import io.requestly.android.core.modules.logs.lib.lynx.main.model.*
import io.requestly.android.core.modules.logs.lib.lynx.main.presenter.LynxRequestlyPresenter

class Requestly {
    companion object {
        private lateinit var INSTANCE: Requestly

        @JvmStatic
        fun getInstance(): Requestly {
            return INSTANCE
        }
    }

    lateinit var applicationContext: Context
    lateinit var listNotificationHelper: ListNotificationHelper

    // Logs Helpers
    var logsConfig: LynxConfig = LynxConfig()
    lateinit var logsLynx: Lynx
    lateinit var logsLynxPresenter: LynxRequestlyPresenter

    class Builder(
        private val application: Application,
        private val appToken: String,
    ) {
        private var applicationContext: Context = application.applicationContext

        // Start Configuration: Different Configurations of Builder
        private var networkLoggerUIState = true

        // module/logs
        private var logsConfig = LynxConfig()

        // End Configuration

        fun build() {
            Log.d("RQ-Core", "Start: Building Core")
            INSTANCE = Requestly()
            getInstance()?.applicationContext = applicationContext
            getInstance()?.listNotificationHelper = ListNotificationHelper(applicationContext)

            SettingsManager.getInstance().setAppToken(appToken)
            KeyValueStorageManager.initialize(applicationContext)
            this.updateFeaturesState()

            buildLogsModule()
            Log.d("RQ-Core", "Finish: Building Core")
        }

        // TODO: @wrongSahil
        fun buildLogsModule() {
            // LynxConfig init
            // Lynx init
            // LynxRequestlyPresenter init
            val lynx = Lynx(Logcat(), AndroidMainThread(), TimeProvider())
            lynx.config = logsConfig
            val presenter = LynxRequestlyPresenter(lynx, logsConfig.maxNumberOfTracesToShow)
            presenter.resume()

            getInstance().logsConfig = logsConfig
            getInstance().logsLynx = lynx
            getInstance().logsLynxPresenter = presenter
        }

        private fun updateFeaturesState() {
            Log.d("RQ-Core", "Start: Updating Features")
            SettingsManager.getInstance().setFeatureState(Feature.NETWORK_LOGGER_UI, this.networkLoggerUIState)
            Log.d("RQ-Core", "End: Updating Features")
        }

        fun setNetworkLoggerUIState(visible: Boolean = true): Builder {
            this.networkLoggerUIState = visible
            return this
        }

        fun setLoggerConfig(
            maxNumberOfTracesToShow: Int = 2500,
            filter: String? = null,
            filterTraceLevel: TraceLevel? = null,
            textSizeInPx: Float = 36F,
            samplingRate: Int = 150): Builder
        {
            logsConfig.maxNumberOfTracesToShow = maxNumberOfTracesToShow
            logsConfig.filter = filter
            logsConfig.filterTraceLevel = filterTraceLevel
            logsConfig.textSizeInPx = textSizeInPx
            logsConfig.samplingRate = samplingRate
            return this
        }
    }
}
