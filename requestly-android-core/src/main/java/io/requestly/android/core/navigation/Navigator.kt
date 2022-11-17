package io.requestly.android.core.navigation

import androidx.navigation.NavController
import io.requestly.android.core.RqMainNavGraphDirections

class Navigator {
    lateinit var navController: NavController

//     navigate on main thread or nav component crashes sometimes
    fun navigateToFlow(navigationFlow: NavigationFlow) = when (navigationFlow) {
        is NavigationFlow.NetworkFlow -> navController.navigate(
            RqMainNavGraphDirections.actionGlobalNetworkFlow()
        )
        is NavigationFlow.AnalyticsFlow -> navController.navigate(
            RqMainNavGraphDirections.actionGlobalAnalyticsFlow()
        )
        is NavigationFlow.HostSwitcherFlow -> navController.navigate(
            RqMainNavGraphDirections.actionGlobalHostSwitcher()
        )
//        is NavigationFlow.DashboardFlow -> navController.navigate(
//            RqMainNavGraphDirections.actionGlobalDashboardFlow(navigationFlow.msg)
//        )
    }
}
