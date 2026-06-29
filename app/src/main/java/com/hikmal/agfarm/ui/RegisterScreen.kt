package com.hikmal.agfarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hikmal.agfarm.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val hasUpper by viewModel.hasUpperCase.collectAsState()
    val hasLower by viewModel.hasLowerCase.collectAsState()
    val hasNum by viewModel.hasNumber.collectAsState()
    val hasSym by viewModel.hasSymbol.collectAsState()
    val isValid by viewModel.isPasswordValid.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 30.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.align(Alignment.Start)
        ) {
            // UPDATE: Mengubah teks Back menjadi Hijau
            Text("← Back", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AgFarm",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22C55E) // UPDATE: Brand Text Hijau
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create an Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Name", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            SmartTextField(value = name, onValueChange = { name = it }, placeholder = "Johan Orindo")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Alamat Tinggal / Lahan", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            SmartTextField(value = alamat, onValueChange = { alamat = it }, placeholder = "Jl. Agrikultur No. 12")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Email", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            SmartTextField(value = email, onValueChange = { email = it }, placeholder = "joedoe75@gmail.com")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Password", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            SmartTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.onPasswordChanged(it)
                },
                placeholder = "••••••••",
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "Syarat Keamanan Password:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B5563),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IndicatorItem(label = "Huruf Besar", checked = hasUpper)
                    IndicatorItem(label = "Huruf Kecil", checked = hasLower)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IndicatorItem(label = "Angka", checked = hasNum)
                    IndicatorItem(label = "Simbol unik", checked = hasSym)
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (isLoading) {
                // UPDATE: Loading Indicator Hijau
                CircularProgressIndicator(color = Color(0xFF22C55E))
            } else {
                Button(
                    onClick = { viewModel.registerUser(name, alamat, email, password, onRegisterSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E),       // UPDATE: Tombol aktif Hijau
                        disabledContainerColor = Color(0xFF86EFAC) // UPDATE: Tombol mati Hijau Muda/Lembut
                    )
                ) {
                    Text("Create account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Sign in", color = Color(0xFF6B7280), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SmartTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF9CA3AF), fontSize = 14.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF3F4F6),
            unfocusedContainerColor = Color(0xFFF3F4F6),
            disabledContainerColor = Color(0xFFF3F4F6),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFF1F2937),
            unfocusedTextColor = Color(0xFF1F2937)
        ),
        visualTransformation = visualTransformation,
        singleLine = true
    )
}

@Composable
fun IndicatorItem(label: String, checked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = if (checked) "✓" else "✕",
            color = if (checked) Color(0xFF10B981) else Color(0xFFEF4444),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (checked) Color(0xFF111827) else Color(0xFF9CA3AF),
            fontSize = 12.sp
        )
    }
}