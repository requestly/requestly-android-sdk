package io.requestly.android.okhttp.internal.support

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder


internal class ClearNotificationService : Service() {
    class KillBinder(val service: Service) : Binder()

    private var mNotificationManager: NotificationManager? = null
    private val mBinder: IBinder = KillBinder(this)

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder;
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager!!.cancel(MetadataNotificationHelper.NOTIFICATION_ID)
    }

}
