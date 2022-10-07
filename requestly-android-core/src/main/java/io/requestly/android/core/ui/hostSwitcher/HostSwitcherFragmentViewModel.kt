package io.requestly.android.core.ui.hostSwitcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.requestly.android.core.KeyValueStorageManager
import java.util.*


data class SwitchingRule(
    @SerializedName("startingText") var startingText: String,
    @SerializedName("provisionalText") var provisionalText: String,
    @SerializedName("isActive") var isActive: Boolean,
    @SerializedName("id") val id: String)

class HostSwitcherFragmentViewModel: ViewModel() {

    companion object {
        const val KEY_NAME = "io.requestly.host_switch_rules_key"
    }

    private var _rulesListLive = MutableLiveData<List<SwitchingRule>>()
    val rulesListLive: LiveData<List<SwitchingRule>> = _rulesListLive

    init {
        // T should be known at compile time.
        // https://stackoverflow.com/a/14506181
        val typeToken = object : TypeToken<List<SwitchingRule>>() {}
        _rulesListLive.value = KeyValueStorageManager.getList(KEY_NAME, typeToken) ?: emptyList()
    }

    fun createItem(startingText: String, provisionalText: String) {
        val newRule = SwitchingRule(startingText, provisionalText, true, UUID.randomUUID().toString())
        val list = _rulesListLive.value!! + newRule
        _rulesListLive.value = list

        KeyValueStorageManager.putList(KEY_NAME, list)
    }

    fun editSwitchState(ruleId: String, newValue: Boolean) {
        _rulesListLive.value?.find { it.id === ruleId }?.isActive = newValue
        KeyValueStorageManager.putList(KEY_NAME, _rulesListLive.value!!)
    }

    fun editItem(startingText: String, provisionalText: String, ruleId: String) {
        val rule =  _rulesListLive.value?.find { it.id === ruleId }
        rule?.startingText = startingText
        rule?.provisionalText = provisionalText
        _rulesListLive.postValue(_rulesListLive.value)
        KeyValueStorageManager.putList(KEY_NAME, _rulesListLive.value!!)
    }

    fun deleteItem(ruleId: String) {
        val newList = _rulesListLive.value?.filter { it.id !== ruleId } ?: emptyList()
        _rulesListLive.value = newList
        KeyValueStorageManager.putList(KEY_NAME, newList)
    }
}
