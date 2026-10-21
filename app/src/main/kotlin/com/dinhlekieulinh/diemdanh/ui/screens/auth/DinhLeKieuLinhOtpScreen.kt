package com.dinhlekieulinh.diemdanh.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhUser
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhButton
import com.dinhlekieulinh.diemdanh.ui.components.DinhLeKieuLinhTopAppBar

@Composable
fun DinhLeKieuLinhOtpScreen(
    viewModel: DinhLeKieuLinhAuthViewModel,
    onNavigateBack: () -> Unit,
    onOtpSuccess: (user: DinhLeKieuLinhUser) -> Unit
) {
    val state by viewModel.otpState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            DinhLeKieuLinhTopAppBar(
                title = "Xác thực mã OTP",
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
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = "OTP",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Xác thực 2 bước (2FA)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mã xác thực gồm 6 chữ số đã được gửi tới email:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = state.email.ifBlank { "email_sinhvien@edu.vn" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Ô nhập mã OTP 6 số thiết kế to rõ
                OutlinedTextField(
                    value = state.otp,
                    onValueChange = viewModel::onOtpChanged,
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    placeholder = {
                        Text(
                            text = "------",
                            style = TextStyle(
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center,
                                letterSpacing = 8.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    isError = state.otpError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(68.dp)
                )

                if (state.otpError != null) {
                    Text(
                        text = state.otpError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị mã OTP ngẫu nhiên vừa gửi để người dùng kiểm tra/nhập
                state.generatedOtp?.let { code ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Mã OTP ngẫu nhiên nhận được: ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = code,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AssistChip(
                        onClick = { viewModel.onOtpChanged(code) },
                        label = { Text("Điền nhanh mã OTP: $code") },
                        leadingIcon = {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, Modifier.size(16.dp))
                        }
                    )
                } ?: run {
                    AssistChip(
                        onClick = { viewModel.onOtpChanged("123456") },
                        label = { Text("Điền mã OTP Demo") },
                        leadingIcon = {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, Modifier.size(16.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Đếm ngược thời gian gửi lại OTP
                if (state.countdownSeconds > 0) {
                    Text(
                        text = "Gửi lại mã sau 00:${state.countdownSeconds.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    TextButton(onClick = viewModel::resendOtp) {
                        Text(
                            text = "Gửi lại mã xác thực mới",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

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

                Spacer(modifier = Modifier.height(32.dp))

                // Nút Xác nhận OTP
                DinhLeKieuLinhButton(
                    text = "Xác nhận OTP",
                    onClick = { viewModel.verifyOtp(onOtpSuccess) },
                    isLoading = state.isLoading,
                    icon = Icons.Default.VerifiedUser
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
