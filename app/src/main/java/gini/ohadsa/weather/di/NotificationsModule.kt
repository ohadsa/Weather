package gini.ohadsa.weather.di

import android.app.NotificationManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.weather.utils.notifications.NotificationHandler
import gini.ohadsa.weather.utils.notifications.NotificationHandlerImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    abstract fun bindNotificationManager(impl: NotificationHandlerImpl): NotificationHandler
}

@Module
@InstallIn(SingletonComponent::class)
object NotificationManagerProvider {
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}