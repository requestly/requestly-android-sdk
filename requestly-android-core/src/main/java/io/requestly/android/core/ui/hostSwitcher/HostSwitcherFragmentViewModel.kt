package io.requestly.android.core.ui.hostSwitcher

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.UUID


data class SwitchingRule(var startingText: String, var provisionalText: String, var isActive: Boolean, val id: String)

class HostSwitcherFragmentViewModel: ViewModel() {

    private var _rulesListLive = MutableLiveData<List<SwitchingRule>>()
    val rulesListLive: LiveData<List<SwitchingRule>> = _rulesListLive

    init {
        _rulesListLive.value = emptyList()
    }

    fun createItem(startingText: String, provisionalText: String) {
        val newRule = SwitchingRule(startingText, provisionalText, true, UUID.randomUUID().toString())
        _rulesListLive.value = _rulesListLive.value!! + newRule
    }

    fun editSwitchState(ruleId: String, newValue: Boolean) {
        _rulesListLive.value?.find { it.id === ruleId }?.isActive = newValue
    }

    fun editItem(startingText: String, provisionalText: String, ruleId: String) {
        val rule =  _rulesListLive.value?.find { it.id === ruleId }
        rule?.startingText = startingText
        rule?.provisionalText = provisionalText
        _rulesListLive.postValue(_rulesListLive.value)
    }

    fun deleteItem(ruleId: String) {
        val newList = _rulesListLive.value?.filter { it.id !== ruleId } ?: emptyList()
        _rulesListLive.value = newList
    }
}
