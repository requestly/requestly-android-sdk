package io.requestly.android.event.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.databinding.FragmentAnalyticsHomeBinding
import io.requestly.android.event.internal.data.repository.RepositoryProvider
import io.requestly.android.event.ui.adapter.EventListAdapter

class AnalyticsHomeFragment : Fragment() {
    private lateinit var mainBinding: FragmentAnalyticsHomeBinding
    private val viewModel: EventListViewModel by viewModels()
    private lateinit var eventsListAdapter: EventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryProvider.initialize(requireActivity().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentAnalyticsHomeBinding.inflate(layoutInflater)
        initEventRecyclerView()
        initEventListeners()

//      initToolbar()
        return mainBinding.root
    }

//    private fun initToolbar() {
//        setSupportActionBar(mainBinding.toolbar)
//        mainBinding.toolbar.subtitle = applicationName
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.events_list_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.clear -> {
//                viewModel.clearTransactions()
//                true
//            }
//            else -> {
//                super.onOptionsItemSelected(item)
//            }
//        }
//    }

    private fun initEventListeners() {
        viewModel.events.observe(
            viewLifecycleOwner
        ) {
                eventsList ->
            eventsListAdapter.updateEventsList(eventsList)
            mainBinding.tutorialGroup.isVisible = eventsList.isEmpty()
        }
    }

    private fun initEventRecyclerView() {
        mainBinding.rqInterceptorAllEventsRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        eventsListAdapter = EventListAdapter(requireContext()) {
            eventId ->
            Log.d("Requestly","Clicked Analytics Event")
            findNavController().navigate(AnalyticsHomeFragmentDirections.actionAnalyticsHomeFragmentToEventOverviewFragment(eventId))
        }
        mainBinding.rqInterceptorAllEventsRecyclerview.adapter = eventsListAdapter
    }
}
