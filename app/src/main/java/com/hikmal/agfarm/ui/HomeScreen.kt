package com.hikmal.agfarm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.hikmal.agfarm.viewmodel.FruitViewModel
import com.hikmal.agfarm.viewmodel.HarvestViewModel
import com.hikmal.agfarm.viewmodel.LandViewModel
import com.hikmal.agfarm.viewmodel.SaleViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUserName: String = "Johan Orindo",
    navController: NavController,
    landViewModel: LandViewModel,
    fruitViewModel: FruitViewModel,
    harvestViewModel: HarvestViewModel,
    saleViewModel: SaleViewModel
) {
    val scrollState = rememberScrollState()
    val auth = FirebaseAuth.getInstance()

    // 1. Ambil UID pengguna aktif saat ini
    val currentUid = remember { auth.currentUser?.uid ?: "" }

    val landsState by landViewModel.lands.collectAsState()
    val fruitsState by fruitViewModel.fruits.collectAsState()
    val harvestState by harvestViewModel.harvestHistory.collectAsState()
    val salesState by saleViewModel.saleHistory.collectAsState()

    // 2. FILTER BERLAPIS: Saring semua data agar hanya memproses data milik UID aktif
    val myLands = remember(landsState, currentUid) { landsState.filter { it.userId == currentUid } }
    val myFruits = remember(fruitsState, currentUid) { fruitsState.filter { it.userId == currentUid } }
    val myHarvests = remember(harvestState, currentUid) { harvestState.filter { it.userId == currentUid } }
    val mySales = remember(salesState, currentUid) { salesState.filter { it.userId == currentUid } }

    // 3. KALKULASI RINGKASAN: Sekarang menghitung dari variabel terfilter ('my...')
    val totalOmset = remember(mySales) { mySales.sumOf { it.totalPrice } }
    val totalTanaman = remember(myFruits) { myFruits.size }
    val totalLahan = remember(myLands) { myLands.size }
    val logPanenTerbaru = remember(myHarvests) { myHarvests.firstOrNull() }

    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf("Petani") }

    // Memastikan data segar kembali setiap kali masuk ke Dashboard Utama
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            landViewModel.fetchLands()
            fruitViewModel.fetchFruits()
            harvestViewModel.fetchHarvestHistory()
            saleViewModel.fetchSaleHistory()

            FirebaseDatabase.getInstance().getReference("users").child(currentUid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        userPhotoUrl = snapshot.child("photoUrl").value?.toString()
                        userRole = snapshot.child("role").value?.toString() ?: "Petani"
                    }
                }
        }
    }

    val formatRupiah = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { navController.navigate("profile_screen") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
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
                            Text("🧑‍🌾", fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Hi, $currentUserName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text(text = "Welcome to AgFarm", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔔", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TOMBOL ADMIN PANEL KHUSUS ROLE ADMIN
            if (userRole == "Admin") {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("admin_screen") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔐", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    "Panel Manajemen Admin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Lihat data & informasi seluruh petani",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "Ringkasan Pertanian\nHari Ini",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Manajemen",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .background(Color(0xFF22C55E), RoundedCornerShape(24.dp))
                        .clickable { navController.navigate("fruit_screen") }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🌱", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$totalTanaman Tanaman", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .clickable { navController.navigate("land_screen") }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🗺️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$totalLahan Lahan Aktif", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEFF6FF))
                    .clickable { navController.navigate("land_screen") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("➕", color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Kelola / Tambah Lahan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E3A8A))
                        Text("Daftarkan area kapling pertanian baru", fontSize = 12.sp, color = Color(0xFF3B82F6))
                    }
                }
                Text("➔", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Statistik Penjualan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1E293B))
                    .clickable { navController.navigate("sale_screen") }
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Total Omset Penjualan", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupiah.format(totalOmset),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .background(Color(0xFF22C55E).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📈", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sinkronisasi Real-time", color = Color(0xFF4ADE80), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Target Panen Terdekat",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .clickable { navController.navigate("harvest_screen") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌾", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    if (logPanenTerbaru != null) {
                        Text("Panen Komoditas ${logPanenTerbaru.fruitName}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text("Volume Produksi: ${logPanenTerbaru.weightInKg} Kg di ${logPanenTerbaru.landId}", fontSize = 13.sp, color = Color(0xFF6B7280))
                    } else {
                        Text("Belum Ada Log Aktivitas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text("Ketuk untuk mencatat panen pertama", fontSize = 13.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }

        // BOTTOM NAVIGATION BAR
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏠", fontSize = 18.sp)
                }

                IconButton(onClick = { navController.navigate("fruit_screen") }) { Text("🌱", fontSize = 20.sp) }
                IconButton(onClick = { navController.navigate("sale_screen") }) { Text("📊", fontSize = 20.sp) }
                IconButton(onClick = { navController.navigate("harvest_screen") }) { Text("🌾", fontSize = 20.sp) }

                IconButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                ) {
                    Text("🚪", fontSize = 20.sp)
                }
            }
        }
    }
}