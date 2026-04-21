package com.lerchenflo.taximeter.location

import com.lerchenflo.taximeter.core.domain.location.LocationPoint
import com.lerchenflo.taximeter.core.domain.location.LocationTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosLocationTracker : LocationTracker {
    override fun startTracking(): Flow<LocationPoint> = emptyFlow()
    override fun stopTracking() {}
}
