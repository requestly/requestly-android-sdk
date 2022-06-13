package io.requestly.android.okhttp.internal.support

internal interface Logger {
    fun info(message: String, throwable: Throwable? = null)

    fun warn(message: String, throwable: Throwable? = null)

    fun error(message: String, throwable: Throwable? = null)

    companion object : Logger {
        override fun info(message: String, throwable: Throwable?) {
            io.requestly.android.okhttp.api.RQ.logger.info(message, throwable)
        }

        override fun warn(message: String, throwable: Throwable?) {
            io.requestly.android.okhttp.api.RQ.logger.warn(message, throwable)
        }

        override fun error(message: String, throwable: Throwable?) {
            io.requestly.android.okhttp.api.RQ.logger.error(message, throwable)
        }
    }
}
