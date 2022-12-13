package io.requestly.android.core.modules.hostSwitcher.models

import com.google.gson.annotations.SerializedName

data class Rule constructor(
    val id: String,
    val ruleType: RuleType,
    internal var status: RuleStatus,
    val pairs: List<Pair>
) {
    companion object
}

enum class SourceKey {
    @SerializedName("Url")
    URL
}

enum class SourceOperator {
    @SerializedName("Contains")
    CONTAINS
}

enum class HttpVerb {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    CONNECT,
    TRACE
}

data class Filter(
    @SerializedName("requestMethod") val requestMethod: List<HttpVerb>
)

data class Source(
    @SerializedName("key") val key: SourceKey,
    @SerializedName("operator") val operator: SourceOperator,
    @SerializedName("value") val value: String,
    @SerializedName("filters") val filters: List<Filter> = emptyList()
)

sealed class Pair
class Redirect(
    @SerializedName("destination") val destination: String,
    @SerializedName("source") val source: Source
) : Pair()

class Replace(
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("source") val source: Source
) : Pair()

enum class RuleType {
    @SerializedName("Replace")
    REPLACE,

    @SerializedName("Redirect")
    REDIRECT
}

enum class RuleStatus {
    @SerializedName("Active")
    ACTIVE,

    @SerializedName("Inactive")
    INACTIVE
}
