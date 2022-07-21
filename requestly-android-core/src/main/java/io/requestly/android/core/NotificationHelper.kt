package io.requestly.android.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationHelper(val context: Context, private val CHANNEL_ID:String, private val NotificationID:Int, private val CHANNEL_NAME:String) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
    fun show(title:String, message:String,) : NotificationCompat.Builder{
            val builder =
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setLocalOnly(true)
                    .setColor(ContextCompat.getColor(context, R.color.rq_interceptor_color_primary))
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.rq_interceptor_ic_transaction_notification)
                    .setAutoCancel(true)
        return builder
        }

    fun dismissNotifications() {
        notificationManager.cancel(NotificationID)
    }

    }


