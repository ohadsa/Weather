package gini.ohadsa.weather.data.repository

import gini.ohadsa.weather.data.models.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getCurrentWeatherLocation(lat :Double , lng :Double ): Flow<WeatherLocation>
    fun getWeatherWithoutUpdateData(latitude: Double, longitude: Double): Flow<WeatherLocation>
    suspend fun refresh(latitude: Double, longitude: Double)
    suspend fun getLastLocation() : WeatherLocation?

}