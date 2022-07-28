package io.requestly.android.okhttp.internal.support

import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import io.requestly.android.okhttp.api.RQClientProvider


internal class CopyIntentServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val clipboard: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val deviceId: String? = RQClientProvider.client().deviceId
        val clip = ClipData.newPlainText("rq device id", deviceId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }
}
