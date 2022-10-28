package io.requestly.android.core.internal.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.requestly.android.core.Constants
import io.requestly.android.core.R
import io.requestly.android.core.SettingsManager

class MetadataNotificationHelper(val context: Context) {

    companion object {
        private const val CHANNEL_ID = "rq_interceptor_metadata"
        const val NOTIFICATION_ID = 1139
        private const val INTENT_REQUEST_CODE = 11
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val homeScreenIntent by lazy {
        PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Utils.getMainActivityLaunchIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val transactionsChannel = NotificationChannel(
                CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannels(listOf(transactionsChannel))
        }
    }

    private fun createConnectionAction(capturingEnabled: Boolean):
        NotificationCompat.Action {
        if(!SettingsManager.getInstance().getIsAnonymousSession()) {
            val connectTitle =
                if (capturingEnabled) Constants.NOTIFICATION_DISCONNECT_TEXT else Constants.NOTIFICATION_CONNECT_TEXT
            val connectBroadcastIntent =
                Intent(context, RQConnectionIntentServiceReceiver::class.java)
            val pendingBroadcastIntent = PendingIntent.getBroadcast(
                context,
                INTENT_REQUEST_CODE,
                connectBroadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
            return NotificationCompat.Action(
                R.drawable.ic_requestly_24,
                connectTitle,
                pendingBroadcastIntent
            )
        } else {
            val title = Constants.NOTIFICATION_CONNECT_UNAVAILABLE_TEXT
            return NotificationCompat.Action(
                R.drawable.ic_requestly_24,
                title,
                null
            )
        }
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
            R.drawable.ic_requestly_24,
            "Copy DeviceId",
            pendingBroadcastIntent
        )
    }


    fun show(uid: String?, capturingEnabled: Boolean = false) {
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(homeScreenIntent)
                .setSmallIcon(R.drawable.ic_requestly_24)
                .setColor(ContextCompat.getColor(context, R.color.requestly_primary))
                .setContentTitle("DeviceId: $uid")
                .addAction(createCopyDeviceIdAction())
                .addAction(createConnectionAction(capturingEnabled))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH)
        }

        builder.setContentText("Capturing: $capturingEnabled")
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun immutableFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
}
