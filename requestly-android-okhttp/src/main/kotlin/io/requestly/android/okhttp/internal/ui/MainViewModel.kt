package io.requestly.android.okhttp.internal.ui

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.data.entity.HttpTransactionTuple
import io.requestly.android.okhttp.internal.data.repository.RepositoryProvider
import io.requestly.android.okhttp.internal.support.NotificationHelper
import kotlinx.coroutines.launch

internal class MainViewModel : ViewModel() {

    private val currentFilter = MutableLiveData("")

    val transactions: LiveData<List<HttpTransactionTuple>> = currentFilter.switchMap { searchQuery ->
        with(RepositoryProvider.transaction()) {
            when {
                searchQuery.isNullOrBlank() -> {
                    getSortedTransactionTuples()
                }
                TextUtils.isDigitsOnly(searchQuery) -> {
                    getFilteredTransactionTuples(searchQuery, "")
                }
                else -> {
                    getFilteredTransactionTuples("", searchQuery)
                }
            }
        }
    }

    suspend fun getAllTransactions(): List<HttpTransaction> = RepositoryProvider.transaction().getAllTransactions()

    fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = searchQuery
    }

    fun clearTransactions() {
        viewModelScope.launch {
            RepositoryProvider.transaction().deleteAllTransactions()
        }
        NotificationHelper.clearBuffer()
    }
}
