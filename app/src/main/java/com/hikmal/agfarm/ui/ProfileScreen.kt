package com.hikmal.agfarm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid
    val currentUserEmail = auth.currentUser?.email ?: "user@agfarm.com"

    var fullName by remember { mutableStateOf("Memuat nama...") }
    var userAlamat by remember { mutableStateOf("Memuat alamat...") }
    var userRole by remember { mutableStateOf("Memuat role...") }
    // Tambahan state untuk menyimpan URL foto dari Firebase
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }

    // State untuk mengontrol tampilan Dialog Security Check
    var showSecurityDialog by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf("Kamera") }

    LaunchedEffect(currentUid) {
        if (currentUid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(currentUid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        fullName = snapshot.child("name").value?.toString() ?: "Tanpa Nama"
                        userAlamat = snapshot.child("alamat").value?.toString() ?: "Tidak Ada Alamat"
                        userRole = snapshot.child("role").value?.toString() ?: "Petani"
                        // Mengambil path gambar spesifik milik UID user login
                        userPhotoUrl = snapshot.child("photoUrl").value?.toString()
                    } else {
                        fullName = currentUserEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                        userAlamat = "-"
                        userRole = "Petani"
                        userPhotoUrl = null
                    }
                }
                .addOnFailureListener {
                    fullName = "Gagal memuat"
                    userAlamat = "Gagal memuat"
                    userRole = "Petani"
                    userPhotoUrl = null
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Pengguna", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Text("⬅️", color = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Container Visual Foto Profil Dinamis (Terkoneksi Coil & Database)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userPhotoUrl.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(userPhotoUrl),
                                contentDescription = "Foto Profil Pengguna",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("🧑‍🌾", fontSize = 48.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text(text = currentUserEmail, fontSize = 14.sp, color = Color(0xFF9CA3AF))

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .background(
                                    if (userRole == "Admin") Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Hak Akses: $userRole",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userRole == "Admin") Color(0xFFEF4444) else Color(0xFF22C55E)
                            )
                        }
                    }

                    Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "Domisili / Wilayah", fontSize = 12.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                        Text(text = userAlamat, fontSize = 14.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { showSecurityDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Unggah Foto Profil", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Keluar dari Akun (Logout)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (userRole == "Admin") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("admin_screen") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Masuk Panel Admin Panel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // ================= FITUR SECURITY CHECK (PSI DIALOG) =================
    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔐 Security Check", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Keamanan Profil Anda Penting. Pilih metode untuk melanjutkan unggah foto:",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )

                    // Pilihan Opsi 1: Gunakan Kamera
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedMethod == "Kamera") Color(0xFFDCFCE7) else Color(0xFFF3F4F6))
                            .clickable { selectedMethod = "Kamera" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (selectedMethod == "Kamera"), onClick = { selectedMethod = "Kamera" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Gunakan Kamera", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Ambil foto baru langsung", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }

                    // Pilihan Opsi 2: Pilih dari Galeri
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedMethod == "Galeri") Color(0xFFEFF6FF) else Color(0xFFF3F4F6))
                            .clickable { selectedMethod = "Galeri" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (selectedMethod == "Galeri"), onClick = { selectedMethod = "Galeri" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Pilih dari Galeri", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Unggah dari penyimpanan perangkat", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSecurityDialog = false
                        // UPDATE: Navigasi sekarang sukses melempar argumen ke rute baru dinamis
                        navController.navigate("poto_profile_screen/$selectedMethod")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lanjutkan dengan PSI ✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Batal", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}