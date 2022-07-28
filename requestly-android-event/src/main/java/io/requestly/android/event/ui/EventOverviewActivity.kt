package io.requestly.android.event.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.R
import io.requestly.android.event.databinding.ActivityEventOverviewBinding
import io.requestly.android.event.ui.adapter.EventOverviewAdapter

class EventOverviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventOverviewBinding
    private val viewModel: EventOverviewViewModel by viewModels {
        EventOverviewViewModelFactory(
            intent.getLongExtra((R.string.INTENT_EVENT_ID_KEY.toString()), 0)
        )
    }
    private lateinit var eventOverviewAdapter: EventOverviewAdapter

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rqInterceptorEventOverviewRecyclerview.layoutManager = LinearLayoutManager(this)

        eventOverviewAdapter = EventOverviewAdapter(viewModel.event.value, this@EventOverviewActivity)
        binding.rqInterceptorEventOverviewRecyclerview.adapter = eventOverviewAdapter

        viewModel.event.observe(this) { event ->
            if (event != null) {
                binding.eventOverviewTitle.text = event.eventName
                eventOverviewAdapter.updateEvent(event)
            }
        }

        initToolbar()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.subtitle = applicationName
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.event_overview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
