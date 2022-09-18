package io.requestly.android.core.navigation

sealed class NavigationFlow {
    object NetworkFlow : NavigationFlow()
    object AnalyticsFlow: NavigationFlow()
//    class DashboardFlow(val msg: String) : NavigationFlow()
}
