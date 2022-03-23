package io.requestly.rqinterceptor.internal.data.har

import io.requestly.rqinterceptor.internal.data.entity.HttpTransaction
import io.requestly.rqinterceptor.internal.data.har.log.Creator
import com.google.gson.annotations.SerializedName

internal data class Har(
    @SerializedName("log") val log: Log
) {
    constructor(transactions: List<HttpTransaction>, creator: Creator) : this(
        log = Log(transactions, creator)
    )
}
