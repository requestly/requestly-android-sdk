package io.requestly.android.core.modules.logs.lib.lynx.main.ui


import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.core.R
import io.requestly.android.core.databinding.LogsTraceRowViewBinding
import io.requestly.android.core.modules.logs.lib.lynx.main.model.Trace
import io.requestly.android.core.modules.logs.lib.lynx.main.model.TraceLevel

class TracesListAdapter: ListAdapter<Trace, TracesListAdapter.ViewHolder>(TracesListDiffCallback()) {
    override fun getItemViewType(position: Int): Int {
        return getItem(position).level.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder private constructor(private val binding: LogsTraceRowViewBinding): RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup, viewType: Int): ViewHolder {
                val binding = LogsTraceRowViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolder(binding)
            }
        }

        fun bind(item: Trace) {
            val traceMessage: String = item.message
            val traceRepresentation = getTraceVisualRepresentation(item.level, traceMessage)
            renderTraceLog(item)
            binding.logText.text = traceRepresentation
        }

        private fun renderTraceLog(item: Trace) {
            when(item.level) {
                TraceLevel.ERROR -> {
                    setLogTextColor(R.color.log_error_primary_text_color)
                    setBackgroundColor(R.color.log_error_primary_background_color)
                }
                TraceLevel.WARNING -> {
                    setLogTextColor(R.color.log_warning_primary_text_color)
                    setBackgroundColor(R.color.log_warning_primary_background_color)
                }
                else -> {
                    setLogTextColor(R.color.log_default_primary_text_color)
                    setBackgroundColor(R.color.log_default_primary_background_color)
                }
            }
        }

        private fun setLogTextColor(colorId: Int) {
            val res = ContextCompat.getColor(binding.root.context, colorId)
            binding.logText.setTextColor(res)
        }

        private fun setBackgroundColor(colorId: Int) {
            val res = ContextCompat.getColor(binding.root.context, colorId)
            binding.logText.setBackgroundColor(res)
        }

        private fun getTraceColor(level: TraceLevel): Int {
            return when(level) {
                TraceLevel.ASSERT -> Color.BLUE
                TraceLevel.DEBUG -> Color.BLUE
                TraceLevel.INFO -> Color.GRAY
                TraceLevel.WARNING -> Color.rgb(255, 165, 0)
                TraceLevel.ERROR -> Color.RED
                TraceLevel.WTF -> Color.RED
                else -> Color.GRAY
            }
        }


        private fun getTraceVisualRepresentation(
            level: TraceLevel,
            traceMessage: String
        ): Spannable? {
            var traceMessage = traceMessage
            traceMessage = " " + level.value + "  " + traceMessage
            val traceRepresentation: Spannable = SpannableString(traceMessage)
            val traceColor: Int = getTraceColor(level)
            traceRepresentation.setSpan(
                BackgroundColorSpan(traceColor), 0, 3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return traceRepresentation
        }
    }
}

class TracesListDiffCallback: DiffUtil.ItemCallback<Trace>() {
    override fun areItemsTheSame(oldItem: Trace, newItem: Trace): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Trace, newItem: Trace): Boolean {
        return oldItem == newItem
    }
}


