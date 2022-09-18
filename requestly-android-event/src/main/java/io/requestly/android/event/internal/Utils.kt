package io.requestly.android.event.internal

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class Utils private constructor() {
    companion object {
        private var requestCode = 8281

        fun getDefaultScreenIntent(context: Context): PendingIntent {
            return PendingIntent.getActivity(
                context,
                requestCode, // @wrongsahil: Check why this is useful?
                Utils.getLaunchIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
        }

        fun getLaunchIntent(context: Context): Intent {
            return Intent(context, io.requestly.android.core.ui.MainRequestlyActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("STARTING_FLOW", "ANALYTICS")
        }

        private fun immutableFlag() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
