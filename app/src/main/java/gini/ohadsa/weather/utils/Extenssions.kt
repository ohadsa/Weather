package gini.ohadsa.weather.utils

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import gini.ohadsa.weather.R
import gini.ohadsa.weather.data.models.WeatherLocation

fun Context.getNotificationManager() =
    getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager


fun toString(lat: Double, lon: Double): String {
    return "($lat, $lon)"
}

fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

/**
 * Helper functions to simplify permission checks/requests.
 */
fun Context.hasPermission(permission: String): Boolean {

    // Background permissions didn't exit prior to Q, so it's approved by default.
    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
    ) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

fun Context.hasPreciseLocation(): Boolean =
    hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

fun Fragment.hasPreciseLocation(): Boolean =
    requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

fun Context.hasCoarseLocation(): Boolean =
    hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

fun Fragment.hasCoarseLocation(): Boolean =
    requireContext().hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

fun Context.hasLocationPermission(): Boolean =
    hasPreciseLocation() or hasCoarseLocation()

fun Fragment.hasLocationPermission(): Boolean =
    requireContext().hasLocationPermission()

fun Context.hasBackgroundPermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        true
    }

fun Fragment.hasBackgroundPermission(): Boolean =
    requireContext().hasBackgroundPermission()


fun Fragment.requestPermissionLauncher(callback: (Boolean) -> Unit) =
    registerForActivityResult(ActivityResultContracts.RequestPermission(), callback)

fun Fragment.requestPermissionsLauncher(callback: (Map<String, Boolean>) -> Unit) =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), callback)

/**
 * Provides access to SharedPreferences for location to Activities and Services.
 */
internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    const val KEY_DID_REQUEST_PERMISSION_ALREADY = "DidRequestPermissionAlready"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }

    fun getDidRequestPermissionAlready(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
            .getBoolean(KEY_DID_REQUEST_PERMISSION_ALREADY, false)

    fun saveDidRequestPermissionAlready(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).edit {
            putBoolean(KEY_DID_REQUEST_PERMISSION_ALREADY, requestingLocationUpdates)
        }


    fun Double.toCelsius(): String {
        return "${((this - 32) * 5 / 9).toInt()}°C"
    }

    fun String.costumeUri(): Uri {
        val header = "https://assetambee.s3-us-west-2.amazonaws.com/weatherIcons/PNG/"
        return Uri.parse("$header$this.png")
    }

    fun ImageView.loadImage(costumeUri: Uri) {
        Picasso
            .get()
            .load(costumeUri)
            .into(this)
    }

    fun Geocoder.getFullAddress(lat: Double, lng: Double) : String{
        return try{
            val addresses: Address = this.getFromLocation(lat, lng, 1)[0]
            val city = addresses.locality ?: ""
            val state = addresses.countryName ?: ""
            "$state $city"
        } catch (e:Exception){
            "Unknown"
        }
    }
}

val defaultLocation= 32.160280 to 34.809770
const val defaultPlace = "Israel Herzliya"
const val defaultSummery = "error getting location, No Internet"


