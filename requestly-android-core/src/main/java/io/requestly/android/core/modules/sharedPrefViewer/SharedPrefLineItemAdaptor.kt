package io.requestly.android.core.modules.sharedPrefViewer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.core.databinding.SharedPrefLineItemBinding

data class SharedPrefLineItemModel(
    val dataTypeText: String,
    val fileName: String,
    val prefKeyText: String,
    val prefValueText: String,
    val onEditClickListener: (() -> Unit)?,
    val onDeleteClickListener: (() -> Unit)?
)

class SharedPrefLineItemAdaptor(var items: List<SharedPrefLineItemModel>) :
    RecyclerView.Adapter<SharedPrefLineItemAdaptor.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val viewBinding = SharedPrefLineItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: SharedPrefLineItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private val dataTypeTextView = itemView.dataTypeTextView
        private val fileNameTextView = itemView.fileNameTextView
        private val prefKeyTextView = itemView.prefKeyTextView
        private val prefValueTextView = itemView.prefValueTextView
        private val editPrefButton = itemView.editPrefButton
        private val deletePrefButton = itemView.deletePrefButton

        fun bindTo(model: SharedPrefLineItemModel) {
            dataTypeTextView.text = model.dataTypeText
            fileNameTextView.text = model.prefKeyText
            prefKeyTextView.text = model.prefKeyText
            prefValueTextView.text = model.prefValueText
            editPrefButton.setOnClickListener {
                model.onEditClickListener?.let { it1 -> it1() }
            }
            deletePrefButton.setOnClickListener {
                model.onDeleteClickListener?.let { it1 -> it1() }
            }
        }
    }
}
