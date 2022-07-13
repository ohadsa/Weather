package gini.ohadsa.weather.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.weather.db.DataSource
import gini.ohadsa.weather.db.DataSourceImpl
import gini.ohadsa.weather.db.WeatherDatabase
import gini.ohadsa.weather.db.WeatherLocationDao
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context): WeatherDatabase =
        Room.databaseBuilder(appContext, WeatherDatabase::class.java, WeatherDatabase.DB_NAME)
            .fallbackToDestructiveMigration()
            //.addTypeConverter(Converters())
            .build()


    @Provides
    fun provideWeatherLocationDao(weatherDatabase: WeatherDatabase): WeatherLocationDao =
        weatherDatabase.weatherLocationDao()

}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceProvider{

    @Binds
    @Singleton
    abstract fun bindDataSource(impl: DataSourceImpl) : DataSource

}



