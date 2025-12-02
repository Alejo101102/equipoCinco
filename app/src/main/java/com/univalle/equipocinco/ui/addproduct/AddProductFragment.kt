package com.univalle.equipocinco.ui.addproduct

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.univalle.equipocinco.R
import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.databinding.FragmentAddProductBinding
import com.univalle.equipocinco.ui.home.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupInputFilters()
        setupTextWatchers()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.action_addProductFragment_to_homeFragment)
        }
    }

    private fun setupInputFilters() {
        binding.etProductCode.filters = arrayOf(
            InputFilter.LengthFilter(4),
            InputFilter { source, start, end, _, _, _ ->
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }
        )

        binding.etProductName.filters = arrayOf(InputFilter.LengthFilter(40))

        binding.etPrice.filters = arrayOf(
            InputFilter.LengthFilter(20)
        )

        binding.etQuantity.filters = arrayOf(
            InputFilter { source, start, end, _, _, _ ->
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }
        )
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilProductCode.error = null
                binding.tilProductName.error = null
                binding.tilPrice.error = null
                binding.tilQuantity.error = null
            }
        }

        binding.etProductCode.addTextChangedListener(watcher)
        binding.etProductName.addTextChangedListener(watcher)
        binding.etPrice.addTextChangedListener(watcher)
        binding.etQuantity.addTextChangedListener(watcher)
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (validateFields()) {
                saveProduct()
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        val code = binding.etProductCode.text.toString().trim()
        if (code.isEmpty()) {
            binding.tilProductCode.error = "El código es requerido"
            isValid = false
        } else if (code.length > 4) {
            binding.tilProductCode.error = "Máximo 4 dígitos"
            isValid = false
        }

        val name = binding.etProductName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilProductName.error = "El nombre es requerido"
            isValid = false
        } else if (name.length > 40) {
            binding.tilProductName.error = "Máximo 40 caracteres"
            isValid = false
        }

        val price = binding.etPrice.text.toString().trim()
        if (price.isEmpty()) {
            binding.tilPrice.error = "El precio es requerido"
            isValid = false
        } else if (price.length > 20) {
            binding.tilPrice.error = "Máximo 20 dígitos"
            isValid = false
        } else {
            try {
                val priceValue = price.toDouble()
                if (priceValue <= 0) {
                    binding.tilPrice.error = "El precio debe ser mayor a 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.tilPrice.error = "Precio inválido"
                isValid = false
            }
        }

        val quantity = binding.etQuantity.text.toString().trim()
        if (quantity.isEmpty()) {
            binding.tilQuantity.error = "La cantidad es requerida"
            isValid = false
        } else {
            try {
                val quantityValue = quantity.toInt()
                if (quantityValue <= 0) {
                    binding.tilQuantity.error = "La cantidad debe ser mayor a 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.tilQuantity.error = "Cantidad inválida"
                isValid = false
            }
        }

        return isValid
    }

    private fun saveProduct() {
        binding.btnSave.isEnabled = false

        try {
            val code = binding.etProductCode.text.toString().trim().toInt()
            val name = binding.etProductName.text.toString().trim()
            val price = binding.etPrice.text.toString().trim().toDouble()
            val quantity = binding.etQuantity.text.toString().trim().toInt()

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Check if product with this code already exists
                    val existingProduct = viewModel.productsFlow.firstOrNull()
                        ?.find { it.code == code }

                    if (existingProduct != null) {
                        binding.tilProductCode.error = "Este código ya existe"
                        binding.btnSave.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "El producto con código $code ya existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val newProduct = ProductDto(
                            id = "", // Firestore will generate
                            code = code,
                            name = name,
                            price = price,
                            quantity = quantity
                        )
                        viewModel.addProduct(newProduct)

                        Toast.makeText(
                            requireContext(),
                            "Producto guardado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        findNavController().navigate(R.id.action_addProductFragment_to_homeFragment)
                    }

                } catch (e: Exception) {
                    if (_binding != null) {
                        binding.btnSave.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "Error al guardar el producto: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        } catch (e: NumberFormatException) {
            binding.btnSave.isEnabled = true
            Toast.makeText(
                requireContext(),
                "Error: Verifica los valores numéricos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}