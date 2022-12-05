package io.requestly.android.core.modules.hostSwitcher

import android.content.ClipData.Item
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.core.R
import io.requestly.android.core.databinding.HostSwitcherItemBinding

sealed class ApiModiferRuleItemModel
data class HostSwitchItemModel(
    val startingText: String,
    val provisionalText: String,
    val isActive: Boolean,
    val onSwitchStateChangeListener: ((Boolean) -> Unit)?,
    val onEditClickListener: (() -> Unit)?,
    val onDeleteClickListener: (() -> Unit)?
): ApiModiferRuleItemModel()

data class RedirectRuleItemModel(
    val httpVerbText: String,
    val sourceUrlText: String,
    val destinationUrlText: String,
    val isActive: Boolean,
    val onSwitchStateChangeListener: ((Boolean) -> Unit)?,
    val onEditClickListener: (() -> Unit)?,
    val onDeleteClickListener: (() -> Unit)?
): ApiModiferRuleItemModel()

class HostSwitchItemAdaptor(var items: List<ApiModiferRuleItemModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        when (viewType) {
            R.layout.host_switcher_item -> {
                val binding = HostSwitcherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ItemViewHolder(binding)
            }
            else -> {
                TODO()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is HostSwitchItemModel -> R.layout.host_switcher_item
            is RedirectRuleItemModel -> TODO()
        }
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            (items[position] as? HostSwitchItemModel)?.let {
                holder.bindTo(it)
            }
        }
    }
}
