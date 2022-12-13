package io.requestly.android.core.modules.hostSwitcher.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.util.*

class RuleJsonDeserializer : JsonDeserializer<Rule?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Rule? {
        val jsonObject = json?.asJsonObject

        val id =
            context?.deserialize<String>(jsonObject?.get("id"), String::class.java) ?: return null
        val ruleType =
            context.deserialize<RuleType>(jsonObject?.get("ruleType"), RuleType::class.java)
                ?: return null
        val status =
            context.deserialize<RuleStatus>(jsonObject?.get("status"), RuleStatus::class.java)
                ?: RuleStatus.INACTIVE

        val pairsJsonArray = jsonObject?.get("pairs")?.asJsonArray ?: return null

        val pairs = when (ruleType) {
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
            pairs = pairs
        )
    }
}

fun Rule.Companion.newRedirectRule(
    destination: String,
    requestMethod: List<HttpVerb>,
    sourceUrlText: String
): Rule {
    return Rule(
        id = UUID.randomUUID().toString(),
        ruleType = RuleType.REDIRECT,
        status = RuleStatus.ACTIVE,
        pairs = listOf(
            Redirect(
                destination = destination,
                source = Source(
                    key = SourceKey.URL,
                    operator = SourceOperator.CONTAINS,
                    value = sourceUrlText,
                    filters = listOf(
                        Filter(
                            requestMethod
                        )
                    )
                )
            )
        )
    )
}

fun Rule.Companion.newReplaceRule(
    id: String = UUID.randomUUID().toString(),
    from: String,
    to: String
): Rule {
    return Rule(
        id = id,
        ruleType = RuleType.REPLACE,
        status = RuleStatus.ACTIVE,
        pairs = listOf(
            Replace(
                from,
                to,
                Source(
                    key = SourceKey.URL,
                    operator = SourceOperator.CONTAINS,
                    value = from,
                    filters = listOf(Filter(listOf(HttpVerb.GET, HttpVerb.POST)))
                )
            )
        )
    )
}

var Rule.isActive: Boolean
    get() = status == RuleStatus.ACTIVE
    set(value) {
    if (value) {
        status = RuleStatus.ACTIVE
        return
    }

    status = RuleStatus.INACTIVE
}
