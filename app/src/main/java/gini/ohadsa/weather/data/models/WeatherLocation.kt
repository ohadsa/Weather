package gini.ohadsa.weather.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "WeatherLocation", primaryKeys = ["lat", "lng"])
open class WeatherLocation(
    val lat: Double,
    val lng: Double,
    val temperature: Double,
    val windBearing: Double,
    val apparentTemperature: Double,
    val dewPoint: Double,
    val windGust: Double,
    val cloudCover: Double,
    val visibility: Double,
    val humidity: Double,
    val uvIndex: Double,
    val time: Long,
    val summary: String,
    @SerializedName("icon")
    val icon: String,
    val precipiceIntensity: Double
)

@Entity(tableName = "CurrentLocation")
class CurrentLocation(
    lat: Double,
    lng: Double,
    temperature: Double,
    windBearing: Double,
    apparentTemperature: Double,
    dewPoint: Double,
    windGust: Double,
    cloudCover: Double,
    visibility: Double,
    humidity: Double,
    uvIndex: Double,
    time: Long,
    summary: String,
    icon: String,
    precipiceIntensity: Double,
    @PrimaryKey
    val key: String = CONST_PRIMARY_KEY,
) : WeatherLocation(
    lat,
    lng,
    temperature,
    windBearing,
    apparentTemperature,
    dewPoint,
    windGust,
    cloudCover,
    visibility,
    humidity,
    uvIndex,
    time,
    summary,
    icon,
    precipiceIntensity
)

const val CONST_PRIMARY_KEY = "KEY"




