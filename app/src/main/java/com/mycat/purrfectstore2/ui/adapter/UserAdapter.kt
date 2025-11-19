package com.mycat.purrfectstore2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.ItemUserBinding
import com.mycat.purrfectstore2.model.User

class UserAdapter(
    private val onUserClicked: (User) -> Unit,
    private val onUserLongClicked: (User) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users: List<User> = emptyList()
    var isSelectionMode = false
        set(value) {
            field = value
            if (!value) {
                selectedItems.clear()
            }
            notifyDataSetChanged()
            onSelectionChanged(selectedItems.size)
        }
    val selectedItems = mutableSetOf<Int>()

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun toggleSelection(userId: Int) {
        if (selectedItems.contains(userId)) {
            selectedItems.remove(userId)
        } else {
            selectedItems.add(userId)
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedItems.size)
    }

    fun clearSelection() {
        isSelectionMode = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.userName.text = user.username
            binding.userEmail.text = user.email

            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_menu_profile) // Placeholder image
                .into(binding.userImage)

            // Handle user status safely
            when (user.status?.lowercase()) {
                "active" -> {
                    binding.userStatus.text = "Activo"
                    binding.userStatus.setBackgroundResource(R.drawable.bg_status_active)
                }
                "banned" -> {
                    binding.userStatus.text = "Baneado"
                    binding.userStatus.setBackgroundResource(R.drawable.bg_status_banned)
                }
                else -> {
                    binding.userStatus.text = user.status ?: "Desconocido"
                    binding.userStatus.setBackgroundColor(Color.GRAY) // Default case
                }
            }

            // Handle selection UI
            val isSelected = selectedItems.contains(user.id)
            binding.root.isActivated = isSelected
            if (isSelected) {
                binding.root.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.selected_item_color))
            } else {
                binding.root.setCardBackgroundColor(Color.WHITE)
            }

            itemView.setOnClickListener {
                onUserClicked(user)
            }

            itemView.setOnLongClickListener {
                onUserLongClicked(user)
                true
            }
        }
    }
}
