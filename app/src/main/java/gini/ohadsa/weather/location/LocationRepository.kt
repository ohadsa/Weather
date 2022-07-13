package gini.ohadsa.weather.location


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LocationRepository @Inject constructor(
        private val sharedLocationManager: SharedLocationManager
) {

    val receivingLocationUpdates: StateFlow<Boolean> = sharedLocationManager.receivingLocationUpdates

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLocations() = sharedLocationManager.locationFlow()
}