package io.requestly.android.core.ui.hostSwitcher

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.core.databinding.HostSwitcherItemBinding

data class HostSwitchItemModel(
    val startingText: String,
    val provisionalText: String,
    val isActive: Boolean,
    val onSwitchStateChangeListener: ((Boolean) -> Unit)?,
    val onEditClickListener: (() -> Unit)?,
    val onDeleteClickListener: (() -> Unit)?
)

class HostSwitchItemAdaptor(var items: List<HostSwitchItemModel>) :
    RecyclerView.Adapter<HostSwitchItemAdaptor.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val viewBinding =
            HostSwitcherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: HostSwitcherItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private val startingTextView = itemView.startingTextView
        private val provisionalTextView = itemView.provisionalTextView
        private val editButton = itemView.editButton
        private val deleteButton = itemView.deleteButton
        private val activeSwitch = itemView.activeSwitch

        fun bindTo(model: HostSwitchItemModel) {
            startingTextView.text = model.startingText
            provisionalTextView.text = model.provisionalText
            activeSwitch.isChecked = model.isActive
            activeSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                model.onSwitchStateChangeListener?.let { it(b) }
            }
            editButton.setOnClickListener {
                model.onEditClickListener?.let { it1 -> it1() }
            }
            deleteButton.setOnClickListener {
                model.onDeleteClickListener?.let { it1 -> it1() }
            }
        }
    }
}
