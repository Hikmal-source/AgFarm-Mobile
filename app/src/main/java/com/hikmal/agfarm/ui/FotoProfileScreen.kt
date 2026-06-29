package com.hikmal.agfarm.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PotoProfileScreen(navController: NavController, metodeSaja: String) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val tempUri = remember {
        val file = File(context.cacheDir, "avatar_${currentUid ?: "temp"}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) imageUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) imageUri = tempUri }

    // Otomatis memicu launcher sesuai pilihan dari Security Check saat halaman terbuka
    LaunchedEffect(metodeSaja) {
        if (metodeSaja == "Kamera") {
            cameraLauncher.launch(tempUri)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pratinjau Foto", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Text("⬅️", color = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(180.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Text("Belum ada foto terpilih", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Simpan Terisolasi Menggunakan UID Login
            Button(
                onClick = {
                    if (imageUri != null && currentUid != null) {
                        // Menyimpan string URI foto hanya ke dalam ID pengguna yang sedang aktif!
                        FirebaseDatabase.getInstance().getReference("users")
                            .child(currentUid)
                            .child("photoUrl")
                            .setValue(imageUri.toString())
                            .addOnSuccessListener {
                                Toast.makeText(context, "Foto Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = (imageUri != null)
            ) {
                Text("Simpan & Sinkronkan ✓", fontWeight = FontWeight.Bold)
            }
        }
    }
}