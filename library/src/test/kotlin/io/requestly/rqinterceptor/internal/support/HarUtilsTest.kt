package io.requestly.rqinterceptor.internal.support

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.requestly.rqinterceptor.R
import io.requestly.rqinterceptor.internal.data.har.log.Entry
import io.requestly.rqinterceptor.util.HarTestUtils
import io.requestly.rqinterceptor.util.HarTestUtils.createHarString
import io.requestly.rqinterceptor.util.HarTestUtils.createListTransactionHar
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
internal class HarUtilsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `entry list is created correctly with different methods`() {
        val har = context.createListTransactionHar()

        assertThat(har.log.entries).hasSize(2)
        assertThat(har.log.entries[0].request.method).isEqualTo("GET")
        assertThat(har.log.entries[1].request.method).isEqualTo("POST")
    }

    @Test
    fun `har content is created correctly`(): Unit = runBlocking {
        val transaction = HarTestUtils.createTransaction("POST")
        val result = context.createHarString()
        val rqInterceptorName = context.getString(R.string.rq_interceptor_name)
        val rqInterceptorVersion = context.getString(R.string.rq_interceptor_version)
        val startedDateTime = Entry.DateFormat.get()!!.format(Date(transaction.requestDate!!))

        assertThat(result).isEqualTo(
            """
                {
                  "log": {
                    "version": "1.2",
                    "creator": {
                      "name": "$rqInterceptorName",
                      "version": "$rqInterceptorVersion"
                    },
                    "entries": [
                      {
                        "startedDateTime": "$startedDateTime",
                        "time": 1000,
                        "request": {
                          "method": "POST",
                          "url": "http://localhost:80/getUsers",
                          "httpVersion": "HTTP",
                          "cookies": [],
                          "headers": [],
                          "queryString": [],
                          "postData": {
                            "mimeType": "application/json"
                          },
                          "headersSize": 0,
                          "bodySize": 1000,
                          "totalSize": 1000
                        },
                        "response": {
                          "status": 200,
                          "statusText": "OK",
                          "httpVersion": "HTTP",
                          "cookies": [],
                          "headers": [],
                          "content": {
                            "size": 1000,
                            "mimeType": "application/json",
                            "text": "{\"field\": \"value\"}"
                          },
                          "redirectURL": "",
                          "headersSize": 0,
                          "bodySize": 1000,
                          "totalSize": 1000
                        },
                        "cache": {},
                        "timings": {
                          "connect": 0,
                          "send": 0,
                          "wait": 1000,
                          "receive": 0,
                          "comment": "The information described by this object is incomplete."
                        }
                      }
                    ]
                  }
                }
            """.trimIndent()
        )
    }
}
