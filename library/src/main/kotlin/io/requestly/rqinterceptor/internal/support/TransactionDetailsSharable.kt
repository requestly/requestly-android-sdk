package io.requestly.rqinterceptor.internal.support

import android.content.Context
import io.requestly.rqinterceptor.R
import io.requestly.rqinterceptor.internal.data.entity.HttpTransaction
import okio.Buffer
import okio.Source

internal class TransactionDetailsSharable(
    private val transaction: HttpTransaction,
    private val encodeUrls: Boolean,
) : Sharable {
    override fun toSharableContent(context: Context): Source = Buffer().apply {
        writeUtf8("${context.getString(R.string.rq_interceptor_url)}: ${transaction.getFormattedUrl(encodeUrls)}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_method)}: ${transaction.method}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_protocol)}: ${transaction.protocol}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_status)}: ${transaction.status}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_response)}: ${transaction.responseSummaryText}\n")
        val isSsl = if (transaction.isSsl) R.string.rq_interceptor_yes else R.string.rq_interceptor_no
        writeUtf8("${context.getString(R.string.rq_interceptor_ssl)}: ${context.getString(isSsl)}\n")
        writeUtf8("\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_request_time)}: ${transaction.requestDateString}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_response_time)}: ${transaction.responseDateString}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_duration)}: ${transaction.durationString}\n")
        writeUtf8("\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_request_size)}: ${transaction.requestSizeString}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_response_size)}: ${transaction.responseSizeString}\n")
        writeUtf8("${context.getString(R.string.rq_interceptor_total_size)}: ${transaction.totalSizeString}\n")
        writeUtf8("\n")
        writeUtf8("---------- ${context.getString(R.string.rq_interceptor_request)} ----------\n\n")

        var headers = FormatUtils.formatHeaders(transaction.getParsedRequestHeaders(), false)

        if (headers.isNotBlank()) {
            writeUtf8(headers)
            writeUtf8("\n")
        }

        writeUtf8(
            if (transaction.requestBody.isNullOrBlank()) {
                val resId = if (transaction.isResponseBodyEncoded) {
                    R.string.rq_interceptor_body_omitted
                } else {
                    R.string.rq_interceptor_body_empty
                }
                context.getString(resId)
            } else {
                transaction.getFormattedRequestBody()
            }
        )

        writeUtf8("\n\n")
        writeUtf8("---------- ${context.getString(R.string.rq_interceptor_response)} ----------\n\n")

        headers = FormatUtils.formatHeaders(transaction.getParsedResponseHeaders(), false)

        if (headers.isNotBlank()) {
            writeUtf8(headers)
            writeUtf8("\n")
        }

        writeUtf8(
            if (transaction.responseBody.isNullOrBlank()) {
                val resId = if (transaction.isResponseBodyEncoded) {
                    R.string.rq_interceptor_body_omitted
                } else {
                    R.string.rq_interceptor_body_empty
                }
                context.getString(resId)
            } else {
                transaction.getFormattedResponseBody()
            }
        )
    }
}
