package com.dapascript.memogram.presentation.ui.story

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dapascript.memogram.R
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.databinding.FragmentUploadStoryBinding
import com.dapascript.memogram.utils.*
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class UploadStoryFragment : Fragment() {

    private lateinit var binding: FragmentUploadStoryBinding
    private lateinit var userPreference: UserPreference
    private lateinit var result: Bitmap
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private var getFile: File? = null
    private var myLat = 0.0
    private var myLong = 0.0
    private val uploadStoryViewModel: UploadStoryViewModel by viewModels()

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile
            result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.absolutePath),
                isBackCamera
            )

            binding.apply {
                btnUpload.isEnabled = true
                tvEmpty.visibility = View.GONE
                ivPhoto.visibility = View.VISIBLE
                ivPhoto.setImageBitmap(result)
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val uri = it.data?.data as Uri
            val file = uriToFile(uri, requireContext())
            getFile = file

            binding.apply {
                btnUpload.isEnabled = true
                tvEmpty.visibility = View.GONE
                ivPhoto.visibility = View.VISIBLE
                ivPhoto.setImageURI(uri)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
        handler = Handler(Looper.getMainLooper())

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        userPreference = UserPreference(requireContext())

        binding.apply {
            btnCamera.setOnClickListener {
                openCameraX()
            }

            btnGallery.setOnClickListener {
                openGallery()
            }

            btnUpload.setOnClickListener {
                etDesc.clearFocus()
                val imm =
                    requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                val desc = etDesc.text.toString()
                if (desc.isNotEmpty()) {
                    uploadState(true)
                    uploadStory(desc)
                } else {
                    showSnackbar()
                }
            }
        }
    }

    private fun showSnackbar() {
        val snackBar = Snackbar.make(
            binding.root,
            "Deskripsi tidak boleh kosong.",
            Snackbar.LENGTH_LONG
        ).apply {
            val bottom = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            anchorView = bottom
            setBackgroundTint(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
        }
        val layoutParams = snackBar.view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(60, 0, 60, 200)
        snackBar.view.layoutParams = layoutParams
        snackBar.show()
    }

    private fun uploadStory(desc: String) {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)
            val reqBodyImage = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val reqBodyDesc = desc.toRequestBody("text/plain".toMediaType())
            val reqBodyLat = myLat.toString().toRequestBody("text/plain".toMediaType())
            val reqBodyLong = myLong.toString().toRequestBody("text/plain".toMediaType())
            val multipartImage = MultipartBody.Part.createFormData("photo", file.name, reqBodyImage)

            val token = userPreference.userToken
            token.observe(viewLifecycleOwner) {
                uploadStoryViewModel.postStory(
                    it,
                    multipartImage,
                    reqBodyDesc,
                    reqBodyLat,
                    reqBodyLong
                )
                    .observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Resource.Loading -> uploadState(true)
                            is Resource.Success -> {
                                binding.etDesc.text.clear()
                                binding.ivPhoto.visibility = View.GONE
                                binding.tvEmpty.visibility = View.VISIBLE
                                uploadState(false)
                                Toast.makeText(
                                    requireContext(),
                                    "Berhasil upload story",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().navigate(R.id.nav_feed)
                            }
                            is Resource.Error -> {
                                uploadState(false)
                                Toast.makeText(
                                    requireContext(),
                                    "Gagal upload story",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("UploadStoryFragment", result.message.toString())
                            }
                        }
                    }
            }
        }
    }

    private fun uploadState(state: Boolean) {
        binding.apply {
            if (state) {
                progressBar.visibility = View.VISIBLE
                btnUpload.isEnabled = false
            } else {
                progressBar.visibility = View.GONE
                btnUpload.isEnabled = true
            }
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        val chooser = Intent.createChooser(intent, "Pilih Gambar")
        launcherIntentGallery.launch(chooser)
    }

    private fun openCameraX() {
        val intent = Intent(requireActivity(), CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                fusedLocation.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location = task.result
                    if (location != null) {
                        myLong = location.longitude
                        myLat = location.latitude
                        getAddressName(requireActivity(), binding.tvTurnOnLocation, myLat, myLong)
                    } else {
                        requestNewLocationData()
                    }
                }
            } else {
                binding.tvTurnOnLocation.text = resources.getString(R.string.turn_on_location)
                myLong = 0.0
                myLat = 0.0
            }
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_LOCATION
        )
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 100
            fastestInterval = 3000
            numUpdates = 1
        }
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocation.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation!!
            myLong = location.longitude
            myLat = location.latitude
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onResume() {
        super.onResume()

        handler.postDelayed(Runnable {
            getLocation()
            handler.postDelayed(runnable, 1000)
        }.also { runnable = it }, 1000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        const val PERMISSION_LOCATION = 100

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}