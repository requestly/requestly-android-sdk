package io.requestly.android.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object KeyValueStorageManager {

    private const val FILE_NAME = "io.requestly.requestly_pref_file"

    private lateinit var mSharedPref: SharedPreferences
    private lateinit var gson: Gson
    private var changeListeners: MutableList<SharedPreferences.OnSharedPreferenceChangeListener> = mutableListOf()

    fun initialize(context: Context) {
        mSharedPref = context.getSharedPreferences(FILE_NAME, 0)
        gson = Gson()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return mSharedPref.getString(key, defaultValue)
    }

    fun putString(key: String, value: String) {
        with(mSharedPref.edit()) {
            this.putString(key, value)
            this.commit()
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return mSharedPref.getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        with(mSharedPref.edit()) {
            this.putBoolean(key, value)
            this.commit()
        }
    }

    fun <T> putList(key: String, list: List<T>) {
        val json = gson.toJson(list)
        putString(key, json)
    }

    fun <T> getList(key: String, typeToken: TypeToken<T>): T? {
        val json = mSharedPref.getString(key, null) ?: return null
        return gson.fromJson(json, typeToken.type)
    }

    fun registerChangeListener(forKey: String, changeListener: () -> Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key === forKey) {
                changeListener()
            }
        }
        changeListeners.add(listener)
        mSharedPref.registerOnSharedPreferenceChangeListener(listener)
    }
}
