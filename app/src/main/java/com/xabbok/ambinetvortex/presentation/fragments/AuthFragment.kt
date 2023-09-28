package com.xabbok.ambinetvortex.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.databinding.FragmentAuthBinding
import com.xabbok.ambinetvortex.error.ApiAppError
import com.xabbok.ambinetvortex.error.NetworkAppError
import com.xabbok.ambinetvortex.presentation.AuthScreenState
import com.xabbok.ambinetvortex.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {
    private val binding: FragmentAuthBinding by viewBinding(FragmentAuthBinding::bind)
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //добавляем верхнее меню с кнопкой назад
        /*(activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            setupActionBarWithNavControllerDefault()
        }*/

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = authViewModel

        setupListeners()

        lifecycleScope.launch {
            authViewModel.uiState.collect {
                when (it) {
                    is AuthScreenState.AuthScreenNormal -> {
                        showNormal()
                    }

                    is AuthScreenState.AuthScreenRequesting -> {
                        showRequesting()
                    }

                    is AuthScreenState.AuthScreenError -> {
                        showError(it)
                    }

                    is AuthScreenState.AuthScreenMustNavigateUp -> {
                        showNavigateUp()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.submitButton.setOnClickListener {
            authViewModel.login()
        }
    }

    private fun showNavigateUp() {
        findNavController().navigateUp()
    }

    private fun showNormal() {
        binding.submitButton.isEnabled = true
    }

    private fun showRequesting() {
        binding.submitButton.isEnabled = false
    }

    private fun showError(it: AuthScreenState.AuthScreenError) {
        val error =
            when (it.error) {
                is ApiAppError -> {
                    getString(R.string.check_login_password_message)
                }

                is NetworkAppError -> {
                    getString(R.string.check_internet_connection_text)
                }

                else -> {
                    getString(R.string.unknown_error_text)
                }
            }

        Snackbar.make(
            requireContext(),
            binding.snackBarCoordinator,
            error,
            Snackbar.LENGTH_SHORT
        )
            .setAction(getString(R.string.ok_button_text)) {}
            .show()

        binding.submitButton.isEnabled = true
    }
}

