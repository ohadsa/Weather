package gini.ohadsa.weather.ui.map

import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import gini.ohadsa.weather.data.models.WeatherLocation
import gini.ohadsa.weather.data.models.response.WeatherDataResponse
import gini.ohadsa.weather.data.repository.WeatherRepository
import gini.ohadsa.weather.utils.SharedPreferenceUtil.getFullAddress
import gini.ohadsa.weather.utils.defaultLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val externalScope: CoroutineScope,
    private val geocoder: Geocoder
) : ViewModel() {

    val locationClickedFlow = MutableStateFlow(defaultLocation)

    fun getAddresses(lat: Double, lng: Double): String {
        return geocoder.getFullAddress(lat, lng)
    }

    @OptIn(FlowPreview::class)
    fun weatherLocationFlow() = locationClickedFlow.flatMapMerge {
        weatherRepository.getWeatherWithoutUpdateData(it.first, it.second)
    }.shareIn(
        externalScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed(),
    )

}