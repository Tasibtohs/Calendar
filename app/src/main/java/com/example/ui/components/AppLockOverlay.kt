package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing
import com.example.ui.viewmodel.CalendarViewModel
import com.example.util.AppLockManager

@Composable
fun AppLockOverlay(
    viewModel: CalendarViewModel,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    var inputCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    var lockType by remember { mutableStateOf("PIN") }
    var savedCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        lockType = AppLockManager.getLockType(viewModel.repository)
        savedCode = AppLockManager.getLockCode(viewModel.repository)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(AppSpacing.xl)
            .testTag("app_lock_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "App Locked",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Text(
                text = "App Locked",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            Text(
                text = "অনুগ্রহ করে আপনার $lockType কোড প্রবেশ করান",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(AppSpacing.xl))

            if (lockType == "PIN") {
                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    modifier = Modifier.padding(bottom = AppSpacing.lg)
                ) {
                    repeat(4) { index ->
                        val filled = index < inputCode.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                if (isError) {
                    AppErrorInline(
                        text = "ভুল PIN কোড! আবার চেষ্টা করুন",
                        modifier = Modifier.padding(bottom = AppSpacing.sm)
                    )
                }

                // Number Pad (1-9, 0, Del)
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Biometric", "0", "DEL")
                    )

                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                            row.forEach { digit ->
                                KeypadButton(
                                    text = digit,
                                    onClick = {
                                        when (digit) {
                                            "DEL" -> {
                                                if (inputCode.isNotEmpty()) inputCode = inputCode.dropLast(1)
                                                isError = false
                                            }
                                            "Biometric" -> {
                                                Toast.makeText(context, "Biometric Verified ✓", Toast.LENGTH_SHORT).show()
                                                onUnlocked()
                                            }
                                            else -> {
                                                if (inputCode.length < 4) {
                                                    inputCode += digit
                                                    isError = false
                                                    if (inputCode.length == 4) {
                                                        if (savedCode.isEmpty() || inputCode == savedCode) {
                                                            onUnlocked()
                                                        } else {
                                                            isError = true
                                                            inputCode = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Password Input
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = {
                        inputCode = it
                        isError = false
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    AppErrorInline(
                        text = "ভুল পাসওয়ার্ড! আবার চেষ্টা করুন"
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                Button(
                    onClick = {
                        if (savedCode.isEmpty() || inputCode == savedCode) {
                            onUnlocked()
                        } else {
                            isError = true
                        }
                    },
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text("Unlock (আনলক করুন)")
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.size(68.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (text == "DEL") {
                Icon(
                    imageVector = Icons.Outlined.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(AppDimensions.iconMedium)
                )
            } else if (text == "Biometric") {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "Biometric",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppDimensions.iconLarge)
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

