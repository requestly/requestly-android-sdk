package io.requestly.android.okhttp.api

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.support.CacheDirectoryProvider
import io.requestly.android.okhttp.internal.support.PlainTextDecoder
import io.requestly.android.okhttp.internal.support.RequestProcessor
import io.requestly.android.okhttp.internal.support.ResponseProcessor
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * An OkHttp Interceptor which persists and displays HTTP activity
 * in your application for later inspection.
 */
public class RQInterceptor private constructor(
    builder: Builder,
) : Interceptor {

    /**
     * An OkHttp Interceptor which persists and displays HTTP activity
     * in your application for later inspection.
     *
     * This constructor  is a shorthand for `RQInterceptor.Builder(context).build()`.
     *
     * @param context An Android [Context]
     * @see RQInterceptor.Builder
     */
    public constructor(context: Context) : this(Builder(context))

    private val headersToRedact = builder.headersToRedact.toMutableSet()

    private val decoders = builder.decoders + BUILT_IN_DECODERS

    private val collector = builder.collector ?: RQCollector(builder.context, "")

    private val requestProcessor = RequestProcessor(
        builder.context,
        collector,
        builder.maxContentLength,
        headersToRedact,
        decoders,
    )

    private val responseProcessor = ResponseProcessor(
        collector,
        builder.cacheDirectoryProvider ?: CacheDirectoryProvider { builder.context.filesDir },
        builder.maxContentLength,
        headersToRedact,
        builder.alwaysReadResponseBody,
        decoders,
    )

    init {
        if (builder.createShortcut) {
            RQ.createShortcut(builder.context)
        }
    }

    /** Adds [headerName] into [headersToRedact] */
    public fun redactHeader(vararg headerName: String) {
        headersToRedact.addAll(headerName)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val transaction = HttpTransaction()
        var request = chain.request()

//        request = requestProcessor.process(request, transaction)
        request = requestProcessor.process(request, transaction)

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            transaction.error = e.toString()
            collector.onResponseReceived(transaction)
            throw e
        }

        return responseProcessor.process(response, transaction)
    }

    /**
     * Assembles a new [RQInterceptor].
     *
     * @param context An Android [Context].
     */
    public class Builder(internal var context: Context) {
        internal var collector: RQCollector? = null
        internal var maxContentLength = MAX_CONTENT_LENGTH
        internal var cacheDirectoryProvider: CacheDirectoryProvider? = null
        internal var alwaysReadResponseBody = false
        internal var headersToRedact = emptySet<String>()
        internal var decoders = emptyList<BodyDecoder>()
        internal var createShortcut = true

        /**
         * Sets the [RQCollector] to customize data retention.
         */
        public fun collector(collector: RQCollector): Builder = apply {
            this.collector = collector
        }

        /**
         * Sets the maximum length for requests and responses content before their truncation.
         *
         * Warning: setting this value too high may cause unexpected results.
         */
        public fun maxContentLength(length: Long): Builder = apply {
            this.maxContentLength = length
        }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the RQ-Interceptor UI.
         */
        public fun redactHeaders(headerNames: Iterable<String>): Builder = apply {
            this.headersToRedact = headerNames.toSet()
        }

        /**
         * Sets headers that will be redacted if their names match.
         * They will be replaced with the `**` symbols in the RQ-Interceptor UI.
         */
        public fun redactHeaders(vararg headerNames: String): Builder = apply {
            this.headersToRedact = headerNames.toSet()
        }

        /**
         * If set to `true` [RQInterceptor] will read full content of response
         * bodies even in case of parsing errors or closing the response body without reading it.
         *
         * Warning: enabling this feature may potentially cause different behaviour from the
         * production application.
         */
        public fun alwaysReadResponseBody(enable: Boolean): Builder = apply {
            this.alwaysReadResponseBody = enable
        }

        /**
         * Adds a [decoder] into RQ-Interceptor's processing pipeline. Decoders are applied in an order they
         * were added in. Request and response bodies are set to the first non–null value returned by any of
         * the decoders.
         */
        public fun addBodyDecoder(decoder: io.requestly.android.okhttp.api.BodyDecoder): Builder = apply {
            this.decoders += decoder
        }

        /**
         * If set to `true`, [RQInterceptor] will create a shortcut for your app
         * to access list of transaction in RQ-Interceptor.
         */
        public fun createShortcut(enable: Boolean): Builder = apply {
            this.createShortcut = enable
        }

        /**
         * Sets provider of a directory where RQ-Interceptor will save temporary responses
         * before processing them.
         */
        @VisibleForTesting
        internal fun cacheDirectorProvider(provider: CacheDirectoryProvider): Builder = apply {
            this.cacheDirectoryProvider = provider
        }

        /**
         * Creates a new [RQInterceptor] instance with values defined in this builder.
         */
        public fun build(): RQInterceptor = RQInterceptor(this)
    }

    private companion object {
        private const val MAX_CONTENT_LENGTH = 250_000L

        private val BUILT_IN_DECODERS = listOf(PlainTextDecoder)
    }
}
