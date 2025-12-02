package com.univalle.equipocinco.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.databinding.ItemProductBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ProductAdapter(
    private val onItemClick: (ProductDto) -> Unit
) : ListAdapter<ProductDto, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onItemClick: (ProductDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductDto) {
            binding.apply {
                tvProductName.text = product.name
                tvProductId.text = "Id: ${product.code}"
                tvProductTotal.text = formatCurrency(product.price)

                root.setOnClickListener {
                    onItemClick(product)
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
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<ProductDto>() {
        override fun areItemsTheSame(oldItem: ProductDto, newItem: ProductDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductDto, newItem: ProductDto): Boolean {
            return oldItem == newItem
        }
    }
}