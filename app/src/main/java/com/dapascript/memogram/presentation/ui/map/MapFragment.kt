package com.dapascript.memogram.presentation.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var gMap: GoogleMap
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var userPreference: UserPreference

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

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val mapFragment = childFragmentManager.findFragmentById(
            R.id.fragment_map
        ) as SupportMapFragment
        mapFragment.getMapAsync(this@MapFragment)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        userPreference.userToken.observe(viewLifecycleOwner) { token ->
            locationViewModel.getLocation(token).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        setUpMap(googleMap, result.data!!.listStory)
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setUpMap(googleMap: GoogleMap, listStory: List<ListStoryItem>) {
        gMap = googleMap

        getDeviceLoc()
        for (loc in listStory.indices) {
            feedLat = listStory[loc].lat!!
            feedLng = listStory[loc].lon!!
            val latLng = LatLng(feedLat, feedLng)
            gMap.apply {
                addMarker(MarkerOptions().position(latLng).title(listStory[loc].name))
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isCompassEnabled = true
                    isMapToolbarEnabled = true
                }
            }
        }
    }

    private fun getDeviceLoc() {
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            gMap.isMyLocationEnabled = true
            fusedLocation.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 2f))
                } else {
                    Toast.makeText(
                        requireContext().applicationContext,
                        "Location not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getDeviceLoc()
            }
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