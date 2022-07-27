package io.requestly.android.event.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.event.R
import io.requestly.android.event.databinding.EventsListItemBinding
import io.requestly.android.event.internal.data.entity.Event
import java.text.SimpleDateFormat
import java.util.Date

class EventListAdapter(
    private val context: Context,
    private val onEventClickListener: (Long) -> Unit,
) : RecyclerView.Adapter<EventListAdapter.EventsViewHolder>() {

    private var eventsList: List<Event> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val viewBinding = EventsListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventsViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        holder.name.text = eventsList[position].eventName

        val dateFormatter = SimpleDateFormat("hh:mm:ss a")
        val resultdate = Date(eventsList[position].timestamp ?: 0)
        holder.timestamp.text = dateFormatter.format(resultdate)

        if (eventsList[position].status == true) {
            holder.statusBar.setBackgroundResource(R.color.success)
        } else {
            holder.statusBar.setBackgroundResource(R.color.failure)
        }

        holder.name.setOnClickListener {
            onEventClickListener(eventsList[position].id)
        }
    }

    override fun getItemCount(): Int {
        return eventsList.size ?: 0
    }

    fun updateEventsList(updatedEventsList: List<Event>) {
        eventsList = updatedEventsList
        notifyDataSetChanged()
    }

    class EventsViewHolder(itemView: EventsListItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val name = itemView.eventListItemName
        val timestamp = itemView.eventsListItemTimestamp
        val statusBar = itemView.eventListItemStatusBar
    }
}
