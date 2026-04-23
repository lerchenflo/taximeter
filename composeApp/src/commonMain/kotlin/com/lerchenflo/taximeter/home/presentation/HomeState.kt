package com.lerchenflo.taximeter.home.presentation

import com.lerchenflo.taximeter.datasource.database.entities.RouteWithPassenger

data class HomeState(
    val recentRoutes: List<RouteWithPassenger> = emptyList(),
    val isLoading: Boolean = true
)
