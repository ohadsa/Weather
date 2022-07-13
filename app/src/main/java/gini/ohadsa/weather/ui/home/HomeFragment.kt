package gini.ohadsa.weather.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.weather.R
import gini.ohadsa.weather.data.models.WeatherLocation
import gini.ohadsa.weather.databinding.FragmentHomeBinding
import gini.ohadsa.weather.network.NetworkStatusChecker
import gini.ohadsa.weather.utils.SharedPreferenceUtil.costumeUri
import gini.ohadsa.weather.utils.SharedPreferenceUtil.loadImage
import gini.ohadsa.weather.utils.SharedPreferenceUtil.toCelsius
import gini.ohadsa.weather.utils.defaultSummery
import gini.ohadsa.weather.utils.permissions.Permission
import gini.ohadsa.weather.utils.permissions.PermissionRequestHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    @Inject
    lateinit var permissionManager: PermissionRequestHandler

    @Inject
    lateinit var networkStatusChecker: NetworkStatusChecker


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUnbarMenu()
        permissionManager.from(this)
        if (!foregroundPermissionApproved()) checkPermissionAndCollectLocations()
        else collectLocationFlow()
        observeConnectivityChanges()

    }

    private fun observeConnectivityChanges() {
        networkStatusChecker.addCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                showSnakeBar(getString(R.string._internet_available_label))
            }

            override fun onLost(network: Network) =
                showSnakeBar(getString(R.string.no_internet_label))
        })
        if (!networkStatusChecker.hasInternetConnection()) showSnakeBar(getString(R.string.no_internet_label))
    }


    private fun showSnakeBar(msg: String) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            msg,
            Snackbar.LENGTH_SHORT
        )
            .show()
    }

    private fun checkPermissionAndCollectLocations() {
        permissionManager
            .request(Permission.Location)
            .rationale(getString(R.string.permission_rationale))
            .checkDetailedPermission { result ->
                if (result.getValue(Permission.Location)) {
                    lifecycleScope.launch {
                        viewModel.permissionResultFlow.emit(true)
                    }
                    collectLocationFlow()
                } else {
                    lifecycleScope.launch {
                        viewModel.permissionResultFlow.emit(false)
                    }

                }
            }
        lifecycleScope.launch(Dispatchers.Main) {
            if (!foregroundPermissionApproved()) getLastLocation()
        }

    }

    private fun getLastLocation() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getLastAddress()?.let {
                withContext(Dispatchers.Main) {
                    updateUI(it)
                    binding.loadProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun collectLocationFlow() {
        lifecycleScope.launchWhenResumed {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                binding.loadProgressBar.visibility = View.VISIBLE
                viewModel.weatherLocationFlow().collectLatest {
                    updateUI(it)
                    binding.loadProgressBar.visibility = View.GONE
                }
            }
        }
    }


    private fun initUnbarMenu() {
        setMenuVisibility(true)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_item, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.map_button -> {
                        findNavController().navigate(R.id.action_HomeFragment_to_MapFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(location: WeatherLocation?) {

        with(binding) {
            location?.let {
                address.text = viewModel.getAddresses(location.lat, location.lng)
                subscription.text = location.summary
                logo.loadImage(location.icon.costumeUri())
                temperature.text = location.temperature.toCelsius()
            } ?: kotlin.run {
                binding.loadProgressBar.visibility = View.VISIBLE
                subscription.text = defaultSummery
            }
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}