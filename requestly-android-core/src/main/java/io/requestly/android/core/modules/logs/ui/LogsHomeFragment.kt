package io.requestly.android.core.modules.logs.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.requestly.android.core.R
import io.requestly.android.core.Requestly
import io.requestly.android.core.databinding.FragmentLogsHomeBinding
import io.requestly.android.core.modules.logs.lib.lynx.main.presenter.LynxRequestlyPresenter

class LogsHomeFragment: Fragment() {
    private lateinit var mainBinding: FragmentLogsHomeBinding
    private lateinit var menuHost: MenuHost
    private lateinit var logsPresenter: LynxRequestlyPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainBinding = FragmentLogsHomeBinding.inflate(layoutInflater)
        logsPresenter = Requestly.getInstance().logsLynxPresenter
        return mainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        menuHost = requireActivity()
        setupMenu()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupMenu() {
        menuHost.addMenuProvider(
            object: MenuProvider {
                override fun onPrepareMenu(menu: Menu) {
                    super.onPrepareMenu(menu)
                }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.logs_home_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.clear -> {
                            Log.d("Requestly", "Logs Cleared")
                            logsPresenter.clear()
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }
}
