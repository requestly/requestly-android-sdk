package io.requestly.android.okhttp.internal.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.internal.ui.BaseRequestlyNetworkFragment

internal class MetadataNotificationHelper(val context: Context) {

    companion object {
        private const val CHANNEL_ID = "rq_interceptor_metadata"
        public const val NOTIFICATION_ID = 1139
        private const val INTENT_REQUEST_CODE = 11
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val transactionsChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.rq_interceptor_metadata_notification_category),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannels(listOf(transactionsChannel))
        }
    }

    private fun createConnectionAction(capturingEnabled: Boolean):
        NotificationCompat.Action {
        val connectTitle = context.getString(
            if(capturingEnabled) R.string.rq_interceptor_disconnect else R.string.rq_interceptor_connect
        )
        val connectBroadcastIntent =
            Intent(context, RQConnectionIntentServiceReceiver::class.java)
        val pendingBroadcastIntent = PendingIntent.getBroadcast(
            context,
            INTENT_REQUEST_CODE,
            connectBroadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
        return NotificationCompat.Action(
            R.drawable.rq_interceptor_ic_transaction_notification,
            connectTitle,
            pendingBroadcastIntent
        )
    }

    private fun createCopyDeviceIdAction():
        NotificationCompat.Action {
        val copyBroadcastIntent =
            Intent(context, CopyIntentServiceReceiver::class.java)
        val pendingBroadcastIntent = PendingIntent.getBroadcast(
            context,
            INTENT_REQUEST_CODE,
            copyBroadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
        return NotificationCompat.Action(
            R.drawable.rq_interceptor_ic_transaction_notification,
            "Copy DeviceId",
            pendingBroadcastIntent
        )
    }


    fun show(uid: String?, capturingEnabled: Boolean = false) {
        if (!BaseRequestlyNetworkFragment.isInForeground) {
            val builder =
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.rq_interceptor_ic_transaction_notification)
                    .setColor(ContextCompat.getColor(context, R.color.rq_interceptor_color_primary))
                    .setContentTitle("DeviceId: $uid")
                    .addAction(createCopyDeviceIdAction())
                    .addAction(createConnectionAction(capturingEnabled))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPriority(NotificationManager.IMPORTANCE_HIGH)
            }

            builder.setContentText("Capturing: $capturingEnabled")
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun immutableFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
}
