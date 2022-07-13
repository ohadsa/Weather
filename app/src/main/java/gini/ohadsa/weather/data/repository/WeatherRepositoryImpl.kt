package gini.ohadsa.weather.data.repository

import android.util.Log
import gini.ohadsa.weather.data.models.WeatherLocation
import gini.ohadsa.weather.db.DataSource
import gini.ohadsa.weather.network.NetworkStatusChecker
import gini.ohadsa.weather.network.WeatherApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private var weatherApi: WeatherApi,
    private var dataSource: DataSource,
    private var networkStatusChecker: NetworkStatusChecker
) : WeatherRepository {

    override fun getCurrentWeatherLocation(lat: Double, lng: Double): Flow<WeatherLocation> =
        if (networkStatusChecker.hasInternetConnection()) {
            flow {
                dataSource.getCurrent()?.let {
                    emit(it) // to get fast value from cache and than update
                }
                val tmp: WeatherLocation = weatherApi.weatherInLocation(lat, lng).data
                dataSource.insertLocationToLastPlace(tmp)
                emit(tmp)
            }
        } else {
                dataSource.getCurrentLocationFlow()

        }

    override suspend fun refresh(latitude: Double, longitude: Double) {
        dataSource.insertLocationToLastPlace(weatherApi.weatherInLocation(latitude, longitude).data)
    }

    override suspend fun getLastLocation() =
        dataSource.getCurrent()


    override fun getWeatherWithoutUpdateData(lat: Double, lng: Double) : Flow<WeatherLocation> =
        if (networkStatusChecker.hasInternetConnection()) {
            flow {
                emit(weatherApi.weatherInLocation(lat, lng).data)
            }
        } else {
            throw Exception("No internet Connection")
        }

}