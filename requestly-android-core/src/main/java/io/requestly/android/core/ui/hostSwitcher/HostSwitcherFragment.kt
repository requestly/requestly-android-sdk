package io.requestly.android.core.ui.hostSwitcher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.core.R
import io.requestly.android.core.databinding.FragmentHostSwitcherBinding

class HostSwitcherFragment : Fragment() {

    private lateinit var mainBinding: FragmentHostSwitcherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentHostSwitcherBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        with(mainBinding) {
            initRecyclerView()
        }

        return mainBinding.root
    }

    private fun initRecyclerView() {
        mainBinding.hostSwitcherRulesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}
