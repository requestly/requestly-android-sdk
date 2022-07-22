package io.requestly.android.event.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.R
import io.requestly.android.event.ui.adapter.EventOverviewAdapter
import io.requestly.android.event.databinding.ActivityEventOverviewBinding

class EventOverviewActivity : AppCompatActivity() {
    private lateinit var binding:ActivityEventOverviewBinding
    private val viewModel: EventOverviewViewModel by viewModels {
        EventOverviewViewModelFactory(
            intent.getLongExtra((R.string.INTENT_EVENT_ID_KEY.toString()), 0)
        )
    }
    private lateinit var eventOverviewAdapter: EventOverviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEventOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rqInterceptorEventOverviewRecyclerview.layoutManager=LinearLayoutManager(this)

        eventOverviewAdapter = EventOverviewAdapter(viewModel.event.value, this@EventOverviewActivity)
        binding.rqInterceptorEventOverviewRecyclerview.adapter = eventOverviewAdapter

        viewModel.event.observe(this) { event ->
            if (event != null) {
                binding.eventOverviewTitle.text = event.eventName
                eventOverviewAdapter.updateEvent(event)
            }
        }
    }
}
