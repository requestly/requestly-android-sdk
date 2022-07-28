package io.requestly.android.core.internal.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.requestly.android.core.R
import java.util.HashSet

class ListNotificationHelper(val context: Context) {

    companion object {
        private const val CHANNEL_ID = "requestly_mobile_debugger"

        private const val EVENT_NOTIFICATION_ID = 1140

        private const val BUFFER_SIZE = 10
        private val notificationBuffer = LongSparseArray<String>()
        private val notificationIdsSet = HashSet<Long>()

        fun clearBuffer() {
            synchronized(notificationBuffer) {
                notificationBuffer.clear()
                notificationIdsSet.clear()
            }
        }
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//    private val transactionsScreenIntent by lazy {
//        PendingIntent.getActivity(
//            context,
//            TRANSACTION_NOTIFICATION_ID,
//            io.requestly.android.okhttp.api.RQ.getLaunchIntent(context),
//            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
//        )
//    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val transactionsChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_category_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannels(listOf(transactionsChannel))
        }
    }

    private fun addToBuffer(notificationText: String, notificationTextId: Long) {
        if (notificationTextId == 0L) {
            // Don't store Transactions with an invalid ID (0).
            // Transaction with an Invalid ID will be shown twice in the notification
            // with both the invalid and the valid ID and we want to avoid this.
            return
        }
        synchronized(notificationBuffer) {
            notificationIdsSet.add(notificationTextId)
            notificationBuffer.put(notificationTextId, notificationText)
            if (notificationBuffer.size() > BUFFER_SIZE) {
                notificationBuffer.removeAt(0)
            }
        }
    }

    // Inherit this class and pass entity here instead of passing String and id
    fun show(notificationText: String, id: Long, intent: PendingIntent, notificationTitle: String) {
        addToBuffer(notificationText, id)
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(intent)
                .setLocalOnly(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(ContextCompat.getColor(context, R.color.color_primary))
                .setContentTitle(notificationTitle)
                .setAutoCancel(true)
//                    .addAction(createClearAction())
        val inboxStyle = NotificationCompat.InboxStyle()
        synchronized(notificationBuffer) {
            var count = 0
            (notificationBuffer.size() - 1 downTo 0).forEach { i ->
                val bufferedTransaction = notificationBuffer.valueAt(i)
                if ((bufferedTransaction != null) && count < BUFFER_SIZE) {
                    if (count == 0) {
                        builder.setContentText(bufferedTransaction)
                    }
                    inboxStyle.addLine(bufferedTransaction)
                }
                count++
            }
            builder.setStyle(inboxStyle)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setSubText(notificationIdsSet.size.toString())
            } else {
                builder.setNumber(notificationIdsSet.size)
            }
        }
        notificationManager.notify(EVENT_NOTIFICATION_ID, builder.build())
    }

    fun dismissNotifications() {
        notificationManager.cancel(EVENT_NOTIFICATION_ID)
    }

// RQLY-1 Create clear action to delete all the logs in the DB
//    private fun createClearAction():
//        NotificationCompat.Action {
//        val clearTitle = context.getString(R.string.rq_interceptor_clear)
//        val clearTransactionsBroadcastIntent =
//            Intent(context, ClearDatabaseJobIntentServiceReceiver::class.java)
//        val pendingBroadcastIntent = PendingIntent.getBroadcast(
//            context,
//            INTENT_REQUEST_CODE,
//            clearTransactionsBroadcastIntent,
//            PendingIntent.FLAG_ONE_SHOT or immutableFlag()
//        )
//        return NotificationCompat.Action(
//            R.drawable.rq_interceptor_ic_delete_white,
//            clearTitle,
//            pendingBroadcastIntent
//        )
//    }
//
//    private fun immutableFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        PendingIntent.FLAG_IMMUTABLE
//    } else {
//        0
//    }
}
