package com.dapascript.memogram.presentation.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dapascript.memogram.R
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.databinding.FragmentAccountBinding
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())

        val name = userPreference.userName
        val email = userPreference.userEmail

        binding.apply {
            name.observe(viewLifecycleOwner) {
                tvName.text = it
            }
            email.observe(viewLifecycleOwner) {
                tvEmail.text = it
            }
            btnLogout.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    userPreference.clear()
                }
                requireActivity().finish()
                findNavController().navigate(R.id.action_nav_account_to_authActivity)
            }
        }
    }
}