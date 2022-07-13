package gini.ohadsa.weather.ui.home

import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gini.ohadsa.weather.data.models.WeatherLocation
import gini.ohadsa.weather.data.repository.WeatherRepository
import gini.ohadsa.weather.location.LocationRepository
import gini.ohadsa.weather.utils.SharedPreferenceUtil.getFullAddress
import gini.ohadsa.weather.utils.permissions.Permission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val externalScope: CoroutineScope,
    private val weatherRepository: WeatherRepository,
    private val geocoder: Geocoder
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    fun weatherLocationFlow(): SharedFlow<WeatherLocation> {

        return locationRepository.getLocations().flatMapLatest {
            Log.d("TAG", "inside get current")
            weatherRepository.getCurrentWeatherLocation(it.latitude, it.longitude)
        }.shareIn(
            externalScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed(),
        )
    }

    fun getAddresses(lat: Double, lng: Double): String {
        return geocoder.getFullAddress(lat, lng)
    }

    suspend fun getLastAddress() =
        weatherRepository.getLastLocation()


    val permissionResultFlow =
        MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)


}


data class PermissionData(
    val request: Permission,
    val rationale: String
)