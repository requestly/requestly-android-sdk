package io.requestly.rqinterceptor.internal.data.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.requestly.rqinterceptor.internal.data.repository.RepositoryProvider.initialize
import io.requestly.rqinterceptor.internal.data.room.RQInterceptorDatabase

/**
 * A singleton to hold the [HttpTransactionRepository] instance.
 * Make sure you call [initialize] before accessing the stored instance.
 */
internal object RepositoryProvider {

    private var transactionRepository: HttpTransactionRepository? = null

    fun transaction(): HttpTransactionRepository {
        return checkNotNull(transactionRepository) {
            "You can't access the transaction repository if you don't initialize it!"
        }
    }

    /**
     * Idempotent method. Must be called before accessing the repositories.
     */
    fun initialize(applicationContext: Context) {
        if (transactionRepository == null) {
            val db = RQInterceptorDatabase.create(applicationContext)
            transactionRepository = HttpTransactionDatabaseRepository(db)
        }
    }

    /**
     * Cleanup stored singleton objects
     */
    @VisibleForTesting
    fun close() {
        transactionRepository = null
    }
}
