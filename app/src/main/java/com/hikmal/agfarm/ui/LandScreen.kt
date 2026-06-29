package com.hikmal.agfarm.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.hikmal.agfarm.viewmodel.LandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandScreen(viewModel: LandViewModel, navController: NavController) {
    val lands by viewModel.lands.collectAsState()
    val context = LocalContext.current

    // 1. Ambil UID pengguna yang sedang aktif (Joni / Hikmal)
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // 2. FILTER DATA: Saring hanya lahan yang memiliki userId sama dengan user aktif
    val myLands = remember(lands, currentUid) {
        lands.filter { it.userId == currentUid }
    }

    var lokasi by remember { mutableStateOf("") }
    var luas by remember { mutableStateOf("") }
    var buah by remember { mutableStateOf("") }

    // Pemicu refresh data agar ditarik ulang saat masuk screen ini
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            viewModel.fetchLands()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Area Lahan", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) },
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
                .padding(24.dp)
        ) {
            // Card Input Modern ala Dribbble
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Registrasi Kapling Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))

                    OutlinedTextField(
                        value = lokasi, onValueChange = { lokasi = it },
                        label = { Text("Lokasi / Nama Blok") }, placeholder = { Text("Contoh: Blok C Sawah Utara") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = luas, onValueChange = { luas = it },
                        label = { Text("Luas Lahan (Hektar)") }, placeholder = { Text("Contoh: 1.5") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = buah, onValueChange = { buah = it },
                        label = { Text("Komoditas Buah Utama") }, placeholder = { Text("Contoh: Semangka Madu") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            val luasDouble = luas.toDoubleOrNull()
                            if (lokasi.isNotBlank() && luasDouble != null && buah.isNotBlank()) {
                                viewModel.addNewLand(lokasi, luasDouble, buah) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Lahan Berhasil Didaftarkan!", Toast.LENGTH_SHORT).show()
                                        lokasi = ""; luas = ""; buah = ""
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Semua data wajib diisi dengan benar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Simpan Sektor Lahan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Daftar Lahan Aktif", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Menggunakan Data yang Sudah Difilter
            if (myLands.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada lahan terdaftar untuk akun ini.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // PENTING: Pakai 'myLands', bukan 'lands' lagi!
                    items(myLands) { land ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🗺️", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(land.location, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
                                    Text("Fokus: ${land.fruitName} • ${land.sizeInHectares} Ha", fontSize = 12.sp, color = Color(0xFF6B7280))
                                }
                            }
                            IconButton(onClick = { viewModel.deleteExistingLand(land.id) {} }) {
                                Text("🗑️", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}