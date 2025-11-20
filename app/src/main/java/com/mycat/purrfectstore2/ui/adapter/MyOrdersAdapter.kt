package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.OrderItemLayoutBinding
import com.mycat.purrfectstore2.model.Cart
import com.mycat.purrfectstore2.ui.fragments.MyOrdersFragmentDirections
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyOrdersAdapter(private var orders: List<Cart>) : RecyclerView.Adapter<MyOrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = OrderItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Cart>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(private val binding: OrderItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val orderId = orders[position].id
                    val action = MyOrdersFragmentDirections.actionMyOrdersFragmentToOrderDetailsFragment(orderId)
                    itemView.findNavController().navigate(action)
                }
            }
        }

        fun bind(order: Cart) {
            val totalValue = order.total ?: 0.0
            binding.textViewOrderTotalValue.text = currencyFormat.format(totalValue)

            val totalQuantity = order.product_id?.sumOf { it.quantity } ?: 0
            binding.textViewOrderQuantityValue.text = totalQuantity.toString()

            val date = Date(order.created_at)
            binding.textViewOrderDateValue.text = dateFormat.format(date)

            binding.textViewOrderStatus.text = order.status.replaceFirstChar { it.uppercase() }

            val statusBackground = when (order.status.lowercase()) {
                "aprobado" -> R.drawable.status_approved_background
                "rechazado" -> R.drawable.status_rejected_background
                "pendiente" -> R.drawable.status_pending_background
                else -> R.drawable.bg_status_active // A default background
            }
            binding.textViewOrderStatus.setBackgroundResource(statusBackground)
        }
    }
}
