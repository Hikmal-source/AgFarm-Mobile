package com.hikmal.agfarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hikmal.agfarm.data.model.Sale
import com.hikmal.agfarm.data.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SaleViewModel : ViewModel() {

    private val repository = SaleRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _saleHistory = MutableStateFlow<List<Sale>>(emptyList())
    val saleHistory: StateFlow<List<Sale>> = _saleHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchSaleHistory()
    }

    fun fetchSaleHistory() {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.getSaleHistory().filter { it.userId == currentUid }
                _saleHistory.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data penjualan: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addNewSale(
        namaBuah: String,
        beratTerjualKg: Double,
        hargaTotal: Long,
        namaPembeli: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val newSale = Sale(
                id = "",
                userId = currentUid, // Hanya bisa diakses oleh akun yang menjual
                fruitName = namaBuah,
                weightSoldInKg = beratTerjualKg,
                totalPrice = hargaTotal,
                buyerName = namaPembeli,
                dateMillis = System.currentTimeMillis()
            )

            val isSuccess = repository.addSale(newSale)
            if (isSuccess) {
                fetchSaleHistory()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menambahkan transaksi penjualan baru"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun updateExistingSale(sale: Sale, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val isSuccess = repository.updateSale(sale)
            if (isSuccess) {
                fetchSaleHistory()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal memperbarui data penjualan"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    fun deleteExistingSale(saleId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val isSuccess = repository.deleteSale(saleId)
            if (isSuccess) {
                fetchSaleHistory()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menghapus transaksi penjualan"
                onResult(false)
            }
            _isLoading.value = false
        }
    }
}