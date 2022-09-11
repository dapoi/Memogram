package com.dapascript.memogram.presentation.view.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.data.source.remote.model.LoginResult
import com.dapascript.memogram.databinding.FragmentLoginBinding
import com.dapascript.memogram.presentation.viewmodel.AuthViewModel
import com.dapascript.memogram.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var userPreference: UserPreference
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference(requireContext())

        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            btnLogin.setOnClickListener {
                showControl(false)
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                when {
                    email.isEmpty() -> {
                        tilEmail.error = "Email harus diisi"
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        tilEmail.error = "Email tidak valid"
                    }
                    password.isEmpty() || password.length < 6 -> {
                        tilPassword.error = "Kata sandi minimal 6 karakter"
                    }
                    else -> {
                        tilEmail.error = null
                        tilPassword.error = null
                        loginUser(email, password)
                    }
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        authViewModel.loginUser(email, password).observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> showControl(true)
                is Resource.Success -> {
                    showControl(false)
                    saveUserData(it.data?.loginResult, email)
                    AlertDialog.Builder(requireContext())
                        .setTitle("Berhasil")
                        .setMessage("Selamat datang kembali")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                is Resource.Error -> {
                    showControl(false)
                    AlertDialog.Builder(requireContext())
                        .setTitle("Gagal")
                        .setMessage(it.message)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    private fun saveUserData(loginResult: LoginResult?, email: String) {
        lifecycleScope.launch {
            userPreference.saveUserName(loginResult?.name!!)
            userPreference.saveUserEmail(email)
        }
    }

    private fun showControl(state: Boolean) {
        binding.apply {
            if (state) {
                progressBar.visibility = View.VISIBLE
                btnLoginText.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                btnLoginText.visibility = View.VISIBLE
            }
        }
    }
}