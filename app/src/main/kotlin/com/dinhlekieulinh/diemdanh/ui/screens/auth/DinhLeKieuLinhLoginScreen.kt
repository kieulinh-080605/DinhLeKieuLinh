package com.dinhlekieulinh.diemdanh.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhRole
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhButton
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhTextField

@Composable
fun DinhLeKieuLinhLoginScreen(
    viewModel: DinhLeKieuLinhAuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToOtp: (email: String) -> Unit,
    onBiometricClick: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Logo biểu tượng App Điểm danh
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DinhLeKieuLinh_DiemDanh",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "App Điểm Danh Cho Sinh Viên",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Thanh chọn nhanh tài khoản Demo cho người chấm / giảng viên
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Chọn nhanh tài khoản Demo:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { viewModel.fillDemoAccount(DinhLeKieuLinhRole.ADMIN) },
                                label = { Text("Admin") },
                                leadingIcon = {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, Modifier.size(16.dp))
                                }
                            )
                            AssistChip(
                                onClick = { viewModel.fillDemoAccount(DinhLeKieuLinhRole.GIANG_VIEN) },
                                label = { Text("Giảng viên") },
                                leadingIcon = {
                                    Icon(Icons.Default.School, contentDescription = null, Modifier.size(16.dp))
                                }
                            )
                            AssistChip(
                                onClick = { viewModel.fillDemoAccount(DinhLeKieuLinhRole.SINH_VIEN) },
                                label = { Text("Sinh viên") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form nhập thông tin
                DinhLeKieuLinhTextField(
                    value = state.usernameOrEmail,
                    onValueChange = viewModel::onLoginUsernameChanged,
                    label = "Tên đăng nhập hoặc Email",
                    placeholder = "nhapemail@edu.vn",
                    leadingIcon = Icons.Default.Email,
                    errorMessage = state.usernameError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                DinhLeKieuLinhTextField(
                    value = state.password,
                    onValueChange = viewModel::onLoginPasswordChanged,
                    label = "Mật khẩu",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Hiện/Ẩn mật khẩu"
                            )
                        }
                    },
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    errorMessage = state.passwordError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (!state.errorMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nút Đăng nhập (gọi API bước 1 -> sang màn hình OTP)
                DinhLeKieuLinhButton(
                    text = "Đăng nhập",
                    onClick = { viewModel.login(onNavigateToOtp) },
                    isLoading = state.isLoading,
                    icon = Icons.Default.Login
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nút đăng nhập vân tay (Nâng cao 3)
                OutlinedButton(
                    onClick = onBiometricClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Vân tay",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng nhập bằng vân tay",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Chưa có tài khoản? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            text = "Đăng ký ngay",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
