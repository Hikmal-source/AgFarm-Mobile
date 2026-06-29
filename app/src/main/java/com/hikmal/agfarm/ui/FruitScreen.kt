package com.hikmal.agfarm.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.hikmal.agfarm.viewmodel.FruitViewModel
import com.hikmal.agfarm.viewmodel.LandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FruitScreen(
    viewModel: FruitViewModel,
    landViewModel: LandViewModel,
    navController: NavController
) {
    val fruits by viewModel.fruits.collectAsState()
    val lands by landViewModel.lands.collectAsState()
    val context = LocalContext.current

    // 1. Dapatkan UID dari Firebase Auth
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // 2. FILTER BERLAPIS: Saring daftar lahan dan buah milik user aktif saja
    val myLands = remember(lands, currentUid) {
        lands.filter { it.userId == currentUid }
    }
    val myFruits = remember(fruits, currentUid) {
        fruits.filter { it.userId == currentUid }
    }

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    var selectedLandLocation by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Segarkan data saat screen dimuat kembali
    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            viewModel.fetchFruits()
            landViewModel.fetchLands()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Tanaman", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) },
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
                        Text("Tambah Varian Komoditas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))

                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Nama Tanaman / Buah") }, placeholder = { Text("Contoh: Melon Hidroponik") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = type, onValueChange = { type = it },
                            label = { Text("Kategori / Spesies") }, placeholder = { Text("Contoh: Hibrida Premium") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                        )

                        // MENGGUNAKAN 'myLands' yang sudah disaring kepemilikannya
                        if (myLands.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(16.dp))
                                    .clickable { navController.navigate("land_screen") }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "⚠️ Belum ada lahan aktif terdaftar. Klik di sini untuk membuat area lahan terlebih dahulu sebelum menanam.",
                                    color = Color(0xFFB45309),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = expandedDropdown,
                                onExpandedChange = { expandedDropdown = !expandedDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedLandLocation.ifEmpty { "Pilih Lokasi Lahan Menanam" },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Alokasi Lahan Penanaman") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = if (selectedLandLocation.isEmpty()) Color.Gray else Color.Black
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDropdown,
                                    onDismissRequest = { expandedDropdown = false }
                                ) {
                                    // Dropdown hanya merujuk pada lahan milik akun bersangkutan
                                    myLands.forEach { land ->
                                        DropdownMenuItem(
                                            text = { Text(text = "${land.location} (${land.sizeInHectares} Ha)") },
                                            onClick = {
                                                selectedLandLocation = land.location
                                                expandedDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (name.isNotBlank() && type.isNotBlank() && selectedLandLocation.isNotBlank()) {
                                    viewModel.addNewFruit(name, type) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Bibit $name teralokasi di $selectedLandLocation!", Toast.LENGTH_SHORT).show()
                                            name = ""; type = ""; selectedLandLocation = ""
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Mohon lengkapi semua data termasuk lokasi lahan", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = myLands.isNotEmpty(), // Menggunakan 'myLands'
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Daftarkan Bibit Tanaman", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Daftar Varian Terdata", fontWeight = FontWeight.Bold, color = Color(0xFF1F2937), fontSize = 14.sp)
            }

            // PENTING: Loop menggunakan 'myFruits' hasil filter personal, bukan 'fruits' global!
            items(myFruits) { fruit ->
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌱", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(fruit.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
                            Text("Tipe: ${fruit.type}", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                    IconButton(onClick = { viewModel.deleteExistingFruit(fruit.id) {} }) {
                        Text("🗑️", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}