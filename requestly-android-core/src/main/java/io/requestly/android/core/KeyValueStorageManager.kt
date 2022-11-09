package io.requestly.android.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.ref.WeakReference

data class SharedPrefFileData(
    val key: String,
    val value: Any?,
    val dataType: SharedPrefType?,
    val fileName: String,
)

enum class SharedPrefType {
    STRING {
        override fun toString() = "String"
    },
    INTEGER {
        override fun toString() = "Integer"
    },
    DOUBLE {
        override fun toString() = "Double"
    },
    LONG {
        override fun toString() = "Long"
    },
    BOOLEAN {
        override fun toString() = "Boolean"
    },
    STRING_SET {
        override fun toString() = "StringSet"
    };

    abstract override fun toString(): String
}

object KeyValueStorageManager {

    private const val REQUESTLY_SHARED_PREF_FILE_PREFIX = "io.requestly."
    private const val FILE_NAME = "${REQUESTLY_SHARED_PREF_FILE_PREFIX}requestly_pref_file"

    /**
     * Full path to the default directory assigned to the package for its
     * persistent data.
     */
    private lateinit var mContext: WeakReference<Context>
    private lateinit var mSharedPref: SharedPreferences
    private lateinit var gson: Gson
    private var changeListeners: MutableList<SharedPreferences.OnSharedPreferenceChangeListener> =
        mutableListOf()

    fun initialize(context: Context) {
        mContext = WeakReference(context)
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

    fun deleteKeyFromFile(keyName: String, fileName: String) {
        val pref = mContext.get()?.getSharedPreferences(fileName, 0) ?: return

        with(pref.edit()) {
            this.remove(keyName)
            this.commit()
        }
    }

    fun putStringInfile(value: String, keyName: String, fileName: String) {
        val pref = mContext.get()?.getSharedPreferences(fileName, 0) ?: return
        with(pref.edit()) {
            this.putString(keyName, value)
            this.commit()
        }
    }

    fun putStringSetInfile(value: Set<String>, keyName: String, fileName: String) {
        val pref = mContext.get()?.getSharedPreferences(fileName, 0) ?: return
        with(pref.edit()) {
            this.putStringSet(keyName, value)
            this.commit()
        }
    }

    fun putIntegerInfile(value: Int, keyName: String, fileName: String) {
        val pref = mContext.get()?.getSharedPreferences(fileName, 0) ?: return
        with(pref.edit()) {
            this.putInt(keyName, value)
            this.commit()
        }
    }

    fun putDoubleInfile(value: Double, keyName: String, fileName: String) {
        val pref = mContext.get()?.getSharedPreferences(fileName, 0) ?: return
        with(pref.edit()) {
            this.putFloat(keyName, value.toFloat())
            this.commit()
        }
    }

    fun putLongInfile(value: Long, keyName: String, fileName: String) {
        val pref = mContext.get()?.getSharedPreferences(fileName, 0) ?: return
        with(pref.edit()) {
            this.putLong(keyName, value)
            this.commit()
        }
    }

    fun putBooleanInfile(value: Boolean, keyName: String, fileName: String) {
        val pref = mContext.get()?.getSharedPreferences(fileName, 0) ?: return
        with(pref.edit()) {
            this.putBoolean(keyName, value)
            this.commit()
        }
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

    @Suppress("CANDIDATE_CHOSEN_USING_OVERLOAD_RESOLUTION_BY_LAMBDA_ANNOTATION")
    fun fetchDataFromAllSharedPrefFiles(): List<SharedPrefFileData> {
        val context = mContext.get() ?: return emptyList()

        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

        if (!prefsDir.exists() || !prefsDir.isDirectory) return emptyList()

        return prefsDir
            .list()
            ?.filter { !it.startsWith(REQUESTLY_SHARED_PREF_FILE_PREFIX) }
            ?.flatMap { filename ->
                val fileNameWithOutExtension = filename.removeSuffix(".xml")
                val thisPref = context.getSharedPreferences(fileNameWithOutExtension, 0)
                return@flatMap thisPref.all.map { entry ->
                    SharedPrefFileData(
                        key = entry.key,
                        value = entry.value,
                        dataType = detectType(entry.value),
                        fileName = fileNameWithOutExtension,
                    )
                }
            } ?: emptyList()
    }

    private fun detectType(value: Any?): SharedPrefType? {
        if (value == null) return null

        return when (value::class.simpleName) {
            "Int" -> SharedPrefType.INTEGER
            "Float" -> SharedPrefType.DOUBLE
            "Long" -> SharedPrefType.LONG
            "HashSet" -> SharedPrefType.STRING_SET
            "Boolean" -> SharedPrefType.BOOLEAN
            "String" -> SharedPrefType.STRING
            else -> null
        }
    }
}

