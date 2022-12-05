package io.requestly.android.core.modules.hostSwitcher.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.util.*

data class Rule constructor(
    val id: String,
    val ruleType: RuleType,
    private var status: RuleStatus,
    val pairs: List<Pair>
) {
    companion object {
        fun newRedirectRule(destination: String, source: Source): Rule {
            return Rule(
                UUID.randomUUID().toString(),
                RuleType.REDIRECT,
                RuleStatus.ACTIVE,
                pairs = listOf(
                    Redirect(
                        destination,
                        source
                    )
                )
            )
        }

        fun newReplaceRule(
            id: String = UUID.randomUUID().toString(),
            from: String,
            to: String
        ): Rule {
            return Rule(
                id,
                RuleType.REPLACE,
                RuleStatus.ACTIVE,
                pairs = listOf(
                    Replace(
                        from,
                        to,
                        Source(
                            key = SourceKey.URL,
                            operator = SourceOperator.CONTAINS,
                            from
                        )
                    )
                )
            )
        }
    }

    var isActive: Boolean
        get() = status == RuleStatus.ACTIVE
        set(value) {
            if (value) {
                status = RuleStatus.ACTIVE
                return
            }

            status = RuleStatus.INACTIVE
        }
}

enum class SourceKey {
    @SerializedName("Url")
    URL
}

enum class SourceOperator {
    @SerializedName("Contains")
    CONTAINS
}

data class Source(
    @SerializedName("key") var key: SourceKey,
    @SerializedName("operator") var operator: SourceOperator,
    @SerializedName("value") var value: String
)

interface Pair
class Redirect(
    @SerializedName("destination") var destination: String,
    @SerializedName("source") var source: Source
) : Pair

class Replace(
    @SerializedName("from") var from: String,
    @SerializedName("to") var to: String,
    @SerializedName("source") var source: Source
) : Pair

enum class RuleType {
    @SerializedName("Replace")
    REPLACE {
        override fun toString(): String {
            return "Replace"
        }
    },

    @SerializedName("Redirect")
    REDIRECT {
        override fun toString(): String {
            return "Redirect"
        }
    }
}

enum class RuleStatus {
    @SerializedName("Active")
    ACTIVE,

    @SerializedName("Inactive")
    INACTIVE
}

class RuleJsonDeserializer : JsonDeserializer<Rule?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Rule? {
        val jsonObject = json?.asJsonObject

        val id = context?.deserialize<String>(jsonObject?.get("id"), String::class.java) ?: return null
        val ruleType = context.deserialize<RuleType>(jsonObject?.get("ruleType"), RuleType::class.java)
            ?: return null
        val status = context.deserialize<RuleStatus>(jsonObject?.get("status"), RuleStatus::class.java)
            ?: RuleStatus.INACTIVE

        val pairsJsonArray = jsonObject?.get("pairs")?.asJsonArray ?: return null

        val pairs = when(ruleType) {
            RuleType.REPLACE -> {
                pairsJsonArray.map { context.deserialize<Replace>(it, Replace::class.java) }
            }
            RuleType.REDIRECT -> {
                pairsJsonArray.map { context.deserialize<Redirect>(it, Redirect::class.java) }
            }
        }
        return Rule(
            id = id,
            ruleType = ruleType,
            status = status,
            pairs = pairs.filterNotNull()
        )
    }
}
