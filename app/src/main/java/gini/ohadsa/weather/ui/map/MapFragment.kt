package gini.ohadsa.weather.ui.map

import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.weather.R
import gini.ohadsa.weather.data.models.WeatherLocation
import gini.ohadsa.weather.databinding.FragmentMapBinding
import gini.ohadsa.weather.network.NetworkStatusChecker
import gini.ohadsa.weather.utils.SharedPreferenceUtil.costumeUri
import gini.ohadsa.weather.utils.SharedPreferenceUtil.loadImage
import gini.ohadsa.weather.utils.SharedPreferenceUtil.toCelsius
import gini.ohadsa.weather.utils.defaultLocation
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private lateinit var mMap: GoogleMap

    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels()

    @Inject
    lateinit var networkStatusChecker: NetworkStatusChecker


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMapBinding.inflate(inflater, container, false)

        if (!networkStatusChecker.hasInternetConnection()) showDialog()
        else {
            collectWeatherLocationFlow()
            initMap()
        }


        return binding.root

    }

    private fun showDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.no_internet_label_title))
            .setMessage(getString(R.string.dialog_no_internet))
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                findNavController().navigate(MapFragmentDirections.actionMapFragmentToHomeFragment())
            }.show()
    }


    private fun collectWeatherLocationFlow() {

        lifecycleScope.launchWhenResumed {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                binding.card.loadProgressBar.visibility = View.VISIBLE
                viewModel.weatherLocationFlow()
                    .collectLatest {
                        binding.card.loadProgressBar.visibility = View.VISIBLE
                        updateCard(it)
                        binding.card.loadProgressBar.visibility = View.GONE
                    }
            }
        }
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        defaultLocation.first,
                        defaultLocation.second
                    )
                )
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        defaultLocation.first,
                        defaultLocation.second
                    )
                )
            )
            viewModel.locationClickedFlow.value = defaultLocation

            googleMap.setOnMapClickListener { latLng -> // Creating a marker
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                googleMap.clear()
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng)
                    .zoom(5f).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                googleMap.addMarker(markerOptions)
                viewModel.locationClickedFlow.value = latLng.latitude to latLng.longitude
            }

        }
    }


    private fun updateCard(location: WeatherLocation) {
        with(binding.card) {
            address.text = viewModel.getAddresses(location.lat, location.lng)
            subscription.text = "${location.temperature.toCelsius()}, ${location.summary}"
            logo.loadImage(location.icon.costumeUri())
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}