package io.requestly.rqinterceptor.internal.support

import android.content.*
import android.widget.Toast
import io.requestly.rqinterceptor.api.RQClientProvider


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
