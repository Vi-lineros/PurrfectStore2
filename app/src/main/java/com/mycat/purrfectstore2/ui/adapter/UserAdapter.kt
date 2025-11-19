package com.mycat.purrfectstore2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.ItemUserBinding
import com.mycat.purrfectstore2.model.User

class UserAdapter(
    private val onUserClicked: (User) -> Unit,
    private val onUserLongClicked: (User) -> Unit,
    private val onSelectionChanged: (Int) -> Unit,
    private val onStatusClicked: (User) -> Unit // New communication channel for status clicks
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users: List<User> = emptyList()
    val selectedItems = mutableSetOf<Int>()
    var isSelectionMode = false

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
        onSelectionChanged(selectedItems.size)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
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
                .placeholder(R.drawable.ic_menu_profile)
                .into(binding.userImage)

            // Update status text and appearance
            when (user.status?.lowercase()) {
                "active", "normal" -> {
                    binding.userStatus.text = "Activo"
                    binding.userStatus.setBackgroundResource(R.drawable.bg_status_active)
                }
                "banned" -> {
                    binding.userStatus.text = "Ban"
                    binding.userStatus.setBackgroundResource(R.drawable.bg_status_banned)
                }
                else -> {
                    binding.userStatus.text = user.status ?: "Desconocido"
                    binding.userStatus.setBackgroundColor(Color.GRAY)
                }
            }

            // Set the new listener on the status view
            binding.userStatus.setOnClickListener {
                onStatusClicked(user)
            }

            // Visual feedback for selection
            val isSelected = selectedItems.contains(user.id)
            if (isSelected) {
                binding.root.setCardBackgroundColor(Color.argb(40, 255, 0, 0))
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
