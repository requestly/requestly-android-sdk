package io.requestly.android.okhttp.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * No-op implementation.
 */
@Suppress("UnusedPrivateMember")
public class RQInterceptor private constructor(
    builder: Builder,
) : Interceptor {

    /**
     * No-op implementation.
     */
    public constructor(context: Context) : this(Builder(context))

    public fun redactHeaders(vararg names: String): RQInterceptor {
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }

    /**
     * No-op implementation.
     */
    public class Builder(private val context: Context) {
        public fun collector(collector: RQCollector): Builder = this

        public fun maxContentLength(length: Long): Builder = this

        public fun redactHeaders(headerNames: Iterable<String>): Builder = this

        public fun redactHeaders(vararg headerNames: String): Builder = this

        public fun alwaysReadResponseBody(enable: Boolean): Builder = this

        public fun addBodyDecoder(decoder: Any): Builder = this

        public fun createShortcut(enable: Boolean): Builder = this

        public fun build(): RQInterceptor = RQInterceptor(this)
    }
}
