package io.requestly.android.event.internal

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import io.requestly.android.core.Requestly
import io.requestly.android.core.RequestlyLaunchFlow

class Utils private constructor() {
    companion object {
        private var requestCode = 8281

        fun getDefaultScreenIntent(context: Context): PendingIntent {
            return PendingIntent.getActivity(
                context,
                requestCode, // @wrongsahil: Check why this is useful?
                Requestly.getLaunchIntent(context, RequestlyLaunchFlow.ANALYTICS),
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
        }

        private fun immutableFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
