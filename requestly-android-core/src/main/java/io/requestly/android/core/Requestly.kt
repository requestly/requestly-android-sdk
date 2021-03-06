package io.requestly.android.core

import android.app.Application
import android.content.Context
import android.util.Log
import io.requestly.android.core.internal.support.ListNotificationHelper

class Requestly {
    companion object {
        private lateinit var INSTANCE: Requestly

        fun getInstance(): Requestly? {
            return INSTANCE
        }
    }

    lateinit var applicationContext: Context
    lateinit var listNotificationHelper: ListNotificationHelper

    class Builder(
        private val application: Application,
        private val appToken: String,
    ) {
        private var applicationContext: Context = application.applicationContext

        // Start Configuration: Different Configurations of Builder
        private var networkLoggerUIState = true
        // End Configuration

        fun build() {
            Log.d("RQ-Core", "Start: Building Core")
            Requestly.INSTANCE = Requestly()
            Requestly.getInstance()?.applicationContext = applicationContext
            Requestly.getInstance()?.listNotificationHelper = ListNotificationHelper(applicationContext)

            SettingsManager.getInstance().setAppToken(appToken)
            this.updateFeaturesState()
            Log.d("RQ-Core", "Finish: Building Core")
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
    }
}
