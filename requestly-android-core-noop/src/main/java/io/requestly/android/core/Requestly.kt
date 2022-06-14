package io.requestly.android.core

import android.app.Application
import android.util.Log


@Suppress("UnusedPrivateMember")
public class Requestly {
    public class Builder(
        private val application: Application,
        private val appToken: String,
    ) {
        public fun build () {
            Log.d("RQ-Core-noop", "Building Core - Noop")
        }

        public fun setNetworkLoggerUIState (visible: Boolean = true): Builder {
            return Builder(application, appToken)
        }
    }
}
