package gini.ohadsa.weather.services

import android.app.Notification
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.weather.MainActivity
import gini.ohadsa.weather.R
import gini.ohadsa.weather.location.LocationRepository
import gini.ohadsa.weather.utils.SharedPreferenceUtil
import gini.ohadsa.weather.utils.notifications.NotificationHandler
import gini.ohadsa.weather.utils.toText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ForegroundLocationService : LifecycleService() {

    private var configurationChange = false
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()

    private var currentLocation: Location? = null
    private var locationFlow: Job? = null

    lateinit var notificationHandler: NotificationHandler
    lateinit var repository: LocationRepository

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MyEntryPoint {
        fun getNotification(): NotificationHandler
        fun getLocationRepository(): LocationRepository
    }

    override fun onCreate() {

        Log.d(TAG, "onCreate()")
        notificationHandler =
            EntryPoints.get(applicationContext, MyEntryPoint::class.java).getNotification()
        repository =
            EntryPoints.get(applicationContext, MyEntryPoint::class.java).getLocationRepository()


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)
                ?: false
        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d(TAG, "Start foreground service")
            startForeground(NOTIFICATION_ID, generateNotification(currentLocation))
            serviceRunningInForeground = true
        }
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    inner class LocalBinder : Binder() {
        internal val service: ForegroundLocationService
            get() = this@ForegroundLocationService
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")

        SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        startService(Intent(applicationContext, ForegroundLocationService::class.java))

        locationFlow = lifecycleScope.launchWhenStarted {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getLocations()
                    .onEach {
                        Log.d(TAG, "Service location: ${it.toText()}")
                        currentLocation = it
                    }.collect {
                        // Updates notification content if this service is running as a foreground
                        // service.
                        if (serviceRunningInForeground) {
                            notificationHandler.updateNotification(it.toText())
                        }
                    }
            }
        }

    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")
        locationFlow?.cancel()
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)
    }

    private fun generateNotification(location: Location?): Notification {
        val mainNotificationText = location?.toText() ?: getString(R.string.no_location_text)

        notificationHandler.initNotificationParams(
            channelID = NOTIFICATION_CHANNEL_ID,
            channelName = NOTIFICATION_CHANNEL_ID,
            notificationID = NOTIFICATION_ID,
            priority = NotificationCompat.PRIORITY_HIGH,
            contentText = mainNotificationText,
            style = null,
            icon = null,
            title = null,
            directTo = MainActivity::class.java
        )

        return notificationHandler.getNotification()
    }

    companion object {
        private const val TAG = "ForegroundOnlyLocationService"
        private const val PACKAGE_NAME = "gini.ohadsa.weather.whileinuselocation"
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
        private const val NOTIFICATION_ID = 12345678
        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
    }
}
