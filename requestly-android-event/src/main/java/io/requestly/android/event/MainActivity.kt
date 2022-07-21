package io.requestly.android.event

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.core.NotificationHelper
import io.requestly.android.event.Adapters.AllEventsAdapter
import io.requestly.android.event.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), AllEventsAdapter.OnEventClickedListener {
    var EventList = ArrayList<EventModel>()
    private lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setUpNotification()
        }
        fetchData()
        setAllEventRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setUpNotification() {
        val notificationHelper = NotificationHelper(
            this, EVENTS_CHANNEL_ID,
            EVENTS_NOTIFICATION_ID, "Events Notify"
        )
        val notificationBuilder = notificationHelper.show("Recording Events", "home page visit")
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setStyle(addContentIntent())
            .setContentIntent(createContentIntent())
        NotificationManagerCompat.from(this)
            .notify(EVENTS_NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createContentIntent(): PendingIntent? {
        val intent = Intent(applicationContext, MainActivity::class.java)
        return PendingIntent.getActivity(this@MainActivity, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    private fun addContentIntent(): NotificationCompat.InboxStyle {
        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.addLine("static Banner shown ")
        inboxStyle.addLine("cart Footer Strip Shown ")
        inboxStyle.addLine("static Banner shown ")
        inboxStyle.addLine("cart Footer Strip Shown ")
        return inboxStyle
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun fetchData() {
        val e1 = EventModel("Static Banner Shown", 35819)
        val e2 = EventModel("Cart Footer Strip Shown", 45019)
        for (i in 1..5) {
            EventList.add(e1)
            EventList.add(e2)
        }
    }

    private fun setAllEventRecyclerView() {
        mainBinding.rqInterceptorAllEventsRecyclerview.layoutManager = LinearLayoutManager(this)
        val adapter = AllEventsAdapter(EventList, this@MainActivity, this@MainActivity)
        mainBinding.rqInterceptorAllEventsRecyclerview.adapter = adapter
    }

    override fun onEventClicked(event: String) {
        startActivity(Intent(this, EventsOverview::class.java))
    }

    companion object {
        private const val EVENTS_NOTIFICATION_ID = 12
        private const val EVENTS_CHANNEL_ID = "rq_interceptor_events"
    }
}
