package com.dapascript.memogram.presentation.ui.auth

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dapascript.memogram.R
import com.dapascript.memogram.databinding.FragmentRegisterBinding
import com.dapascript.memogram.utils.Resource
import com.dapascript.memogram.utils.getSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

            btnSignup.setOnClickListener {
                val imm = requireActivity().getSystemService(
                    INPUT_METHOD_SERVICE
                ) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)

                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                showControl(false)

                registerUser(name, email, password)
            }
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        authViewModel.registerUser(name, email, password).observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> showControl(true)
                is Resource.Success -> {
                    showControl(false)
                    AlertDialog.Builder(requireContext())
                        .setTitle("Berhasil")
                        .setMessage("Pendaftaran berhasil, silahkan login")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                        .show()
                }
                is Resource.Error -> {
                    showControl(false)
                    showSnackBar(it.message!!)
                }
            }
        }
    }

    private fun showSnackBar(message: String) {
        if (message == "HTTP 400 Bad Request") {
            getSnackBar(
                requireActivity(),
                binding.root,
                resources.getString(R.string.email_already_in_use),
                binding.btnSignup
            )
        } else {
            getSnackBar(
                requireActivity(),
                binding.root,
                resources.getString(R.string.error),
                binding.btnSignup
            )
        }
    }

    private fun showControl(state: Boolean) {
        binding.apply {
            if (state) {
                progressBar.visibility = View.VISIBLE
                btnSignupText.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                btnSignupText.visibility = View.VISIBLE
            }
        }
    }
}