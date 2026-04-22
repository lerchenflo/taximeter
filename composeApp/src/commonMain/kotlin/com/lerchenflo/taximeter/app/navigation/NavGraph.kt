package com.lerchenflo.taximeter.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.lerchenflo.taximeter.passenger.presentation.passenger_list.PassengerListRoot
import com.lerchenflo.taximeter.passenger.presentation.passenger_routes.PassengerRoutesRoot
import com.lerchenflo.taximeter.routemap.presentation.RouteMapRoot
import com.lerchenflo.taximeter.settings.presentation.SettingsRoot
import com.lerchenflo.taximeter.taximeter.presentation.TaximeterRoot

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = PassengerListRoute,
        modifier = modifier
    ) {
        composable<PassengerListRoute> {
            PassengerListRoot(
                onPassengerClick = { passengerId ->
                    navController.navigate(PassengerRoutesRoute(passengerId))
                },
                onShowMap = {
                    navController.navigate(RouteMapRoute())
                },
                onOpenSettings = {
                    navController.navigate(SettingsRoute)
                }
            )
        }

        composable<PassengerRoutesRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PassengerRoutesRoute>()
            PassengerRoutesRoot(
                passengerId = route.passengerId,
                onStartRoute = { passengerId ->
                    navController.navigate(TaximeterRoute(passengerId))
                },
                onRouteClick = { passengerId, routeId ->
                    navController.navigate(TaximeterRoute(passengerId, routeId))
                },
                onShowMap = { passengerId ->
                    navController.navigate(RouteMapRoute(passengerId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<TaximeterRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TaximeterRoute>()
            TaximeterRoot(
                passengerId = route.passengerId,
                routeId = route.routeId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<RouteMapRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<RouteMapRoute>()
            RouteMapRoot(
                passengerId = route.passengerId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            SettingsRoot(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
