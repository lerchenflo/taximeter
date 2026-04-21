package com.lerchenflo.taximeter.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.lerchenflo.taximeter.passenger.presentation.passenger_list.PassengerListScreenRoot
import com.lerchenflo.taximeter.passenger.presentation.passenger_routes.PassengerRoutesScreenRoot
import com.lerchenflo.taximeter.taximeter.presentation.TaximeterScreenRoot

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
            PassengerListScreenRoot(
                onPassengerClick = { passengerId ->
                    navController.navigate(PassengerRoutesRoute(passengerId))
                }
            )
        }

        composable<PassengerRoutesRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PassengerRoutesRoute>()
            PassengerRoutesScreenRoot(
                passengerId = route.passengerId,
                onStartRoute = { passengerId ->
                    navController.navigate(TaximeterRoute(passengerId))
                },
                onRouteClick = { passengerId, routeId ->
                    navController.navigate(TaximeterRoute(passengerId, routeId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<TaximeterRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TaximeterRoute>()
            TaximeterScreenRoot(
                passengerId = route.passengerId,
                routeId = route.routeId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
