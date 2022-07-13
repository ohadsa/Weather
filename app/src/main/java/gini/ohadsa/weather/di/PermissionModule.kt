package gini.ohadsa.weather.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.weather.utils.permissions.PermissionRequestHandler
import gini.ohadsa.weather.utils.permissions.PermissionRequestHandlerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

    @Binds
    abstract fun bindPermissionHandler(impl: PermissionRequestHandlerImpl): PermissionRequestHandler
}