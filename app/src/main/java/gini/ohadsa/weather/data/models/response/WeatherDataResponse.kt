package gini.ohadsa.weather.data.models.response

import gini.ohadsa.weather.data.models.WeatherLocation


data class WeatherDataResponse(
    val message :  String,
    val data : WeatherLocation
)