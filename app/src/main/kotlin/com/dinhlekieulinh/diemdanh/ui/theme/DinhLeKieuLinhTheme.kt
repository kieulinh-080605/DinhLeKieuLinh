package com.dinhlekieulinh.diemdanh.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Cấu hình Bảng màu Sáng (Light Scheme)
private val DinhLeKieuLinhLightColorScheme = lightColorScheme(
    primary = DinhLeKieuLinhPrimaryLight,
    onPrimary = DinhLeKieuLinhOnPrimaryLight,
    primaryContainer = DinhLeKieuLinhPrimaryContainerLight,
    onPrimaryContainer = DinhLeKieuLinhOnPrimaryContainerLight,
    secondary = DinhLeKieuLinhSecondaryLight,
    onSecondary = DinhLeKieuLinhOnSecondaryLight,
    secondaryContainer = DinhLeKieuLinhSecondaryContainerLight,
    onSecondaryContainer = DinhLeKieuLinhOnSecondaryContainerLight,
    tertiary = DinhLeKieuLinhTertiaryLight,
    onTertiary = DinhLeKieuLinhOnTertiaryLight,
    background = DinhLeKieuLinhBackgroundLight,
    onBackground = DinhLeKieuLinhOnBackgroundLight,
    surface = DinhLeKieuLinhSurfaceLight,
    onSurface = DinhLeKieuLinhOnSurfaceLight,
    surfaceVariant = DinhLeKieuLinhSurfaceVariantLight,
    onSurfaceVariant = DinhLeKieuLinhOnSurfaceVariantLight
)

// Cấu hình Bảng màu Tối (Dark Scheme)
private val DinhLeKieuLinhDarkColorScheme = darkColorScheme(
    primary = DinhLeKieuLinhPrimaryDark,
    onPrimary = DinhLeKieuLinhOnPrimaryDark,
    primaryContainer = DinhLeKieuLinhPrimaryContainerDark,
    onPrimaryContainer = DinhLeKieuLinhOnPrimaryContainerDark,
    secondary = DinhLeKieuLinhSecondaryDark,
    onSecondary = DinhLeKieuLinhOnSecondaryDark,
    secondaryContainer = DinhLeKieuLinhSecondaryContainerDark,
    onSecondaryContainer = DinhLeKieuLinhOnSecondaryContainerDark,
    tertiary = DinhLeKieuLinhTertiaryDark,
    onTertiary = DinhLeKieuLinhOnTertiaryDark,
    background = DinhLeKieuLinhBackgroundDark,
    onBackground = DinhLeKieuLinhOnBackgroundDark,
    surface = DinhLeKieuLinhSurfaceDark,
    onSurface = DinhLeKieuLinhOnSurfaceDark,
    surfaceVariant = DinhLeKieuLinhSurfaceVariantDark,
    onSurfaceVariant = DinhLeKieuLinhOnSurfaceVariantDark
)

@Composable
fun DinhLeKieuLinhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Mặc định dùng màu xanh dương đặc trưng của đồ án
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DinhLeKieuLinhDarkColorScheme
        else -> DinhLeKieuLinhLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DinhLeKieuLinhTypography,
        content = content
    )
}
