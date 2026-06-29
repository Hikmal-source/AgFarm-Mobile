package com.hikmal.agfarm.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.hikmal.agfarm.viewmodel.HarvestViewModel
import com.hikmal.agfarm.viewmodel.LandViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    viewModel: HarvestViewModel,
    landViewModel: LandViewModel,
    navController: NavController
) {
    val harvestHistory by viewModel.harvestHistory.collectAsState()
    val lands by landViewModel.lands.collectAsState()
    val context = LocalContext.current

    // 1. Ambil UID pengguna aktif
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // 2. FILTER DATA: Saring riwayat panen dan daftar lahan milik user aktif
    val myHarvestHistory = remember(harvestHistory, currentUid) {
        harvestHistory.filter { it.userId == currentUid }
    }
    val myLands = remember(lands, currentUid) {
        lands.filter { it.userId == currentUid }
    }

    var fruitNameInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    var selectedLandLocation by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val formatRibuan = remember {
        NumberFormat.getNumberInstance(Locale("in", "ID")).apply { maximumFractionDigits = 2 }
    }

    // Ambil ulang data ketika screen dibuka kembali
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            viewModel.fetchHarvestHistory()
            landViewModel.fetchLands()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pencatatan Panen", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Text("⬅️", color = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Input Data Hasil Bumi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))

                        // MENGGUNAKAN 'myLands' hasil filter
                        if (myLands.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(16.dp))
                                    .clickable { navController.navigate("land_screen") }
                                    .padding(16.dp)
                            ) {
                                Text("⚠️ Lahan belum ada! Daftarkan lahan dulu sebelum mencatat panen.", color = Color(0xFFB45309), fontSize = 13.sp)
                            }
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = expandedDropdown,
                                onExpandedChange = { expandedDropdown = !expandedDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedLandLocation.ifEmpty { "Pilih Asal Lahan Panen" },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Lokasi Sektor Lahan") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                                    // Dropdown mengambil dari myLands
                                    myLands.forEach { land ->
                                        DropdownMenuItem(
                                            text = { Text("${land.location} (${land.fruitName})") },
                                            onClick = {
                                                selectedLandLocation = land.location
                                                fruitNameInput = land.fruitName
                                                expandedDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = fruitNameInput, onValueChange = { fruitNameInput = it },
                            label = { Text("Komoditas Buah Terpanen") }, placeholder = { Text("Contoh: Padi Ketan") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = weightInput, onValueChange = { weightInput = it },
                            label = { Text("Total Bobot Bersih (Kg)") }, placeholder = { Text("Contoh: 1200") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                val beratDouble = weightInput.toDoubleOrNull()
                                if (selectedLandLocation.isNotBlank() && fruitNameInput.isNotBlank() && beratDouble != null) {
                                    viewModel.addNewHarvest(selectedLandLocation, fruitNameInput, beratDouble) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Log Produksi Panen Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                                            selectedLandLocation = ""; fruitNameInput = ""; weightInput = ""
                                        }
                                    }
                                }
                            },
                            enabled = myLands.isNotEmpty(), // Menggunakan myLands
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Simpan Log Panen", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Histori Produksi", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontSize = 14.sp)
            }

            if (myHarvestHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada riwayat catatan panen.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                // PAKAI 'myHarvestHistory' untuk rendering list terfilter
                items(myHarvestHistory) { harvest ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🌾", fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = harvest.fruitName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Sektor: ${harvest.landId} • ${dateFormatter.format(Date(harvest.dateMillis))}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val formattedWeight = formatRibuan.format(harvest.weightInKg)
                                Text(
                                    text = "Hasil: $formattedWeight Kg",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF22C55E),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteExistingHarvest(harvest.id) {} },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("🗑️", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}