package io.requestly.android.okhttp.internal.support

import android.content.Context
import android.util.Log
import io.requestly.android.okhttp.R
import io.requestly.android.okhttp.api.BodyDecoder
import io.requestly.android.okhttp.api.RQClientProvider
import io.requestly.android.okhttp.api.RQCollector
import io.requestly.android.okhttp.internal.RQConstants
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.ByteString
import okio.IOException


internal class RequestProcessor(
    private val context: Context,
    private val collector: RQCollector,
    private val maxContentLength: Long,
    private val headersToRedact: Set<String>,
    private val bodyDecoders: List<BodyDecoder>,
) {
    fun process(request: Request, transaction: HttpTransaction): Request {
        processMetadata(request, transaction)
        processPayload(request, transaction)

        if(!RQClientProvider.client().captureEnabled){
            Log.d(RQConstants.LOG_TAG, "Capturing Not enabled. Passing through requests")
            collector.onRequestSent(transaction)
            return request
        }

        val JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json: String = HarUtils.harStringFromTransaction(
            listOf(transaction),
            "RQInterceptor",
            "0.0.1"
        )
        val body: RequestBody = json.toRequestBody(JSON)
        val newRequest: Request = request.newBuilder()
            .method("POST", body)
            .header("content-type", "application/json")
            .header("rq_device_id", collector.uniqueDeviceId?:"")
            .header("rq_sdk_id", collector.sdkKey)
            .url("${RQConstants.RQ_SERVER_BASE_URL}/${RQConstants.PROXY_REQUEST_PATH}")
            .build()

//        Test Actual POST Request sent to rq server
//        processMetadata(newRequest, transaction)
//        processPayload(newRequest, transaction)
        collector.onRequestSent(transaction)
        return newRequest
    }

    private fun processMetadata(request: Request, transaction: HttpTransaction) {
        transaction.apply {
            requestHeadersSize = request.headers.byteCount()
            setRequestHeaders(request.headers.redact(headersToRedact))
            populateUrl(request.url)

            requestDate = System.currentTimeMillis()
            method = request.method
            requestContentType = request.body?.contentType()?.toString()
            requestPayloadSize = request.body?.contentLength()
        }
    }

    private fun processPayload(request: Request, transaction: HttpTransaction) {
        val body = request.body ?: return
        if (body.isOneShot()) {
            Logger.info("Skipping one shot request body")
            return
        }
        if (body.isDuplex()) {
            Logger.info("Skipping duplex request body")
            return
        }

        val requestSource = try {
            Buffer().apply { body.writeTo(this) }
        } catch (e: IOException) {
            Logger.error("Failed to read request payload", e)
            return
        }
        val limitingSource = LimitingSource(requestSource.uncompress(request.headers), maxContentLength)

        val contentBuffer = Buffer().apply { limitingSource.use { writeAll(it) } }

        val decodedContent = decodePayload(request, contentBuffer.readByteString())
        transaction.requestBody = decodedContent
        transaction.isRequestBodyEncoded = decodedContent == null
        if (decodedContent != null && limitingSource.isThresholdReached) {
            transaction.requestBody += context.getString(R.string.rq_interceptor_body_content_truncated)
        }
    }

    private fun decodePayload(request: Request, body: ByteString) = bodyDecoders.asSequence()
        .mapNotNull { decoder ->
            try {
                Logger.info("Decoding with: $decoder")
                decoder.decodeRequest(request, body)
            } catch (e: IOException) {
                Logger.warn("Decoder $decoder failed to process request payload", e)
                null
            }
        }.firstOrNull()
}
