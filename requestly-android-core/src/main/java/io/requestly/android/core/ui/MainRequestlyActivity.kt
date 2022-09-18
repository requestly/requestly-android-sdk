package io.requestly.android.core.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }

    // Required when navigation from X module to Y. Eg. Network -> Analytics
    override fun navigateToFlow(flow: NavigationFlow) {
        navigator.navigateToFlow(flow)
    }
}
