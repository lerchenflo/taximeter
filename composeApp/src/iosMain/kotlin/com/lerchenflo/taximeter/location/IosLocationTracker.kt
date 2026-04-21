package com.lerchenflo.taximeter.location

import com.lerchenflo.taximeter.taximeter.domain.LocationPoint
import com.lerchenflo.taximeter.taximeter.domain.LocationTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class IosLocationTracker : LocationTracker {
    override fun startTracking(): Flow<LocationPoint> = emptyFlow()
    override fun stopTracking() {}
}
