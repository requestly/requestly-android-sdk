package io.requestly.android.core.modules.hostSwitcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.reflect.TypeToken
import io.requestly.android.core.KeyValueStorageManager
import io.requestly.android.core.modules.hostSwitcher.models.*


typealias SwitchingRule = Rule

class HostSwitcherFragmentViewModel: ViewModel() {

    companion object {
        const val KEY_NAME = "io.requestly.api_modifier_rules_key"
    }

    private var _rulesListLive = MutableLiveData<List<SwitchingRule>>()
    val rulesListLive: LiveData<List<SwitchingRule>> = _rulesListLive

    init {
        // T should be known at compile time.
        // https://stackoverflow.com/a/14506181
        val typeToken = object : TypeToken<List<SwitchingRule>>() {}

        _rulesListLive.value = KeyValueStorageManager.getList(KEY_NAME, typeToken) ?: emptyList()
    }

    fun createReplaceRule(startingText: String, provisionalText: String) {
        val newRule = SwitchingRule.newReplaceRule(
            from = startingText,
            to = provisionalText
        )
        val list = _rulesListLive.value!! + newRule
        _rulesListLive.value = list

        KeyValueStorageManager.putList(KEY_NAME, list)
    }

    fun createRedirectRule(httpMethod: HttpVerb, sourceUrlText: String, destinationUrl: String) {
        val newRule = SwitchingRule.newRedirectRule(
            destination = destinationUrl,
            requestMethod = listOf(httpMethod),
            sourceUrlText = sourceUrlText
        )
        val list = _rulesListLive.value!! + newRule
        _rulesListLive.value = list

        KeyValueStorageManager.putList(KEY_NAME, list)
    }

    fun editSwitchState(ruleId: String, newValue: Boolean) {
        _rulesListLive.value?.find { it.id === ruleId }?.isActive = newValue
        KeyValueStorageManager.putList(KEY_NAME, _rulesListLive.value!!)
    }

    fun editReplaceRule(sourceText: String, targetText: String, ruleId: String) {
        val mutableList =  _rulesListLive.value!!.toMutableList()
        val index =  mutableList.indexOfFirst { it.id == ruleId }

        if (index != -1) {
            val rule = mutableList[index]
            mutableList[index] = SwitchingRule.newReplaceRule(
                id = rule.id,
                from = sourceText,
                to = targetText
            )
        }
        _rulesListLive.postValue(mutableList)
        KeyValueStorageManager.putList(KEY_NAME, mutableList)
    }

    fun editRedirectRule(httpVerb: HttpVerb, sourceText: String, targetText: String, ruleId: String) {
        val mutableList =  _rulesListLive.value!!.toMutableList()
        val index =  mutableList.indexOfFirst { it.id == ruleId }

        if (index != -1) {
            val rule = mutableList[index]
            mutableList[index] = SwitchingRule.newRedirectRule(
                id = rule.id,
                destination = targetText,
                requestMethod = listOf(httpVerb),
                sourceUrlText = sourceText
            )
        }
        _rulesListLive.postValue(mutableList)
        KeyValueStorageManager.putList(KEY_NAME, mutableList)
    }

    fun deleteItem(ruleId: String) {
        val newList = _rulesListLive.value?.filter { it.id !== ruleId } ?: emptyList()
        _rulesListLive.value = newList
        KeyValueStorageManager.putList(KEY_NAME, newList)
    }
}
