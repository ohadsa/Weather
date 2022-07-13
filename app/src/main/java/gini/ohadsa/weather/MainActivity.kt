package gini.ohadsa.weather

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.weather.databinding.ActivityMainBinding
import gini.ohadsa.weather.services.ForegroundLocationService
import gini.ohadsa.weather.ui.home.HomeViewModel
import gini.ohadsa.weather.ui.home.PermissionData
import gini.ohadsa.weather.utils.permissions.Permission
import gini.ohadsa.weather.works.RefreshDataWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var locationServicesBounded = false
    private var foregroundLocationService: ForegroundLocationService? = null

    private val viewModel: HomeViewModel by viewModels()

    private val locationServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundLocationService.LocalBinder
            foregroundLocationService = binder.service
            locationServicesBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundLocationService = null
            locationServicesBounded = false
        }
    }

    override fun onStart() {
        super.onStart()
        bindService()

    }

    private fun bindService() {
        foregroundLocationService
        val serviceIntent = Intent(this, ForegroundLocationService::class.java)
        bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {

        if (locationServicesBounded) {
            unbindService(locationServiceConnection)
            locationServicesBounded = false
        }
        unsubscribeLocationService()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        collectPermissionResultFlow()

    }

    private fun collectPermissionResultFlow() {
        lifecycleScope.launch {
            viewModel.permissionResultFlow.collectLatest { result ->
                when (result) {
                    true -> {
                        subscribeLocationService()
                    }
                    false -> {
                        //TODO - set to the user dialog of not granted...
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun subscribeLocationService() {
        foregroundLocationService?.subscribeToLocationUpdates()
            ?: Log.d(TAG, "Service Not Bound")
    }

    private fun unsubscribeLocationService() {
        foregroundLocationService?.unsubscribeToLocationUpdates()
    }


}

private const val TAG = "MainActivity"
