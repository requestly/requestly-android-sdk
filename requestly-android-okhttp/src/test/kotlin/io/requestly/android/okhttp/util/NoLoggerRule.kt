package io.requestly.android.okhttp.util

import io.requestly.android.okhttp.internal.support.Logger
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

internal class NoLoggerRule : BeforeAllCallback, AfterAllCallback {
    private val defaultLogger = io.requestly.android.okhttp.api.RQ.logger

    override fun beforeAll(context: ExtensionContext) {
        io.requestly.android.okhttp.api.RQ.logger = object : Logger {
            override fun info(message: String, throwable: Throwable?) = Unit

            override fun warn(message: String, throwable: Throwable?) = Unit

            override fun error(message: String, throwable: Throwable?) = Unit
        }
    }

    override fun afterAll(context: ExtensionContext) {
        io.requestly.android.okhttp.api.RQ.logger = defaultLogger
    }
}
