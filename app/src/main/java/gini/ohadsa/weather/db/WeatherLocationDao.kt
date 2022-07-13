package gini.ohadsa.weather.db

import androidx.room.*
import gini.ohadsa.weather.data.models.WeatherLocation
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherLocationDao {

    @Transaction
    suspend fun replaceLastLocation(location: WeatherLocation ){
        delete()
        insertNewLocation(location)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    suspend fun insertNewLocation(location: WeatherLocation)

    @Query("DELETE FROM WeatherLocation")
    suspend fun delete()


    @Query("SELECT * FROM WeatherLocation ")
    fun getCurrentLocationFlow(): Flow<WeatherLocation>


    @Query("SELECT * FROM WeatherLocation ")
    fun getCurrent() : WeatherLocation?




}

