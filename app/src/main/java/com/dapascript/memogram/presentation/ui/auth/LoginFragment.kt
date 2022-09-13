package com.dapascript.memogram.presentation.ui.auth

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.data.source.remote.model.LoginResult
import com.dapascript.memogram.databinding.FragmentLoginBinding
import com.dapascript.memogram.presentation.ui.MainActivity
import com.dapascript.memogram.utils.Resource
import com.dapascript.memogram.utils.getSnackBar
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
                val imm = requireActivity().getSystemService(
                    INPUT_METHOD_SERVICE
                ) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                showControl(false)
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        authViewModel.loginUser(email, password).observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> showControl(true)
                is Resource.Success -> {
                    showControl(false)
                    saveUserData(it.data!!.loginResult, email)
                    Intent(requireContext(), MainActivity::class.java).also { intent ->
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                is Resource.Error -> {
                    showControl(false)
                    showSnackBar(it.message!!)
                }
            }
        }
    }

    private fun showSnackBar(message: String) {
        if (message == "HTTP 401 Unauthorized") {
            getSnackBar(
                requireActivity(),
                binding.root,
                "Email atau kata sandi salah",
                binding.btnLogin
            )
        } else {
            getSnackBar(
                requireActivity(),
                binding.root,
                "Terjadi kesalahan",
                binding.btnLogin
            )
        }
    }

    private fun saveUserData(loginResult: LoginResult, email: String) {
        val token = loginResult.token
        val userName = loginResult.name

        viewLifecycleOwner.lifecycleScope.launch {
            userPreference.apply {
                saveUserName(userName)
                saveUserEmail(email)
                saveUserToken(token)
                saveIsLoggedIn(true)
            }
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