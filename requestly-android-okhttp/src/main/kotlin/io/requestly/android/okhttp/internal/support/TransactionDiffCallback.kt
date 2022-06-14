package io.requestly.android.okhttp.internal.support

import androidx.recyclerview.widget.DiffUtil
import io.requestly.android.okhttp.internal.data.entity.HttpTransactionTuple

internal object TransactionDiffCallback : DiffUtil.ItemCallback<HttpTransactionTuple>() {
    override fun areItemsTheSame(oldItem: HttpTransactionTuple, newItem: HttpTransactionTuple): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HttpTransactionTuple, newItem: HttpTransactionTuple): Boolean {
        return oldItem == newItem
    }

    // Overriding function is empty on purpose to avoid flickering by default animator
    override fun getChangePayload(oldItem: HttpTransactionTuple, newItem: HttpTransactionTuple) = Unit
}
