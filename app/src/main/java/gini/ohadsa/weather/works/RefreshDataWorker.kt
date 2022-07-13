package gini.ohadsa.weather.works

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import gini.ohadsa.weather.data.repository.WeatherRepository
import gini.ohadsa.weather.location.LocationRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import retrofit2.HttpException


@HiltWorker
class RefreshDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val sharedPreferences: SharedPreferences

) : CoroutineWorker(appContext, workerParams) {


    override suspend fun doWork(): Result {

        try {
            locationRepository.getLocations().take(1).collect {
                weatherRepository.refresh(it.latitude, it.longitude)
            }
            Log.d("Shay", "refreshed data succeeded")
            val x = sharedPreferences.getInt(SP_KEY_S, 0)
            sharedPreferences.edit().putInt(SP_KEY_S, x + 1)

        } catch (e: Exception) {
            Log.d("TAG", e.message.toString())
            val x = sharedPreferences.getInt(SP_KEY_F, 0)
            sharedPreferences.edit().putInt(SP_KEY_F, x + 1)
            Result.retry()
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "gini.ohadsa.weather.refreshDataWorker"
        const val SP_KEY_S = "sp-Keu_s"
        const val SP_KEY_F = "sp-Keu_f"
    }
}