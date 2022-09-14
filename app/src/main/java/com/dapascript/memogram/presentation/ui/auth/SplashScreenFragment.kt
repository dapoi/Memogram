package com.dapascript.memogram.presentation.ui.auth

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

class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding
    private lateinit var userPreference: UserPreference

    private val duration = 3500L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        }, duration)
    }
}