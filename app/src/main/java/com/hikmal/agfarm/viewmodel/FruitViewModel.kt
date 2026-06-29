package com.hikmal.agfarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.hikmal.agfarm.data.model.Fruit
import com.hikmal.agfarm.data.repository.FruitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FruitViewModel : ViewModel() {

    private val repository = FruitRepository()
    private val auth = FirebaseAuth.getInstance() // Instance Firebase Auth untuk membaca siapa yang login

    private val _fruits = MutableStateFlow<List<Fruit>>(emptyList())
    val fruits: StateFlow<List<Fruit>> = _fruits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Otomatis memuat data buah/tanaman milik user aktif saat pertama kali dibuka
        fetchFruits()
    }

    // 1. MENGAMBIL DATA BUAH KHUSUS MILIK USER YANG SEDANG LOGIN
    fun fetchFruits() {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Mengambil semua data buah dari repository, lalu disaring berdasarkan userId
                val result = repository.getAllFruits().filter { it.userId == currentUid }
                _fruits.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data buah: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. MENAMBAH DATA BUAH BARU DENGAN MENGUNCI USERID PEMBUATNYA
    fun addNewFruit(namaBuah: String, jenisBuah: String, onResult: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Menambahkan properti userId ke objek Fruit yang akan disimpan
            val newFruit = Fruit(
                id = "",
                userId = currentUid, // <-- Ini yang mengunci kepemilikan buah agar tidak tertukar
                name = namaBuah,
                type = jenisBuah
            )

            val isSuccess = repository.addFruit(newFruit)
            if (isSuccess) {
                fetchFruits() // Refresh list agar UI langsung terupdate otomatis
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menambahkan buah baru"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    // 3. FUNGSI UNTUK MENGUBAH / UPDATE DATA BUAH YANG SUDAH ADA
    fun updateExistingFruit(fruit: Fruit, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val isSuccess = repository.updateFruit(fruit)
            if (isSuccess) {
                fetchFruits()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal memperbarui data buah"
                onResult(false)
            }
            _isLoading.value = false
        }
    }

    // 4. FUNGSI UNTUK MENGHAPUS / DELETE DATA BUAH DARI FIREBASE
    fun deleteExistingFruit(fruitId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val isSuccess = repository.deleteFruit(fruitId)
            if (isSuccess) {
                fetchFruits()
                onResult(true)
            } else {
                _errorMessage.value = "Gagal menghapus data buah"
                onResult(false)
            }
            _isLoading.value = false
        }
    }
}