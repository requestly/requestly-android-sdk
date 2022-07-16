package io.requestly.android.event.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.event.EventModel
import io.requestly.android.event.R
import io.requestly.android.event.databinding.RqInterceptorEventsListSingleDesignBinding


class AllEventsAdapter(
    private val events_list: ArrayList<EventModel>,
    private val context: Context,
    private val listener:OnEventClickedListener
) : RecyclerView.Adapter<AllEventsAdapter.EventsViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val viewBinding = RqInterceptorEventsListSingleDesignBinding.inflate(
        LayoutInflater.from(parent.context), parent, false
        )
        return EventsViewHolder(viewBinding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        holder.name.text= events_list[position].name
        holder.time.text= events_list[position].time.toString()
        if(position%2==0){
            holder.bar.setBackgroundResource(R.color.design_default_color_error)
        }
        else{
            holder.bar.setBackgroundResource(R.color.design_default_color_primary_variant)
        }
        holder.name.setOnClickListener{
            listener.onEventClicked(events_list[position].name)
        }
    }

    override fun getItemCount(): Int {
        return events_list.size
    }

    class EventsViewHolder(itemView: RqInterceptorEventsListSingleDesignBinding) :RecyclerView.ViewHolder(itemView.root){
        val name=itemView.rqInterceptorEventNameAllEvents
        val time=itemView.rqInterceptorTimeStampAllEvents
        val bar=itemView.rqInterceptorBarAllEvents
    }
    interface OnEventClickedListener{
        fun onEventClicked(event:String)
    }

}
