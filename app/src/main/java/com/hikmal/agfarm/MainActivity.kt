package com.hikmal.agfarm

import android.os.Bundle
import android.view.WindowManager // 1. IMPORT BARU UNTUK WINDOW MANAGER
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.hikmal.agfarm.navigation.NavGraph
import com.hikmal.agfarm.ui.theme.Hikmal_AgFarmTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. TAMBAHKAN FLAG SECURE DI SINI (Anti-Screenshot & Anti-Screen Record)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            Hikmal_AgFarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}