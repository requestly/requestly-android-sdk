package io.requestly.android.core.modules.apiModifier.processors

import android.util.Patterns
import com.google.gson.reflect.TypeToken
import io.requestly.android.core.KeyValueStorageManager
import io.requestly.android.core.modules.apiModifier.ApiModifierFragmentViewModel
import io.requestly.android.core.modules.apiModifier.dao.*
import io.requestly.android.core.modules.apiModifier.processors.models.Action
import io.requestly.android.core.modules.apiModifier.processors.models.ActionName
import okhttp3.Request
import java.net.URL

object RuleProcessor {

    private var activeRules: MutableList<Rule> = mutableListOf()

    init {
        val typeToken = object : TypeToken<List<Rule>>() {}
        val storageChangeListener: () -> Unit = {
            activeRules =
                KeyValueStorageManager.getList(ApiModifierFragmentViewModel.KEY_NAME, typeToken)
                    ?.filter {
                        it.isActive
                    }?.toMutableList() ?: mutableListOf()
        }
        storageChangeListener()
        KeyValueStorageManager.registerChangeListener(
            ApiModifierFragmentViewModel.KEY_NAME,
            storageChangeListener
        )
    }

    fun process(request: Request): List<Action> {
        val actions = activeRules.map mapperFunc@{
            val rule = it.pairs.firstOrNull() ?: return@mapperFunc null
            when (rule) {
                is Redirect -> redirectRuleProcessor(rule, request)
                is Replace -> replaceRuleProcessor(rule, request)
            }
        }
        return actions.filterNotNull()
    }
}

private fun replaceRuleProcessor(rule: Replace, request: Request): Action? {
    val url = request.url().toString()
    if (!url.contains(rule.from)) {
        return null
    }
    val modifiedUrl = url.replace(rule.from, rule.to, true)
    if (!Patterns.WEB_URL.matcher(url).matches()) return null

    return Action(
        action = ActionName.REDIRECT,
        url = URL(modifiedUrl)
    )
}

private fun redirectRuleProcessor(rule: Redirect, request: Request): Action? {
    val url = request.url().toString()
    if (!url.contains(rule.source.value)) {
        return null
    }

    rule.source.filters
        .firstOrNull()?.requestMethod
        ?.firstOrNull { it.toString() == request.method() }
        ?: return null

    if (!Patterns.WEB_URL.matcher(rule.destination).matches()) return null

    return Action(
        action = ActionName.REDIRECT,
        url = URL(rule.destination)
    )
}

