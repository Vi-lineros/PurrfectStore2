package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.api.UserService
import com.mycat.purrfectstore2.databinding.FragmentUsersListBinding
import com.mycat.purrfectstore2.model.User
import com.mycat.purrfectstore2.ui.HomeActivity
import com.mycat.purrfectstore2.ui.adapter.UserAdapter
import kotlinx.coroutines.launch

class UsersListFragment : Fragment() {

    private var _binding: FragmentUsersListBinding? = null
    private val binding get() = _binding!!
    private lateinit var userService: UserService
    private lateinit var userAdapter: UserAdapter
    private lateinit var tokenManager: TokenManager
    private var allUsers: List<User> = emptyList()
    private var adminId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersListBinding.inflate(inflater, container, false)
        userService = RetrofitClient.createUserService(requireContext())
        tokenManager = TokenManager(requireContext())
        adminId = tokenManager.getUserId()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        loadUsers()

        binding.fabAddUser.setOnClickListener {
            if (userAdapter.isSelectionMode) {
                showDeleteConfirmationDialog()
            } else {
                findNavController().navigate(R.id.action_usersListFragment_to_addUsersFragment)
            }
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onUserClicked = { user ->
                if (userAdapter.isSelectionMode) {
                    if (user.id == adminId) {
                        Toast.makeText(requireContext(), "No puedes seleccionarte a ti mismo", Toast.LENGTH_SHORT).show()
                    } else {
                        userAdapter.toggleSelection(user.id)
                    }
                } else {
                    val action = UsersListFragmentDirections.actionUsersListFragmentToEditUsersFragment(user.id)
                    findNavController().navigate(action)
                }
            },
            onUserLongClicked = { user ->
                if (!userAdapter.isSelectionMode) {
                     if (user.id == adminId) {
                        Toast.makeText(requireContext(), "No puedes seleccionarte a ti mismo", Toast.LENGTH_SHORT).show()
                    } else {
                        enterSelectionMode(user.id)
                    }
                }
            },
            onSelectionChanged = { count ->
                if (userAdapter.isSelectionMode) {
                    if (count == 0) {
                        exitSelectionMode()
                    } else {
                        updateToolbarTitle(count)
                    }
                }
            },
            onStatusClicked = { user ->
                 if (user.id == adminId) {
                    Toast.makeText(requireContext(), "No puedes cambiar tu propio estado", Toast.LENGTH_SHORT).show()
                } else {
                    showBanUnbanDialog(user)
                }
            }
        )

        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun showBanUnbanDialog(user: User) {
        val isBanned = user.status?.lowercase() == "banned"
        val title = if (isBanned) "¿Desbanear usuario?" else "¿Banear usuario?"
        val message = "¿Confirmas que quieres cambiar el estado de ${user.username}?"
        val actionButton = "Aceptar"
        val newStatus = if (isBanned) "normal" else "banned"
        val successMessage = if (isBanned) "Usuario desbaneado" else "Usuario baneado"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton(actionButton) { _, _ ->
                updateUserStatus(user, newStatus, successMessage)
            }
            .show()
    }

    private fun updateUserStatus(user: User, newStatus: String, successMessage: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                setLoadingState(true) // Lock UI
                val fullUserData = mutableMapOf<String, @JvmSuppressWildcards Any>()
                fullUserData["name"] = user.username
                fullUserData["email"] = user.email
                fullUserData["role"] = user.role
                user.firstName?.let { fullUserData["first_name"] = it }
                user.lastName?.let { fullUserData["last_name"] = it }
                user.shippingAddress?.let { fullUserData["shipping_address"] = it }
                user.phoneNumber?.let { fullUserData["phone"] = it }
                fullUserData["status"] = newStatus
                
                userService.updateUser(user.id, fullUserData)
                Toast.makeText(requireContext(), "$successMessage con éxito", Toast.LENGTH_SHORT).show()
                loadUsers()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al actualizar estado: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoadingState(false) // Unlock UI
            }
        }
    }

    private fun enterSelectionMode(userId: Int) {
        userAdapter.isSelectionMode = true
        userAdapter.toggleSelection(userId)
        binding.fabAddUser.setImageResource(R.drawable.ic_delete)
        updateToolbarTitle(1)
        (activity as? HomeActivity)?.showCancelButton(true)
    }

    fun exitSelectionMode() {
        userAdapter.clearSelection()
        binding.fabAddUser.setImageResource(R.drawable.ic_add)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.title_users)
        (activity as? HomeActivity)?.showCancelButton(false)
    }

    private fun updateToolbarTitle(count: Int) {
        val userText = if (count == 1) "usuario" else "usuarios"
        (activity as? AppCompatActivity)?.supportActionBar?.title = "$count $userText seleccionado(s)"
    }

    private fun showDeleteConfirmationDialog() {
        val selectedCount = userAdapter.selectedItems.size
        val dialogUserWord = if (selectedCount == 1) "usuario" else "usuarios"
        val message = "¿Confirmar eliminación de $selectedCount $dialogUserWord?"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage(message)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Aceptar") { _, _ ->
                deleteSelectedUsers()
            }
            .show()
    }

    private fun deleteSelectedUsers() {
        val itemsToDelete = userAdapter.selectedItems.toList()
        val userWord = if (itemsToDelete.size == 1) "Usuario" else "Usuarios"
        val deleteWord = if (itemsToDelete.size == 1) "eliminado" else "eliminados"

        viewLifecycleOwner.lifecycleScope.launch {
            setLoadingState(true) // Lock UI
            try {
                itemsToDelete.forEach { userId ->
                    userService.deleteUser(userId)
                }
                Toast.makeText(requireContext(), "$userWord $deleteWord con éxito!", Toast.LENGTH_SHORT).show()
                exitSelectionMode()
                loadUsers()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoadingState(false) // Unlock UI
            }
        }
    }

    private fun setupSearch() {
        binding.searchViewUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query)
                binding.searchViewUsers.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun filter(query: String?) {
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        val filteredList = if (normalizedQuery.isBlank()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.email.lowercase().contains(normalizedQuery) ||
                user.username.lowercase().contains(normalizedQuery)
            }
        }
        userAdapter.updateData(filteredList)
    }

    private fun loadUsers() {
        setLoadingState(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val usersFromApi = userService.getUsers()
                allUsers = usersFromApi.filter { user -> 
                    !user.role.equals("supremo", ignoreCase = true) 
                }
                userAdapter.updateData(allUsers)
            } catch (e: Exception) {
                Toast.makeText(context, "Error cargando usuarios: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        (activity as? HomeActivity)?.setDrawerLocked(isLoading)
        binding.recyclerViewUsers.isVisible = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? HomeActivity)?.showCancelButton(false)
        _binding = null
    }
}
