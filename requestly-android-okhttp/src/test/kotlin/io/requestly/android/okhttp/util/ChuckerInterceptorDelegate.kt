package io.requestly.android.okhttp.util

import android.content.Context
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.api.RQCollector
import io.requestly.android.okhttp.api.RQInterceptor
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.support.CacheDirectoryProvider
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

internal class ChuckerInterceptorDelegate(
    maxContentLength: Long = 250000L,
    headersToRedact: Set<String> = emptySet(),
    alwaysReadResponseBody: Boolean = false,
    cacheDirectoryProvider: CacheDirectoryProvider,
    decoders: List<io.requestly.android.okhttp.api.BodyDecoder> = emptyList(),
) : Interceptor {
    private val idGenerator = AtomicLong()
    private val transactions = CopyOnWriteArrayList<HttpTransaction>()

    private val mockContext = mockk<Context> {
        every { getString(R.string.rq_interceptor_body_content_truncated) } returns "\n\n--- Content truncated ---"
    }
    private val mockCollector = mockk<RQCollector> {
        every { onRequestSent(any()) } returns Unit
        every { onResponseReceived(any()) } answers {
            val transaction = (args[0] as HttpTransaction)
            transaction.id = idGenerator.getAndIncrement()
            transactions.add(transaction)
        }
    }

    private val chucker = RQInterceptor.Builder(context = mockContext)
        .collector(mockCollector)
        .maxContentLength(maxContentLength)
        .redactHeaders(headersToRedact)
        .alwaysReadResponseBody(alwaysReadResponseBody)
        .cacheDirectorProvider(cacheDirectoryProvider)
        .apply { decoders.forEach(::addBodyDecoder) }
        .build()

    internal fun expectTransaction(): HttpTransaction {
        if (transactions.isEmpty()) {
            throw AssertionError("Expected transaction but was empty")
        }
        return transactions.removeAt(0)
    }

    internal fun expectNoTransactions() {
        if (transactions.isNotEmpty()) {
            throw AssertionError("Expected no transactions but found ${transactions.size}")
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chucker.intercept(chain)
    }
}
