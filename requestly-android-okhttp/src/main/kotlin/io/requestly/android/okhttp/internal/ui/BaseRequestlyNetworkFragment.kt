package io.requestly.android.okhttp.internal.ui

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.requestly.android.okhttp.internal.data.repository.RepositoryProvider

internal abstract class BaseRequestlyNetworkFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryProvider.initialize(requireActivity().applicationContext)
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    companion object {
        var isInForeground: Boolean = false
            private set
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
