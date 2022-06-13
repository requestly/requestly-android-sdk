package io.requestly.android.okhttp.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.requestly.android.core.SettingsManager
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.internal.RQConstants
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.data.repository.RepositoryProvider
import io.requestly.android.okhttp.internal.support.MetadataNotificationHelper
import io.requestly.android.okhttp.internal.support.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The collector responsible of collecting data from a [RQInterceptor] and
 * storing it/displaying push notification. You need to instantiate one of those and
 * provide it to
 *
 * @param context An Android Context
 * @param showNotification Control whether a notification is shown while HTTP activity
 * is recorded.
 * @param retentionPeriod Set the retention period for HTTP transaction data captured
 * by this collector. The default is one week.
 */
public class RQCollector @JvmOverloads constructor(
    context: Context,
    public val sdkKey: String,
    public var uniqueDeviceId: String? = null,
    public var showNotification: Boolean = true,
    retentionPeriod: RetentionManager.Period = RetentionManager.Period.ONE_WEEK
) {
    private val retentionManager: RetentionManager = RetentionManager(context, retentionPeriod)
    private val notificationHelper: NotificationHelper = NotificationHelper(context)
    private val metadataNotificationHelper: MetadataNotificationHelper = MetadataNotificationHelper(context)
    private val scope = MainScope()

    init {
        RepositoryProvider.initialize(context)
        this.uniqueDeviceId = getUniqueDeviceId(context)

        var captureEnabled = getCaptureEnabled(context)
        Log.d(RQConstants.LOG_TAG, "CaptureEnabled Before init: $captureEnabled")
        Log.d(RQConstants.LOG_TAG, "applicationToken from core ${SettingsManager.getInstance().getAppToken()}")
        RQClientProvider.initialize(uniqueDeviceId, sdkKey, captureEnabled)
        scope.launch {
            var deviceId = RQClientProvider.client().initDevice()
            if(uniqueDeviceId == null) {
                setUniqueDeviceId(context, deviceId)
            }

            captureEnabled = getCaptureEnabled(context)
            Log.d(RQConstants.LOG_TAG, "CaptureEnabled after init: $captureEnabled")
            RQClientProvider.client().captureEnabled = captureEnabled

            metadataNotificationHelper.show(uniqueDeviceId, RQClientProvider.client().captureEnabled)
        }
    }

    private fun getCaptureEnabled(context: Context): Boolean {
        // When deviceId is not yet initialized on server side
        if(uniqueDeviceId == null) {
            Log.d(RQConstants.LOG_TAG, "DeviceId not initialized yet")
            return false
        }
        val pref: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.rq_interceptor_shared_pref_base_key), 0
        )
        return pref.getBoolean(
            context.getString(R.string.rq_interceptor_capture_enabled), true
        )
    }

    private fun getUniqueDeviceId(context: Context): String? {
        val pref: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.rq_interceptor_shared_pref_base_key),
            0
        )
        var deviceId =  pref.getString(context.getString(R.string.rq_interceptor_unique_device_id_key), null)
        Log.d(RQConstants.LOG_TAG, "deviceId get from cache: $deviceId")
        return deviceId
    }

    private fun setUniqueDeviceId(context: Context, deviceId: String?) {
        val pref: SharedPreferences = context.getSharedPreferences(
            context.getString(R.string.rq_interceptor_shared_pref_base_key),
            0
        )
        val editor = pref.edit()
        editor.putString(context.getString(R.string.rq_interceptor_unique_device_id_key), deviceId)
        editor.apply()
        this.uniqueDeviceId = deviceId
        Log.d(RQConstants.LOG_TAG, "deviceId set in cache $deviceId")
    }

    /**
     * Call this method when you send an HTTP request.
     * @param transaction The HTTP transaction sent
     */
    internal fun onRequestSent(transaction: HttpTransaction) {
        scope.launch {
            RepositoryProvider.transaction().insertTransaction(transaction)

            if (showNotification) {
                notificationHelper.show(transaction)
            }
            withContext(Dispatchers.IO) {
                retentionManager.doMaintenance()
            }
        }
    }

    /**
     * Call this method when you received the response of an HTTP request.
     * It must be called after [RQCollector.onRequestSent].
     * @param transaction The sent HTTP transaction completed with the response
     */
    internal fun onResponseReceived(transaction: HttpTransaction) {
        scope.launch {
            val updated = RepositoryProvider.transaction().updateTransaction(transaction)
            if (showNotification && updated > 0) {
                notificationHelper.show(transaction)
            }

            // TODO: This should be moved to server side
//            RQClientProvider.client().addLog(transaction)
        }
    }
}
