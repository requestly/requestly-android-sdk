package io.requestly.android.event

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.Adapters.AllEventsAdapter
import io.requestly.android.event.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), AllEventsAdapter.OnEventClickedListener{
    var EventList= ArrayList<EventModel>()
    private lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mainBinding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        fetchData()
        setAllEventRecyclerView()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun fetchData() {
        val e1= EventModel("Static Banner Shown",35819)
        val e2= EventModel("Cart Footer Strip Shown",45019)
        for(i in 1..5){
            EventList.add(e1)
            EventList.add(e2)
        }
    }

    private fun setAllEventRecyclerView(){
        mainBinding.rqInterceptorAllEventsRecyclerview.layoutManager=LinearLayoutManager(this)
        val adapter=AllEventsAdapter(EventList,this@MainActivity,this@MainActivity)
        mainBinding.rqInterceptorAllEventsRecyclerview.adapter=adapter
    }

    override fun onEventClicked(event: String) {
        startActivity(Intent(this,EventsOverview::class.java))
    }
}
