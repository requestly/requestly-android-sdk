package io.requestly.android.core.modules.hostSwitcher.models

import android.util.Patterns
import com.google.gson.reflect.TypeToken
import io.requestly.android.core.KeyValueStorageManager
import io.requestly.android.core.modules.hostSwitcher.HostSwitcherFragmentViewModel
import okhttp3.Request
import java.net.URL

object RuleProcessor {

    var activeRules: MutableList<Rule> = mutableListOf()

    init {
        val typeToken = object : TypeToken<List<Rule>>() {}
        val storageChangeListener: () -> Unit = {
            activeRules =
                KeyValueStorageManager.getList(HostSwitcherFragmentViewModel.KEY_NAME, typeToken)
                    ?.filter {
                        it.isActive
                    }?.toMutableList() ?: mutableListOf()
        }
        storageChangeListener()
        KeyValueStorageManager.registerChangeListener(
            HostSwitcherFragmentViewModel.KEY_NAME,
            storageChangeListener
        )
    }

    fun process(request: Request): List<Action> {
        val actions = activeRules.map mapperFunc@{
            val rule = it.pairs.firstOrNull() ?: return@mapperFunc null
            when (rule) {
                is Redirect -> RedirectRuleProcessor(rule, request)
                is Replace -> ReplaceRuleProcessor(rule, request)
            }
        }
        return actions.filterNotNull()
    }
}

fun ReplaceRuleProcessor(rule: Replace, request: Request): Action? {
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

fun RedirectRuleProcessor(rule: Redirect, request: Request): Action? {
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

