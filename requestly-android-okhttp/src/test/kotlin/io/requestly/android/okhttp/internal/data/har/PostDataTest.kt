package io.requestly.android.okhttp.internal.data.har

import io.requestly.android.okhttp.util.HarTestUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class PostDataTest {
    @Test
    fun `post data is created correctly with mime type`() {
        val postData = HarTestUtils.createPostData("POST")

        assertThat(postData?.mimeType).isEqualTo("application/json")
    }
}
