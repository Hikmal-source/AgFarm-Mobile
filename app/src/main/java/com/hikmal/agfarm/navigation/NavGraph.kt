package com.hikmal.agfarm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hikmal.agfarm.ui.OnboardingScreen
import com.hikmal.agfarm.ui.LoginScreen
import com.hikmal.agfarm.ui.RegisterScreen
import com.hikmal.agfarm.ui.HomeScreen
import com.hikmal.agfarm.ui.LandScreen
import com.hikmal.agfarm.ui.FruitScreen
import com.hikmal.agfarm.ui.SaleScreen
import com.hikmal.agfarm.ui.HarvestScreen
import com.hikmal.agfarm.ui.AdminScreen
import com.hikmal.agfarm.ui.ProfileScreen
import com.hikmal.agfarm.ui.PotoProfileScreen
import com.hikmal.agfarm.viewmodel.LoginViewModel
import com.hikmal.agfarm.viewmodel.RegisterViewModel
import com.hikmal.agfarm.viewmodel.LandViewModel
import com.hikmal.agfarm.viewmodel.FruitViewModel
import com.hikmal.agfarm.viewmodel.HarvestViewModel
import com.hikmal.agfarm.viewmodel.SaleViewModel

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // 1. Inisialisasi ViewModel Autentikasi
    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()

    // 2. Inisialisasi ViewModel Manajemen Data Pertanian (Pusat Sinkronisasi Real-time)
    val landViewModel: LandViewModel = viewModel()
    val fruitViewModel: FruitViewModel = viewModel()
    val harvestViewModel: HarvestViewModel = viewModel()
    val saleViewModel: SaleViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "onboarding",
        modifier = modifier
    ) {
        // --- AUTHENTICATION FLOW ---

        // Onboarding Screen
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // Halaman Login
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        // Halaman Register
        composable("register") {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // --- MAIN APP FLOW (DASHBOARD & REAKSI DATA MURNI FIREBASE) ---

        // Halaman Utama / Dashboard (Menerima parameter data sinkron)
        composable("dashboard") {
            val currentUserEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
            val displayName = currentUserEmail?.substringBefore("@") ?: "Johan Orindo"

            HomeScreen(
                currentUserName = displayName,
                navController = navController,
                landViewModel = landViewModel,
                fruitViewModel = fruitViewModel,
                harvestViewModel = harvestViewModel,
                saleViewModel = saleViewModel
            )
        }

        // Halaman Kelola Sektor Lahan
        composable("land_screen") {
            LandScreen(
                viewModel = landViewModel,
                navController = navController
            )
        }

        // Halaman Kelola Varian Buah / Tanaman
        composable("fruit_screen") {
            FruitScreen(
                viewModel = fruitViewModel,
                landViewModel = landViewModel,
                navController = navController
            )
        }

        // Halaman Kelola Transaksi Penjualan / Omset Keuangan
        composable("sale_screen") {
            SaleScreen(
                viewModel = saleViewModel,
                fruitViewModel = fruitViewModel,
                navController = navController
            )
        }

        // Halaman Kelola Log Produksi Hasil Panen
        composable("harvest_screen") {
            HarvestScreen(
                viewModel = harvestViewModel,
                landViewModel = landViewModel,
                navController = navController
            )
        }

        // Halaman Profil Pengguna Terintegrasi
        composable("profile_screen") {
            ProfileScreen(
                navController = navController
            )
        }

        // Halaman Admin Panel
        composable("admin_screen") {
            AdminScreen(
                navController = navController
            )
        }

        // === UPDATE: Menerima Parameter Metode dari Security Dialog (Kamera / Galeri) ===
        composable(
            route = "poto_profile_screen/{metodeSaja}",
            arguments = listOf(navArgument("metodeSaja") { type = NavType.StringType })
        ) { backStackEntry ->
            val metode = backStackEntry.arguments?.getString("metodeSaja") ?: "Kamera"
            PotoProfileScreen(
                navController = navController,
                metodeSaja = metode
            )
        }
    }
}