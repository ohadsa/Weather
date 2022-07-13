package gini.ohadsa.weather.db

import gini.ohadsa.weather.data.models.WeatherLocation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/*
this interface and impl is a layer between the repository and the database, and also
for facade all dao to one place ( right now there is only one)
 */
class DataSourceImpl @Inject constructor(
    private val weatherLocationDao: WeatherLocationDao
) : DataSource {

    override suspend fun insertLocationToLastPlace(location: WeatherLocation) =
        weatherLocationDao.replaceLastLocation(location)


    override suspend fun delete() =
        weatherLocationDao.delete()

    override fun getCurrentLocationFlow(): Flow<WeatherLocation> =
        weatherLocationDao.getCurrentLocationFlow()

    override suspend fun getCurrent(): WeatherLocation? =
        weatherLocationDao.getCurrent()


}