package io.requestly.android.event.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.event.databinding.EventOverviewPropertyBinding
import io.requestly.android.event.internal.data.entity.Event

class EventOverviewAdapter(
    private var event: Event?,
    private val context: Context,
): RecyclerView.Adapter<EventOverviewAdapter.EventPropertyViewHolder>() {
    private var eventKeys: List<String> = emptyList()

    init {
        updateEventKeys()
    }

    private fun updateEventKeys() {
        eventKeys = event?.eventData?.keys?.toList() ?: emptyList()
    }

    fun updateEvent(updatedEvent: Event) {
        event = updatedEvent
        updateEventKeys()
        notifyDataSetChanged()
    }

    class EventPropertyViewHolder(itemView: EventOverviewPropertyBinding) :RecyclerView.ViewHolder(itemView.root) {
        val label=itemView.rqInterceptorPropertyLabel
        val propertyValue=itemView.rqInterceptorPropertyValue
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventPropertyViewHolder {
        val viewBinding = EventOverviewPropertyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventPropertyViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: EventPropertyViewHolder, position: Int) {
        val keyName = eventKeys[position]
        holder.label.text = keyName
        holder.propertyValue.text = event?.eventData?.get(keyName)
    }

    override fun getItemCount(): Int {
       return eventKeys.size
    }
}
