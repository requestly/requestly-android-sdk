package io.requestly.android.core.internal.support

import android.content.Context
import android.content.Intent
import io.requestly.android.core.ui.MainRequestlyActivity

class Utils {
    companion object {
        fun getMainActivityLaunchIntent(context: Context): Intent {
            return Intent(context, MainRequestlyActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
