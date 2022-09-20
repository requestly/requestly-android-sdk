package io.requestly.android.event.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.R
import io.requestly.android.event.databinding.FragmentAnalyticsHomeBinding
import io.requestly.android.event.internal.data.repository.RepositoryProvider
import io.requestly.android.event.ui.adapter.EventListAdapter

class AnalyticsHomeFragment : Fragment() {
    private lateinit var mainBinding: FragmentAnalyticsHomeBinding
    private val viewModel: EventListViewModel by viewModels()
    private lateinit var eventsListAdapter: EventListAdapter

    private lateinit var menuHost: MenuHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryProvider.initialize(requireActivity().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainBinding = FragmentAnalyticsHomeBinding.inflate(layoutInflater)
        initEventRecyclerView()
        initEventListeners()
        return mainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        menuHost = requireActivity()
        setupMenu()
    }

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

    private fun setupMenu() {
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.events_list_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return when (menuItem.itemId) {
                    R.id.clear -> {
                        Log.d("Requestly", "Cleared")
                        viewModel.clearTransactions()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
