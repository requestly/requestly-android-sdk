package io.requestly.android.core.modules.sharedPrefViewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.requestly.android.core.databinding.FragmentSharedPrefViewerBinding

class SharedPrefViewerFragment : Fragment() {

    private lateinit var mainBinding: FragmentSharedPrefViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = FragmentSharedPrefViewerBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainBinding = FragmentSharedPrefViewerBinding.inflate(layoutInflater)

        // Inflate the layout for this fragment
        initRecyclerView()

        return mainBinding.root
    }

    private fun initRecyclerView() {
        mainBinding.sharedPrefsEntryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adaptor = SharedPrefLineItemAdaptor(emptyList())
        mainBinding.sharedPrefsEntryRecyclerView.adapter = adaptor
    }
}
