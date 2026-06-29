package com.hikmal.agfarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hikmal.agfarm.data.model.Land
import com.hikmal.agfarm.data.repository.LandRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LandViewModel : ViewModel() {
    private val repository = LandRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _lands = MutableStateFlow<List<Land>>(emptyList())
    val lands: StateFlow<List<Land>> = _lands.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchLands()
    }

    fun fetchLands() {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getAllLands().filter { it.userId == currentUid }
                _lands.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data lahan: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addNewLand(lokasiLahan: String, luasLahan: Double, namaBuah: String, onResult: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val newLand = Land(
                id = "",
                userId = currentUid, // Lahan dikunci khusus untuk user ini
                location = lokasiLahan,
                sizeInHectares = luasLahan,
                fruitName = namaBuah
            )

            val isSuccess = repository.addLand(newLand)
            if (isSuccess) {
                fetchLands()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menambahkan lahan baru"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun updateExistingLand(land: Land, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val isSuccess = repository.updateLand(land) // Disesuaikan nama method repomu
            if (isSuccess) {
                fetchLands()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal memperbarui data lahan"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun deleteExistingLand(landId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val isSuccess = repository.deleteLand(landId)
            if (isSuccess) {
                fetchLands()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menghapus lahan"
                onResult(false)
            }
            _isLoading.value = false
        }
    }
}