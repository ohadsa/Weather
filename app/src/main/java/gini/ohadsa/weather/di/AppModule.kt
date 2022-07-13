package gini.ohadsa.weather.di

import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.view.View
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.weather.location.SharedLocationManager
import gini.ohadsa.weather.network.NetworkStatusChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    fun provideSharedLocationManager(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope
    ): SharedLocationManager =
        SharedLocationManager(context,coroutineScope)

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context) : Geocoder = Geocoder(context)

    @Provides
    @Singleton
    fun provideNetworkStatusChecker(@ApplicationContext context: Context) : NetworkStatusChecker =
        NetworkStatusChecker(
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )


}