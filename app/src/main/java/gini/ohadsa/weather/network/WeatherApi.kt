package gini.ohadsa.weather.network

import gini.ohadsa.weather.data.models.response.WeatherDataResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApi {


    @GET("weather/latest/by-lat-lng")
    suspend fun weatherInLocation(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): WeatherDataResponse


}


