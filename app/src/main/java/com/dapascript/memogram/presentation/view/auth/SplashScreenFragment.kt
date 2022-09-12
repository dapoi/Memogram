package com.dapascript.memogram.presentation.view.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.dapascript.memogram.R
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.databinding.FragmentSplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false)

        userPreference = UserPreference(requireContext())
        val isLoggedIn = userPreference.isLoggedIn.asLiveData()

        Handler(Looper.getMainLooper()).postDelayed({
            isLoggedIn.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigate(R.id.action_splashScreenFragment_to_mainActivity)
                    requireActivity().finish()
                } else {
                    findNavController().navigate(R.id.action_splashScreenFragment_to_onBoardingFragment)
                }
            }
        }, 3500)

        return binding.root
    }
}