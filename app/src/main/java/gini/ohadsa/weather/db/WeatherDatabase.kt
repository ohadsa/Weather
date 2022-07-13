package gini.ohadsa.weather.db

import androidx.room.Database
import androidx.room.RoomDatabase
import gini.ohadsa.weather.data.models.WeatherLocation

@Database(entities = [WeatherLocation::class ], version = 1, exportSchema = false)
abstract class WeatherDatabase: RoomDatabase() {
    abstract fun weatherLocationDao(): WeatherLocationDao

    companion object{
        const val DB_NAME = "gini.ohadsa.weather.WeatherDatabase.db"
    }
}
