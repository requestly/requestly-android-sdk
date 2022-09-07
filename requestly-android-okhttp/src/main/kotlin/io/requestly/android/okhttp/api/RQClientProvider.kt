package io.requestly.android.okhttp.api

import androidx.annotation.VisibleForTesting
import io.requestly.android.okhttp.api.RQClientProvider.initialize

/**
 * A singleton to hold the [RQClient] instance.
 * Make sure you call [initialize] before accessing the stored instance.
 */
internal object RQClientProvider {

    private var rqClient: RQClient? = null

    fun client(): RQClient {
        return checkNotNull(rqClient) {
            "You can't access the client if you don't initialize it!"
        }
    }

    /**
     * Idempotent method. Must be called before accessing the client.
     */
    fun initialize(deviceId: String?, sdkId: String, captureEnabled: Boolean = false) {
        if (rqClient == null) {
            rqClient = RQClient(deviceId, sdkId, captureEnabled)
        }
    }

    /**
     * Cleanup stored singleton objects
     */
    @VisibleForTesting
    fun close() {
        rqClient = null
    }
}
