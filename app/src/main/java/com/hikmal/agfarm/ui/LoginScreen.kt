package com.hikmal.agfarm.ui

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.hikmal.agfarm.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val sharedPreferences = remember {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "agfarm_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    LaunchedEffect(Unit) {
        val savedEmail = sharedPreferences.getString("saved_email", "") ?: ""
        val savedPassword = sharedPreferences.getString("saved_password", "") ?: ""
        if (savedEmail.isNotEmpty() && savedPassword.isNotEmpty()) {
            email = savedEmail
            password = savedPassword
            rememberMe = true
        }
    }

    fun showBiometricPrompt() {
        val savedEmail = sharedPreferences.getString("saved_email", "") ?: ""
        val savedPassword = sharedPreferences.getString("saved_password", "") ?: ""

        if (savedEmail.isEmpty() || savedPassword.isEmpty()) {
            Toast.makeText(context, "Silakan login manual dulu dengan centang 'Remember Me'", Toast.LENGTH_LONG).show()
            return
        }

        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(context, "Sidik Jari Cocok! Menghubungkan...", Toast.LENGTH_SHORT).show()
                    viewModel.loginUser(savedEmail, savedPassword, onLoginSuccess)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Biometrik Gagal: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login via Sidik Jari")
            .setSubtitle("Autentikasi akun AgFarm Anda")
            .setNegativeButtonText("Batal")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 30.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(text = "AgFarm", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E)) // UPDATE: Brand Text Hijau
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Welcome to\nAgFarm login now!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
            color = Color(0xFF1F2937)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text("Email", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        SmartLoginTextField(value = email, onValueChange = { email = it }, placeholder = "joedoe75@gmail.com")

        Spacer(modifier = Modifier.height(20.dp))

        Text("Password", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SmartLoginTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••••",
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            Button(
                onClick = { showBiometricPrompt() },
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("🧬", fontSize = 22.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF22C55E)) // UPDATE: Kotak Centang Hijau
                )
                Text("Remember me", fontSize = 13.sp, color = Color(0xFF4B5563))
            }
            TextButton(onClick = { /* Opsional */ }) {
                Text("Forgot password?", fontSize = 13.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.SemiBold) // UPDATE: Teks Link Hijau
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF22C55E)) // UPDATE: Loading Hijau
        } else {
            Button(
                onClick = {
                    if (rememberMe) {
                        sharedPreferences.edit()
                            .putString("saved_email", email)
                            .putString("saved_password", password)
                            .apply()
                    } else {
                        sharedPreferences.edit().clear().apply()
                    }
                    viewModel.loginUser(email, password, onLoginSuccess)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)) // UPDATE: Tombol Utama Hijau
            ) {
                Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Sign up", color = Color(0xFF6B7280), fontSize = 14.sp)
        }
    }
}

@Composable
fun SmartLoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF9CA3AF), fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF3F4F6),
            unfocusedContainerColor = Color(0xFFF3F4F6),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFF1F2937),
            unfocusedTextColor = Color(0xFF1F2937)
        ),
        visualTransformation = visualTransformation,
        singleLine = true
    )
}