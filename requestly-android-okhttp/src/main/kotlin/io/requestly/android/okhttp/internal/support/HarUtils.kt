package io.requestly.android.okhttp.internal.support

import androidx.annotation.VisibleForTesting
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.data.har.Har
import io.requestly.android.okhttp.internal.data.har.log.Creator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// http://www.softwareishard.com/blog/har-12-spec/
// https://github.com/ahmadnassri/har-spec/blob/master/versions/1.2.md
internal object HarUtils {
    suspend fun harStringFromTransactions(
        transactions: List<HttpTransaction>,
        name: String,
        version: String,
    ): String = withContext(Dispatchers.Default) {
        JsonConverter.nonNullSerializerInstance
            .toJson(fromHttpTransactions(transactions, Creator(name, version)))
    }

    fun harStringFromTransaction(
        transactions: List<HttpTransaction>,
        name: String,
        version: String,
    ): String =
        JsonConverter.nonNullSerializerInstance
            .toJson(fromHttpTransactions(transactions, Creator(name, version)))

    @VisibleForTesting
    fun fromHttpTransactions(transactions: List<HttpTransaction>, creator: Creator): Har {
        return Har(transactions, creator)
    }
}
