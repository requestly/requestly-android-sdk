package io.requestly.android.event

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.Adapters.EventOverviewAdapter
import io.requestly.android.event.databinding.ActivityEventsOverviewBinding
import kotlin.collections.ArrayList

class EventsOverview : AppCompatActivity() {
    private lateinit var binding:ActivityEventsOverviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEventsOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setRecyclerView()
    }

    private fun setRecyclerView() {
        binding.rqInterceptorEventOverviewRecyclerview.layoutManager=LinearLayoutManager(this)
        val mappy:Map<String,Any> = mapOf("acquire_campaign" to "#-NA","acquire_source" to "#-NA","app_city" to "Delhi","cart_id" to 0,"cart_value" to 260.0)


        val adapter=EventOverviewAdapter(this@EventsOverview, ArrayList( mappy.keys),
            ArrayList(mappy.values)
        )
        binding.rqInterceptorEventOverviewRecyclerview.adapter=adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
