package com.hikmal.agfarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // State untuk Indikator Keamanan Password
    private val _hasUpperCase = MutableStateFlow(false)
    val hasUpperCase: StateFlow<Boolean> = _hasUpperCase

    private val _hasLowerCase = MutableStateFlow(false)
    val hasLowerCase: StateFlow<Boolean> = _hasLowerCase

    private val _hasNumber = MutableStateFlow(false)
    val hasNumber: StateFlow<Boolean> = _hasNumber

    private val _hasSymbol = MutableStateFlow(false)
    val hasSymbol: StateFlow<Boolean> = _hasSymbol

    private val _isPasswordValid = MutableStateFlow(false)
    val isPasswordValid: StateFlow<Boolean> = _isPasswordValid

    // Fungsi untuk memvalidasi password secara real-time saat user mengetik
    fun onPasswordChanged(password: String) {
        _hasUpperCase.value = password.any { it.isUpperCase() }
        _hasLowerCase.value = password.any { it.isLowerCase() }
        _hasNumber.value = password.any { it.isDigit() }
        _hasSymbol.value = password.any { !it.isLetterOrDigit() }

        // Syarat aman: Semua kondisi terpenuhi + minimal 8 karakter
        _isPasswordValid.value = _hasUpperCase.value &&
                _hasLowerCase.value &&
                _hasNumber.value &&
                _hasSymbol.value &&
                password.length >= 8
    }

    // Fungsi Registrasi Akun Baru
    fun registerUser(
        name: String,
        alamat: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || alamat.isBlank() || email.isBlank()) {
            _errorMessage.value = "Semua kolom wajib diisi!"
            return
        }

        if (!_isPasswordValid.value) {
            _errorMessage.value = "Password belum memenuhi syarat keamanan!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // 1. Daftarkan email & password ke Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid

                if (uid != null) {
                    // 2. Jika sukses Auth, simpan data tambahan termasuk "role" default "Petani"
                    val userProfile = mapOf(
                        "uid" to uid,
                        "name" to name,
                        "alamat" to alamat,
                        "email" to email,
                        "role" to "Petani" // <-- Default role otomatis menjadi Petani
                    )
                    db.child(uid).setValue(userProfile).await()
                    onSuccess()
                } else {
                    _errorMessage.value = "Gagal mendapatkan User ID"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Registrasi gagal"
            } finally {
                _isLoading.value = false
            }
        }
    }
}