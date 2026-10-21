package com.dinhlekieulinh.diemdanh.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dinhlekieulinh.diemdanh.DinhLeKieuLinhApplication
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhRole
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhBiometricHelper
import com.dinhlekieulinh.diemdanh.ui.screens.admin.DinhLeKieuLinhAdminDashboard
import com.dinhlekieulinh.diemdanh.ui.screens.auth.*
import com.dinhlekieulinh.diemdanh.ui.screens.crud.*
import com.dinhlekieulinh.diemdanh.ui.screens.giangvien.DinhLeKieuLinhGiangVienDashboard
import com.dinhlekieulinh.diemdanh.ui.screens.sinhvien.DinhLeKieuLinhSinhVienDashboard
import com.dinhlekieulinh.diemdanh.ui.screens.sinhvien.DinhLeKieuLinhSinhVienViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DinhLeKieuLinhNavGraph(
    activity: FragmentActivity,
    navController: NavHostController = rememberNavController()
) {
    val app = DinhLeKieuLinhApplication.instance
    val coroutineScope = rememberCoroutineScope()

    // Khởi tạo các ViewModels
    val authViewModel = remember { DinhLeKieuLinhAuthViewModel(app.authRepository) }
    val userViewModel = remember { DinhLeKieuLinhUserViewModel(app.userRepository) }
    val monHocViewModel = remember { DinhLeKieuLinhMonHocViewModel(app.monHocRepository) }
    val lopHocViewModel = remember { DinhLeKieuLinhLopHocViewModel(app.lopHocRepository, app.monHocRepository) }
    val buoiHocViewModel = remember { DinhLeKieuLinhBuoiHocViewModel(app.buoiHocRepository, app.lopHocRepository) }
    val diemDanhViewModel = remember { DinhLeKieuLinhDiemDanhViewModel(app.diemDanhRepository, app.buoiHocRepository, app.userRepository) }
    val sinhVienViewModel = remember { DinhLeKieuLinhSinhVienViewModel(app.authRepository, app.buoiHocRepository, app.diemDanhRepository) }

    val currentUser by app.tokenManager.currentUserFlow.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = DinhLeKieuLinhDestinations.SPLASH
    ) {
        // 1. Màn hình Splash kiểm tra session và điều hướng tự động
        composable(DinhLeKieuLinhDestinations.SPLASH) {
            LaunchedEffect(Unit) {
                val token = app.tokenManager.getToken()
                val role = app.tokenManager.roleFlow.first()
                if (!token.isNullOrBlank() && role != null) {
                    when (role) {
                        DinhLeKieuLinhRole.ADMIN -> navController.navigate(DinhLeKieuLinhDestinations.ADMIN_ROOT) { popUpTo(DinhLeKieuLinhDestinations.SPLASH) { inclusive = true } }
                        DinhLeKieuLinhRole.GIANG_VIEN -> navController.navigate(DinhLeKieuLinhDestinations.GIANG_VIEN_ROOT) { popUpTo(DinhLeKieuLinhDestinations.SPLASH) { inclusive = true } }
                        DinhLeKieuLinhRole.SINH_VIEN -> navController.navigate(DinhLeKieuLinhDestinations.SINH_VIEN_ROOT) { popUpTo(DinhLeKieuLinhDestinations.SPLASH) { inclusive = true } }
                    }
                } else {
                    navController.navigate(DinhLeKieuLinhDestinations.LOGIN) {
                        popUpTo(DinhLeKieuLinhDestinations.SPLASH) { inclusive = true }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // 2. Màn hình Đăng nhập
        composable(DinhLeKieuLinhDestinations.LOGIN) {
            DinhLeKieuLinhLoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(DinhLeKieuLinhDestinations.REGISTER)
                },
                onNavigateToOtp = { email ->
                    navController.navigate(DinhLeKieuLinhDestinations.createOtpRoute(email))
                },
                onBiometricClick = {
                    DinhLeKieuLinhBiometricHelper.showBiometricPrompt(
                        activity = activity,
                        onSuccess = {
                            coroutineScope.launch {
                                val token = app.tokenManager.getBiometricToken() ?: "MOCK_BIO_TOKEN"
                                authViewModel.biometricLogin("sv.linh@dinhlekieulinh.edu.vn", token) { user ->
                                    when (user.role) {
                                        DinhLeKieuLinhRole.ADMIN -> navController.navigate(DinhLeKieuLinhDestinations.ADMIN_ROOT) { popUpTo(DinhLeKieuLinhDestinations.LOGIN) { inclusive = true } }
                                        DinhLeKieuLinhRole.GIANG_VIEN -> navController.navigate(DinhLeKieuLinhDestinations.GIANG_VIEN_ROOT) { popUpTo(DinhLeKieuLinhDestinations.LOGIN) { inclusive = true } }
                                        DinhLeKieuLinhRole.SINH_VIEN -> navController.navigate(DinhLeKieuLinhDestinations.SINH_VIEN_ROOT) { popUpTo(DinhLeKieuLinhDestinations.LOGIN) { inclusive = true } }
                                    }
                                }
                            }
                        },
                        onError = {}
                    )
                }
            )
        }

        // 3. Màn hình Đăng ký
        composable(DinhLeKieuLinhDestinations.REGISTER) {
            DinhLeKieuLinhRegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(DinhLeKieuLinhDestinations.LOGIN) {
                        popUpTo(DinhLeKieuLinhDestinations.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // 4. Màn hình Xác thực OTP
        composable(
            route = DinhLeKieuLinhDestinations.OTP,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            DinhLeKieuLinhOtpScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOtpSuccess = { user ->
                    when (user.role) {
                        DinhLeKieuLinhRole.ADMIN -> navController.navigate(DinhLeKieuLinhDestinations.ADMIN_ROOT) {
                            popUpTo(DinhLeKieuLinhDestinations.LOGIN) { inclusive = true }
                        }
                        DinhLeKieuLinhRole.GIANG_VIEN -> navController.navigate(DinhLeKieuLinhDestinations.GIANG_VIEN_ROOT) {
                            popUpTo(DinhLeKieuLinhDestinations.LOGIN) { inclusive = true }
                        }
                        DinhLeKieuLinhRole.SINH_VIEN -> navController.navigate(DinhLeKieuLinhDestinations.SINH_VIEN_ROOT) {
                            popUpTo(DinhLeKieuLinhDestinations.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 5. Dashboard Quản trị viên (Admin)
        composable(DinhLeKieuLinhDestinations.ADMIN_ROOT) {
            DinhLeKieuLinhAdminDashboard(
                userViewModel = userViewModel,
                monHocViewModel = monHocViewModel,
                lopHocViewModel = lopHocViewModel,
                buoiHocViewModel = buoiHocViewModel,
                diemDanhViewModel = diemDanhViewModel,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(DinhLeKieuLinhDestinations.LOGIN) {
                            popUpTo(DinhLeKieuLinhDestinations.ADMIN_ROOT) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 6. Dashboard Giảng viên
        composable(DinhLeKieuLinhDestinations.GIANG_VIEN_ROOT) {
            DinhLeKieuLinhGiangVienDashboard(
                lopHocViewModel = lopHocViewModel,
                buoiHocViewModel = buoiHocViewModel,
                diemDanhViewModel = diemDanhViewModel,
                currentUser = currentUser,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(DinhLeKieuLinhDestinations.LOGIN) {
                            popUpTo(DinhLeKieuLinhDestinations.GIANG_VIEN_ROOT) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 7. Dashboard Sinh viên
        composable(DinhLeKieuLinhDestinations.SINH_VIEN_ROOT) {
            DinhLeKieuLinhSinhVienDashboard(
                viewModel = sinhVienViewModel,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(DinhLeKieuLinhDestinations.LOGIN) {
                            popUpTo(DinhLeKieuLinhDestinations.SINH_VIEN_ROOT) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
