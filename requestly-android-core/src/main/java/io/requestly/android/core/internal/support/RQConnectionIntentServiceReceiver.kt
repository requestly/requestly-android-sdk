package io.requestly.android.core.internal.support

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// TODO: Improve This
internal class RQConnectionIntentServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        RQConnectionService.enqueueWork(context, intent)
    }
}
