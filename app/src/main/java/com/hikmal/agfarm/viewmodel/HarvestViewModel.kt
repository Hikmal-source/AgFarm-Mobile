package com.hikmal.agfarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hikmal.agfarm.data.model.Harvest
import com.hikmal.agfarm.data.repository.HarvestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HarvestViewModel : ViewModel() {

    private val repository = HarvestRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _harvestHistory = MutableStateFlow<List<Harvest>>(emptyList())
    val harvestHistory: StateFlow<List<Harvest>> = _harvestHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchHarvestHistory()
    }

    // Mengambil riwayat panen khusus milik user yang sedang aktif
    fun fetchHarvestHistory() {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Catatan: Pastikan di repository/Firebase query sudah menggunakan .orderByChild("userId").equalTo(currentUid)
                val result = repository.getHarvestHistory().filter { it.userId == currentUid }
                _harvestHistory.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat riwayat panen: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addNewHarvest(
        idLahan: String,
        namaBuah: String,
        beratKg: Double,
        onResult: (Boolean) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Menyisipkan currentUid ke properti model Harvest
            val newHarvest = Harvest(
                id = "",
                userId = currentUid, // Kunci pemisah kepemilikan data
                landId = idLahan,
                fruitName = namaBuah,
                weightInKg = beratKg,
                dateMillis = System.currentTimeMillis()
            )

            val isSuccess = repository.addHarvest(newHarvest)
            if (isSuccess) {
                fetchHarvestHistory()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menambahkan data panen baru"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun updateExistingHarvest(harvest: Harvest, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val isSuccess = repository.updateHarvest(harvest)
            if (isSuccess) {
                fetchHarvestHistory()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal memperbarui data panen"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun deleteExistingHarvest(harvestId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val isSuccess = repository.deleteHarvest(harvestId)
            if (isSuccess) {
                fetchHarvestHistory()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menghapus data panen"
                onResult(false)
            }
            _isLoading.value = false
        }
    }
}