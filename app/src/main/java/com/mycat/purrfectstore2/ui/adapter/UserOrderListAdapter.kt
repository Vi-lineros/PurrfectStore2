package com.mycat.purrfectstore2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.UserOrderListItemBinding
import com.mycat.purrfectstore2.model.Cart
import com.mycat.purrfectstore2.ui.fragments.UserOrderListDirections
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrderWithUser(val cart: Cart, val username: String)

class UserOrderListAdapter(private var ordersWithUsers: List<OrderWithUser>) : RecyclerView.Adapter<UserOrderListAdapter.OrderViewHolder>() {

    private var originalOrders: List<OrderWithUser> = ordersWithUsers

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = UserOrderListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(ordersWithUsers[position])
    }

    override fun getItemCount() = ordersWithUsers.size

    fun updateOrders(newOrders: List<OrderWithUser>) {
        this.ordersWithUsers = newOrders
        this.originalOrders = newOrders
        notifyDataSetChanged()
    }

    fun filter(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            originalOrders
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            originalOrders.filter {
                it.username.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            }
        }
        ordersWithUsers = filteredList
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(private val binding: UserOrderListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val orderId = ordersWithUsers[position].cart.id
                    val action = UserOrderListDirections.actionUserOrderListToUserOrderDetailsFragment(orderId)
                    itemView.findNavController().navigate(action)
                }
            }
        }

        fun bind(orderWithUser: OrderWithUser) {
            val cart = orderWithUser.cart
            val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())
            binding.textViewOrderUsername.text = orderWithUser.username
            binding.textViewOrderDate.text = dateFormat.format(Date(cart.created_at))
            binding.textViewOrderStatus.text = cart.status.replaceFirstChar { it.uppercase() }
            binding.textViewOrderTotal.text = String.format(Locale.US, "Total: $%.2f", cart.total)
            val productCount = cart.product_id?.size ?: 0
            binding.textViewOrderProductCount.text = "$productCount productos"

            val statusBackground = when (cart.status.lowercase(Locale.getDefault())) {
                "aprobado" -> R.drawable.status_approved_background
                "rechazado" -> R.drawable.status_rejected_background
                "pendiente" -> R.drawable.status_pending_background
                else -> R.drawable.bg_status_active
            }
            binding.textViewOrderStatus.setBackgroundResource(statusBackground)
        }
    }
}
