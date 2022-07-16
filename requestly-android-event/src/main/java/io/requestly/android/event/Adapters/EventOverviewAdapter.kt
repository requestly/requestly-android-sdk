package io.requestly.android.event.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.requestly.android.event.databinding.RqInterceptorEventsListSingleDesignBinding
import io.requestly.android.event.databinding.RqInterceptorSingleDesignEventsOverviewBinding
import java.util.*
import kotlin.collections.ArrayList

class EventOverviewAdapter(
    val context: Context,
    private val property_label_list: ArrayList<String>,
    private val property_label_value: ArrayList<Any>

    ): RecyclerView.Adapter<EventOverviewAdapter.EventPropertyViewHolder>() {
    class EventPropertyViewHolder(itemView: RqInterceptorSingleDesignEventsOverviewBinding) :RecyclerView.ViewHolder(itemView.root) {
        val label=itemView.rqInterceptorPropertyLabel
        val propertyValue=itemView.rqInterceptorPropertyValue
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventPropertyViewHolder {
        val viewBinding = RqInterceptorSingleDesignEventsOverviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventPropertyViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: EventPropertyViewHolder, position: Int) {
        holder.label.text= property_label_list[position].toString()
        holder.propertyValue.text=property_label_value[position].toString()

    }

    override fun getItemCount(): Int {
       return property_label_list.size
    }
}
