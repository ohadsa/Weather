package gini.ohadsa.weather.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.weather.data.repository.WeatherRepository
import gini.ohadsa.weather.data.repository.WeatherRepositoryImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule{


    @Binds
    @Singleton
    abstract fun bindRepository(impl: WeatherRepositoryImpl) : WeatherRepository

}
