package io.requestly.android.sample

import android.app.Application
import io.requestly.android.core.Requestly


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Requestly.Builder(this, "test-token")
            .setNetworkLoggerUIState(true)
            .build()

    }
}
