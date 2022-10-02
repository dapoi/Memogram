package com.dapascript.memogram.presentation.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dapascript.memogram.R
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.data.source.remote.model.ListStoryItem
import com.dapascript.memogram.databinding.FragmentMapBinding
import com.dapascript.memogram.presentation.ui.MainActivity
import com.dapascript.memogram.utils.Resource
import com.dapascript.memogram.utils.getAddressSnippet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var gMap: GoogleMap
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var userPreference: UserPreference
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private var feedLat = 0.0
    private var feedLng = 0.0

    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())
        userPreference = UserPreference(requireContext())
        handler = Handler(Looper.getMainLooper())

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val mapFragment = childFragmentManager.findFragmentById(
            R.id.fragment_map
        ) as SupportMapFragment
        mapFragment.getMapAsync(this@MapFragment)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        userPreference.userToken.observe(viewLifecycleOwner) { token ->
            locationViewModel.getLocation(token).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        setUpMap(result.data!!.listStory)
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setUpMap(listStory: List<ListStoryItem>) {
        val latLngBounds = LatLngBounds.Builder()
        getLocation()
        listStory.indices.forEach { loc ->
            listStory[loc].apply {
                lat?.let { feedLat = it }
                lon?.let { feedLng = it }
            }
            try {
                val latLng = LatLng(feedLat, feedLng)
                val address = getAddressSnippet(requireContext(), feedLat, feedLng)
                gMap.apply {
                    addMarker(
                        MarkerOptions().position(latLng).title(listStory[loc].name).snippet(address)
                    )
                    latLngBounds.include(latLng)
                    uiSettings.apply {
                        isZoomControlsEnabled = true
                        isCompassEnabled = true
                        isMapToolbarEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val bounds = latLngBounds.build()
        gMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
        )
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                gMap.isMyLocationEnabled = true
            } else {
                Toast.makeText(requireContext(), "GPS Tidak Aktif", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocation()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(Runnable {
            getLocation()
            handler.postDelayed(runnable, 2000)
        }.also { runnable = it }, 2000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigationView()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity).showBottomNavigationView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}