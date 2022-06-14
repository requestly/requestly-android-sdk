package io.requestly.android.okhttp.internal.data.repository

import androidx.lifecycle.LiveData
import io.requestly.android.okhttp.internal.data.entity.HttpTransaction
import io.requestly.android.okhttp.internal.data.entity.HttpTransactionTuple
import io.requestly.android.okhttp.internal.data.room.RQInterceptorDatabase
import io.requestly.android.okhttp.internal.support.distinctUntilChanged

internal class HttpTransactionDatabaseRepository(
    private val database: RQInterceptorDatabase
) : HttpTransactionRepository {

    private val transactionDao get() = database.transactionDao()

    override fun getFilteredTransactionTuples(code: String, path: String): LiveData<List<HttpTransactionTuple>> {
        val pathQuery = if (path.isNotEmpty()) "%$path%" else "%"
        return transactionDao.getFilteredTuples("$code%", pathQuery)
    }

    override fun getTransaction(transactionId: Long): LiveData<HttpTransaction?> {
        return transactionDao.getById(transactionId)
            .distinctUntilChanged { old, new -> old?.hasTheSameContent(new) != false }
    }

    override fun getSortedTransactionTuples(): LiveData<List<HttpTransactionTuple>> {
        return transactionDao.getSortedTuples()
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAll()
    }

    override suspend fun insertTransaction(transaction: HttpTransaction) {
        val id = transactionDao.insert(transaction)
        transaction.id = id ?: 0
    }

    override suspend fun updateTransaction(transaction: HttpTransaction): Int {
        return transactionDao.update(transaction)
    }

    override suspend fun deleteOldTransactions(threshold: Long) {
        transactionDao.deleteBefore(threshold)
    }

    override suspend fun getAllTransactions(): List<HttpTransaction> = transactionDao.getAll()
}
