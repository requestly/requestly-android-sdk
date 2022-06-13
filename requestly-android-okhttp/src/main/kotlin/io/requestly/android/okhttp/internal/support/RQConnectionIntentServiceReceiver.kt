package io.requestly.android.okhttp.internal.support

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class RQConnectionIntentServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        RQConnectionService.enqueueWork(context, intent)
    }
}
