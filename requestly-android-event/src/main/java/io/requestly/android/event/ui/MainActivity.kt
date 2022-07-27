package io.requestly.android.event.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.R
import io.requestly.android.event.ui.adapter.EventListAdapter
import io.requestly.android.event.databinding.ActivityMainBinding
import io.requestly.android.event.internal.data.repository.RepositoryProvider

class MainActivity : AppCompatActivity(){
    private lateinit var mainBinding: ActivityMainBinding
    private val viewModel: EventListViewModel by viewModels()
    private lateinit var eventsListAdapter: EventListAdapter

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryProvider.initialize(applicationContext)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        initEventRecyclerView()
        initEventListeners()
        initToolbar()
    }

    private fun initToolbar() {
        setSupportActionBar(mainBinding.toolbar)
        mainBinding.toolbar.subtitle = applicationName
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.events_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.clear -> {
                viewModel.clearTransactions()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initEventListeners() {
        viewModel.events.observe(
            this
        ) { eventsList -> eventsListAdapter.updateEventsList(eventsList) }
    }

    private fun initEventRecyclerView(){
        mainBinding.rqInterceptorAllEventsRecyclerview.layoutManager = LinearLayoutManager(this)
        eventsListAdapter = EventListAdapter(
            this
        ) {
            eventId ->
                val intent = Intent(this, EventOverviewActivity::class.java)
                intent.putExtra(R.string.INTENT_EVENT_ID_KEY.toString(), eventId)
                this.startActivity(intent)
        }
        mainBinding.rqInterceptorAllEventsRecyclerview.adapter = eventsListAdapter
    }
}
