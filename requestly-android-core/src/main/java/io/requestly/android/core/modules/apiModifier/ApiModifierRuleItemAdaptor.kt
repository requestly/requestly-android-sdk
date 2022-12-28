package io.requestly.android.core.modules.apiModifier

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.core.databinding.ApiModifierMockRuleItemBinding

data class ApiModifierRuleItemModel(
    val ruleTypeText: String,
    val httpVerbText: String?,
    val operatorText: String,
    val sourceUrlText: String,
    val targetUrlText: String,
    val targetUrlGuideText: String,
    val isActive: Boolean,
    val onSwitchStateChangeListener: ((Boolean) -> Unit)?,
    val onEditClickListener: (() -> Unit)?,
    val onDeleteClickListener: (() -> Unit)?
)

class ApiModifierRuleItemAdaptor(var items: List<ApiModifierRuleItemModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ApiModifierMockRuleItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: ApiModifierMockRuleItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        private val ruleTypeTextView = itemView.ruleTypeTextView
        private val httpMethodTextView = itemView.httpMethodTextView
        private val operatorTextView = itemView.operatorTextView
        private val sourceUrlTextView = itemView.sourceUrlTextView
        private val targetUrlTextView = itemView.targetUrlTextView
        private val targetUrlGuideTextView = itemView.targetUrlGuideText
        private val activeSwitch = itemView.activeSwitch
        private val editTextButton = itemView.editTextButton
        private val deleteTextButton = itemView.deleteTextButton

        fun bindTo(model: ApiModifierRuleItemModel) {
            if (model.httpVerbText != null) {
                httpMethodTextView.text = model.httpVerbText
                httpMethodTextView.visibility = View.VISIBLE
            } else {
                httpMethodTextView.text = null
                httpMethodTextView.visibility = View.GONE
            }

            ruleTypeTextView.text = model.ruleTypeText
            operatorTextView.text = model.operatorText
            sourceUrlTextView.setText(model.sourceUrlText, TextView.BufferType.NORMAL)
            targetUrlTextView.setText(model.targetUrlText, TextView.BufferType.NORMAL)
            targetUrlGuideTextView.text = model.targetUrlGuideText
            activeSwitch.isChecked = model.isActive
            activeSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                model.onSwitchStateChangeListener?.let { it(b) }
            }
            editTextButton.setOnClickListener {
                model.onEditClickListener?.let { it1 -> it1() }
            }
            deleteTextButton.setOnClickListener {
                model.onDeleteClickListener?.let { it1 -> it1() }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bindTo(items[position])
        }
    }
}
