package io.requestly.android.okhttp.internal.support

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.JobIntentService
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.api.RQClientProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class RQConnectionService : JobIntentService() {
    private val scope = MainScope()

    override fun onHandleWork(intent: Intent) {
        scope.launch {
            // Capture Enabled Changed in RQClient
            Log.d("Rq", "RQConnectionService")
            var captureStatus: String = RQClientProvider.client().captureEnabled.toString()
            Log.d("Rq", "Old status $captureStatus")

            RQClientProvider.client().updateCaptureEnabled(!RQClientProvider.client().captureEnabled)
            captureStatus = RQClientProvider.client().captureEnabled.toString()
            Log.d("Rq", "New status $captureStatus")

            MetadataNotificationHelper(applicationContext).show(
                RQClientProvider.client().deviceId, RQClientProvider.client().captureEnabled
            )

            // Persist Capture Enabled in Shared Preferences
            val pref: SharedPreferences = applicationContext.getSharedPreferences(
                applicationContext.getString(R.string.rq_interceptor_shared_pref_base_key), 0
            )
            val editor = pref.edit()
            editor.putBoolean(
                applicationContext.getString(R.string.rq_interceptor_capture_enabled),
                RQClientProvider.client().captureEnabled
            )
            editor.apply()
        }
    }

    companion object {
        private const val RQ_CONNECTION_JOB_ID = 123322

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, RQConnectionService::class.java, RQ_CONNECTION_JOB_ID, work)
        }
    }
}
