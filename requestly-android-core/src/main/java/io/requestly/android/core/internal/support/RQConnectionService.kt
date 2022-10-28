package io.requestly.android.core.internal.support

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import io.requestly.android.core.SettingsManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// TODO: Improve this
internal class RQConnectionService : JobIntentService() {
    private val scope = MainScope()

    override fun onHandleWork(intent: Intent) {
        scope.launch {
            val oldCaptureStatus = SettingsManager.getInstance().getCaptureEnabled()
            val newCaptureStatus = !oldCaptureStatus
            SettingsManager.getInstance().setCaptureEnabled(newCaptureStatus)

            MetadataNotificationHelper(applicationContext).show(
                SettingsManager.getInstance().getUniqueDeviceId(),
                SettingsManager.getInstance().getCaptureEnabled()
            )
        }
    }

    companion object {
        private const val RQ_CONNECTION_JOB_ID = 123322

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, RQConnectionService::class.java, RQ_CONNECTION_JOB_ID, work)
        }
    }
}
