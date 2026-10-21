package com.dinhlekieulinh.diemdanh.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhRole
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhButton
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhTextField
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinhLeKieuLinhRegisterScreen(
    viewModel: DinhLeKieuLinhAuthViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by viewModel.registerState.collectAsState()
    val scrollState = rememberScrollState()
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DinhLeKieuLinhTopAppBar(
                title = "Đăng ký tài khoản",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
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
                Text(
                    text = "Tạo tài khoản mới",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Vui lòng điền thông tin bên dưới để đăng ký",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Họ và tên
                DinhLeKieuLinhTextField(
                    value = state.hoTen,
                    onValueChange = viewModel::onRegisterHoTenChanged,
                    label = "Họ và tên",
                    placeholder = "Đinh Lê Kiều Linh",
                    leadingIcon = Icons.Default.Badge,
                    errorMessage = state.hoTenError
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mã số (MSSV / MSGV)
                DinhLeKieuLinhTextField(
                    value = state.maSo,
                    onValueChange = viewModel::onRegisterMaSoChanged,
                    label = "Mã số (MSSV / MSGV)",
                    placeholder = "SV2026001",
                    leadingIcon = Icons.Default.Numbers,
                    errorMessage = state.maSoError
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                DinhLeKieuLinhTextField(
                    value = state.email,
                    onValueChange = viewModel::onRegisterEmailChanged,
                    label = "Email",
                    placeholder = "kieulinh@student.edu.vn",
                    leadingIcon = Icons.Default.Email,
                    errorMessage = state.emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tên đăng nhập
                DinhLeKieuLinhTextField(
                    value = state.username,
                    onValueChange = viewModel::onRegisterUsernameChanged,
                    label = "Tên đăng nhập",
                    placeholder = "kieulinh2026",
                    leadingIcon = Icons.Default.Person,
                    errorMessage = state.usernameError
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Số điện thoại
                DinhLeKieuLinhTextField(
                    value = state.soDienThoai,
                    onValueChange = viewModel::onRegisterSoDienThoaiChanged,
                    label = "Số điện thoại",
                    placeholder = "0987654321",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vai trò (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.role.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vai trò") },
                        leadingIcon = { Icon(Icons.Default.ManageAccounts, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        DinhLeKieuLinhRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.displayName) },
                                onClick = {
                                    viewModel.onRegisterRoleChanged(role)
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mật khẩu
                DinhLeKieuLinhTextField(
                    value = state.password,
                    onValueChange = viewModel::onRegisterPasswordChanged,
                    label = "Mật khẩu",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.Lock,
                    trailingIcon = {
                        IconButton(onClick = viewModel::toggleRegisterPasswordVisibility) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    errorMessage = state.passwordError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Xác nhận mật khẩu
                DinhLeKieuLinhTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onRegisterConfirmPasswordChanged,
                    label = "Xác nhận mật khẩu",
                    placeholder = "••••••••",
                    leadingIcon = Icons.Default.LockReset,
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    errorMessage = state.confirmPasswordError,
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

                // Nút Đăng ký
                DinhLeKieuLinhButton(
                    text = "Đăng ký tài khoản",
                    onClick = { viewModel.register(onRegisterSuccess) },
                    isLoading = state.isLoading,
                    icon = Icons.Default.PersonAdd
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
