package io.requestly.android.core.modules.sharedPrefViewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.requestly.android.core.KeyValueStorageManager
import io.requestly.android.core.SharedPrefFileData
import io.requestly.android.core.SharedPrefType

class SharedPrefViewerViewModel : ViewModel() {
    private val CONST_ALL_FILES = "ALL"

    private var _prefEntriesLive = MutableLiveData<List<SharedPrefFileData>>()
    val prefEntriesLive: LiveData<List<SharedPrefFileData>> = _prefEntriesLive

    private var _prefFilesNamesLive = MutableLiveData<List<String>>()
    var prefFilesNamesLive: LiveData<List<String>> = _prefFilesNamesLive

    var mSelectedFileName = CONST_ALL_FILES
        private set

    init {
        loadUIData()
    }

    fun deleteEntry(fileName: String, keyName: String) {
        KeyValueStorageManager.deleteKeyFromFile(keyName, fileName)
        loadUIData()
    }

    fun fileNameSelected(fileName: String) {
        mSelectedFileName = fileName
        loadUIData()
    }

    private fun loadUIData() {
        val entireData = KeyValueStorageManager.fetchDataFromAllSharedPrefFiles()
        _prefEntriesLive.value = entireData.filter {
            it.fileName == mSelectedFileName || (mSelectedFileName == CONST_ALL_FILES)
        }
        _prefFilesNamesLive.value =
            (entireData.map { it.fileName }.distinct() + CONST_ALL_FILES).reversed()
    }

    fun verifyAndSave(newValue: String, prefFileData: SharedPrefFileData): Boolean {
        prefFileData.dataType ?: return false
        var success = false
        when (prefFileData.dataType) {
            SharedPrefType.STRING -> {
                KeyValueStorageManager.putStringInfile(
                    newValue,
                    prefFileData.key,
                    prefFileData.fileName
                )
                success = true
            }
            SharedPrefType.INTEGER -> {
                newValue.toIntOrNull()?.let {
                    KeyValueStorageManager.putIntegerInfile(
                        it,
                        prefFileData.key,
                        prefFileData.fileName
                    )
                    success = true
                }
            }
            SharedPrefType.DOUBLE -> {
                newValue.toDoubleOrNull()?.let {
                    KeyValueStorageManager.putDoubleInfile(
                        it,
                        prefFileData.key,
                        prefFileData.fileName
                    )
                    success = true
                }
            }
            SharedPrefType.LONG -> {
                newValue.toLongOrNull()?.let {
                    KeyValueStorageManager.putLongInfile(
                        it,
                        prefFileData.key,
                        prefFileData.fileName
                    )
                    success = true
                }
            }
            SharedPrefType.BOOLEAN -> {
                newValue.toBooleanStrictOrNull()?.let {
                    KeyValueStorageManager.putBooleanInfile(
                        it,
                        prefFileData.key,
                        prefFileData.fileName
                    )
                    success = true
                }
            }
            SharedPrefType.STRING_SET -> {
                if (newValue[0] == '[' && newValue[newValue.length - 1] == ']') {
                    val stringSet = newValue
                        .subSequence(1, newValue.length - 1)
                        .split(",")
                        .map { it.trim() }
                        .toSet()
                    KeyValueStorageManager.putStringSetInfile(
                        stringSet,
                        prefFileData.key,
                        prefFileData.fileName
                    )
                    success = true
                }
            }
        }
        loadUIData()
        return success
    }
}
