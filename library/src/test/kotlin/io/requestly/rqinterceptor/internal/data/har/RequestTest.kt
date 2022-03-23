package io.requestly.rqinterceptor.internal.data.har

import io.requestly.rqinterceptor.internal.data.har.log.entry.request.PostData
import io.requestly.rqinterceptor.util.HarTestUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class RequestTest {
    @Test
    fun `request is created correctly with method`() {
        val request = HarTestUtils.createRequest("GET")

        assertThat(request?.method).isEqualTo("GET")
    }

    @Test
    fun `request is created correctly with url`() {
        val request = HarTestUtils.createRequest("GET")

        assertThat(request?.url).isEqualTo("http://localhost:80/getUsers")
    }

    @Test
    fun `request is created correctly with http version`() {
        val request = HarTestUtils.createRequest("GET")

        assertThat(request?.httpVersion).isEqualTo("HTTP")
    }

    @Test
    fun `request is created correctly with post data`() {
        val request = HarTestUtils.createRequest("POST")

        assertThat(request?.postData)
            .isEqualTo(PostData(mimeType = "application/json", params = null, text = null))
    }

    @Test
    fun `request is created correctly with body size`() {
        val request = HarTestUtils.createRequest("POST")

        assertThat(request?.bodySize).isEqualTo(1000)
    }
}
