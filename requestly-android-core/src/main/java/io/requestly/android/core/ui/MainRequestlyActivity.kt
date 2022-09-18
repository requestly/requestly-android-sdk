package io.requestly.android.core.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.requestly.android.core.R
import io.requestly.android.core.databinding.ActivityMainRequestlyBinding
import io.requestly.android.core.navigation.NavigationFlow
import io.requestly.android.core.navigation.Navigator
import io.requestly.android.core.navigation.ToFlowNavigatable


class MainRequestlyActivity : AppCompatActivity(), ToFlowNavigatable {

    private lateinit var binding: ActivityMainRequestlyBinding
    private var navigator = Navigator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRequestlyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavView.setupWithNavController(navController)
        navigator.navController = navController
        setupMenu()
    }

    private fun setupMenu() {
        addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                // TODO: Hack Right now. This gets called before the fragment menu clicks
                return false
            }
        })
    }

    // Required when navigation from X module to Y. Eg. Network -> Analytics
    override fun navigateToFlow(flow: NavigationFlow) {
        navigator.navigateToFlow(flow)
    }
}
