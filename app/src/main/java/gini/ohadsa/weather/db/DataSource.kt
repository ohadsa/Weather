package gini.ohadsa.weather.db


import gini.ohadsa.weather.data.models.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface DataSource {

    suspend fun insertLocationToLastPlace(location: WeatherLocation)
    suspend fun delete()
    fun getCurrentLocationFlow(): Flow<WeatherLocation>
    suspend fun getCurrent() : WeatherLocation?



}