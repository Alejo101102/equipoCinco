package com.univalle.equipocinco.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.univalle.equipocinco.R
import com.univalle.equipocinco.databinding.FragmentLoginBinding
import com.univalle.equipocinco.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Verificar si ya está logueado
        if (sessionManager.isLoggedIn()) {
            navigateToHome()
            return
        }

        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateFields()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()

                // Validación en tiempo real de mínimo 6 dígitos
                if (password.isNotEmpty() && password.length < 6) {
                    binding.tvPasswordError.visibility = View.VISIBLE
                    binding.tilPassword.setBoxStrokeColorStateList(
                        ContextCompat.getColorStateList(requireContext(), R.color.orange)!!
                    )
                } else {
                    binding.tvPasswordError.visibility = View.GONE
                    binding.tilPassword.boxStrokeColor = resources.getColor(android.R.color.white, null)
                }

                validateFields()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateFields() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        val isValid = email.isNotEmpty() && password.length >= 6

        binding.btnLogin.isEnabled = isValid
        binding.tvRegister.isEnabled = isValid

        // Cambiar colores según estado
        if (isValid) {
            binding.btnLogin.alpha = 1f
            binding.tvRegister.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            binding.btnLogin.alpha = 0.5f
            binding.tvRegister.setTextColor(resources.getColor(R.color.gray, null))
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            performRegister()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.btnLogin.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            val result = viewModel.login(email, password)

            if (result.isSuccess) {
                sessionManager.setLoggedIn(true)
                Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                binding.btnLogin.isEnabled = true
                Toast.makeText(requireContext(), "Login incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performRegister() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tvRegister.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            val result = viewModel.register(email, password)

            if (result.isSuccess) {
                sessionManager.setLoggedIn(true)
                Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } else {
                binding.tvRegister.isEnabled = true
                Toast.makeText(requireContext(), "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}