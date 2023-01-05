package io.requestly.android.core.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.requestly.android.core.R
import io.requestly.android.core.RequestlyLaunchFlow
import io.requestly.android.core.databinding.ActivityMainRequestlyBinding
import io.requestly.android.core.navigation.NavigationFlow
import io.requestly.android.core.navigation.Navigator
import io.requestly.android.core.navigation.ToFlowNavigatable


class MainRequestlyActivity : AppCompatActivity(), ToFlowNavigatable {

    companion object {
        private const val INTENT_NAME_STARTING_FLOW = "MainRequestlyActivity.STARTING_FLOW"

        fun getLaunchIntent(content: Context, flags: Int, flow: RequestlyLaunchFlow): Intent {
            return Intent(content, MainRequestlyActivity::class.java).apply {
                addFlags(flags)
                putExtra(INTENT_NAME_STARTING_FLOW, flow.toString())
            }
        }
    }

    private lateinit var binding: ActivityMainRequestlyBinding
    private var navigator = Navigator()

    private val viewModel: MainRequestlyActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRequestlyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavView.setupWithNavController(navController)

        // Top Level Destinations for AppBar. To show the up button properly
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.network_home_fragment,
                R.id.analytics_home_fragment,
                R.id.host_switcher_fragment,
                R.id.logs_home_fragment,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navigator.navController = navController
        setupMenu()

        binding.updateNotifyStripView.visibility = View.GONE
        viewModel.versionUpdateLiveData.observe(this) { data ->
            if (data == null) {
                binding.updateNotifyStripView.visibility = View.GONE
                return@observe
            }
            binding.updateNotifyStripView.visibility = View.VISIBLE
            binding.updateNotifyStripText.text = data.displayText
            binding.updateNotifyStripButton.text = data.ctaText
            binding.updateNotifyStripButton.setOnClickListener {
                val intent: Intent =
                    Intent(Intent.ACTION_VIEW).setData(Uri.parse(data.url))
                startActivity(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handleIntent()
    }

    override fun onSupportNavigateUp(): Boolean {
        return (findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp())
    }

    private fun handleIntent() {
        val startFlow = intent.getStringExtra(INTENT_NAME_STARTING_FLOW) ?: return

        when(RequestlyLaunchFlow.valueOf(startFlow)) {
            RequestlyLaunchFlow.ANALYTICS -> {
                navigator.navigateToFlow(NavigationFlow.AnalyticsFlow)
            }
            RequestlyLaunchFlow.MAIN -> {
                navigator.navigateToFlow(NavigationFlow.NetworkFlow)
            }
        }
    }

    private fun setupMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        })
    }

    // Required when navigation from X module to Y. Eg. Network -> Analytics
    override fun navigateToFlow(flow: NavigationFlow) {
        navigator.navigateToFlow(flow)
    }
}
