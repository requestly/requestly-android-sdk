package io.requestly.android.core.navigation

import androidx.navigation.NavController
import io.requestly.android.core.MainNavGraphDirections

class Navigator {
    lateinit var navController: NavController

//     navigate on main thread or nav component crashes sometimes
    fun navigateToFlow(navigationFlow: NavigationFlow) = when (navigationFlow) {
        is NavigationFlow.NetworkFlow -> navController.navigate(
            MainNavGraphDirections.actionGlobalNetworkFlow()
        )
        is NavigationFlow.AnalyticsFlow -> navController.navigate(
            MainNavGraphDirections.actionGlobalAnalyticsFlow()
        )
//        is NavigationFlow.DashboardFlow -> navController.navigate(
//            MainNavGraphDirections.actionGlobalDashboardFlow(navigationFlow.msg)
//        )
    }
}
