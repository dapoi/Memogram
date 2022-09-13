package com.dapascript.memogram.presentation.ui.story

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import com.dapascript.memogram.utils.Resource
import com.dapascript.memogram.utils.reduceFileImage
import com.dapascript.memogram.utils.rotateBitmap
import com.dapascript.memogram.utils.uriToFile
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

    private var getFile: File? = null
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

            binding.btnUpload.isEnabled = true
            binding.ivCameraPlaceholder.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val uri = it.data?.data as Uri
            val file = uriToFile(uri, requireContext())
            getFile = file

            binding.btnUpload.isEnabled = true
            binding.ivCameraPlaceholder.setImageURI(uri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    requireContext(),
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
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
            val multipartImage = MultipartBody.Part.createFormData("photo", file.name, reqBodyImage)

            val token = userPreference.userToken
            token.observe(viewLifecycleOwner) {
                uploadStoryViewModel.postStory(it, multipartImage, reqBodyDesc)
                    .observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Resource.Loading -> uploadState(true)
                            is Resource.Success -> {
                                binding.etDesc.text.clear()
                                binding.ivCameraPlaceholder.setImageResource(
                                    R.drawable.ic_image
                                )
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

    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}