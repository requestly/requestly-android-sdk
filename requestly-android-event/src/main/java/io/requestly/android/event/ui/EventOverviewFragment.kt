package io.requestly.android.event.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.event.databinding.FragmentEventOverviewBinding
import io.requestly.android.event.ui.adapter.EventOverviewAdapter

class EventOverviewFragment : Fragment() {
    private lateinit var binding: FragmentEventOverviewBinding

    private lateinit var viewModel: EventOverviewViewModel
    private lateinit var eventOverviewAdapter: EventOverviewAdapter

//    private val applicationName: CharSequence
//        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventOverviewBinding.inflate(layoutInflater)
        binding.rqInterceptorEventOverviewRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        val arguments = EventOverviewFragmentArgs.fromBundle(requireArguments())
        val viewModelCopy: EventOverviewViewModel by viewModels {
            EventOverviewViewModelFactory(arguments.eventId)
        }
        viewModel = viewModelCopy

        eventOverviewAdapter = EventOverviewAdapter(viewModel.event.value, requireContext())
        binding.rqInterceptorEventOverviewRecyclerview.adapter = eventOverviewAdapter

        viewModel.event.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                binding.eventOverviewTitle.text = event.eventName
                eventOverviewAdapter.updateEvent(event)
            }
        }

//        initToolbar()
        return binding.root
    }

//    private fun initToolbar() {
//        setSupportActionBar(binding.toolbar)
//        binding.toolbar.subtitle = applicationName
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.event_overview_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            else -> {
//                super.onOptionsItemSelected(item)
//            }
//        }
//    }
}
