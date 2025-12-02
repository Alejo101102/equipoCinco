package com.univalle.equipocinco.ui.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.univalle.equipocinco.R
import com.univalle.equipocinco.databinding.FragmentProductDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadProduct(args.productId)

        setupToolbar()
        setupActions()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.action_productDetailFragment_to_homeFragment)
        }
    }

    private fun setupActions() {
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("¿Deseas eliminar este producto?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí") { _, _ ->
                    viewModel.deleteCurrentProduct(
                        onDeleted = {
                            findNavController().navigate(R.id.action_productDetailFragment_to_homeFragment)
                        },
                        onError = { /* No-op: could show a Toast */ }
                    )
                }
                .show()
        }

        binding.fabEdit.setOnClickListener {
            val action = ProductDetailFragmentDirections
                .actionProductDetailFragmentToEditProductFragment(args.productId)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.product.collect { product ->
                        product ?: return@collect
                        binding.tvName.text = product.name
                        binding.tvPriceValue.text = formatCurrency(product.price)
                        binding.tvQuantityValue.text = "${product.quantity}"
                        binding.tvTotalValue.text = formatCurrency(product.getTotal())
                    }
                }
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale("es", "CO")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("$ #,##0.00", symbols)
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}