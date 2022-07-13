package gini.ohadsa.weather.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper

import android.util.Log
import com.google.android.gms.location.*
import gini.ohadsa.weather.utils.hasPermission
import gini.ohadsa.weather.utils.toText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

private const val TAG: String = "gini.ohadsa.weather.location"
class SharedLocationManager constructor(
    private val context: Context,
    externalScope: CoroutineScope
) {

    private val _receivingLocationUpdates: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    val receivingLocationUpdates: StateFlow<Boolean>
        get() = _receivingLocationUpdates

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(10)
        fastestInterval = TimeUnit.SECONDS.toMillis(10)
        maxWaitTime = TimeUnit.SECONDS.toMillis(10)
        priority = Priority.PRIORITY_HIGH_ACCURACY//LocationRequest.PRIORITY_HIGH_ACCURACY//100
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _locationUpdates = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                Log.d(TAG, "New location: ${result.lastLocation.toText()}")
                // Send the new location to the Flow observer
                result.lastLocation?.let {
                    trySend(it)
                }
            }
        }

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) close()

        Log.d(TAG, "Starting location updates")
        _receivingLocationUpdates.value = true

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e) // in case of exception, close the Flow
        }

        awaitClose {
            Log.d(TAG, "Stopping location updates")
            _receivingLocationUpdates.value = false
            fusedLocationClient.removeLocationUpdates(callback) // clean up when Flow collection ends
        }
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    @ExperimentalCoroutinesApi
    fun locationFlow(): Flow<Location> {
        return _locationUpdates
    }
}