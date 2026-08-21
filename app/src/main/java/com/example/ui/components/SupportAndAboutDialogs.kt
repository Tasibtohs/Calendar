package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppDimensions
import com.example.ui.theme.AppRadius
import com.example.ui.theme.AppSpacing

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppRadius.dialog),
        modifier = Modifier.testTag("about_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "About App",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconMedium)
                        )
                    }
                }
                Text(
                    text = "About Application",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Column {
                    Text(
                        text = "Personal Calendar & Planner Pro",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = "Version 6.0.0 Pro • 12 August 2026",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "একটি সম্পূর্ণ অফলাইন ভিত্তিক প্রফেশনাল ক্যালেন্ডার ও পার্সোনাল প্ল্যানার সিস্টেম। এতে রয়েছে ইংরেজি (Gregorian), বাংলা এবং হিজরী ক্যালেন্ডার, ইভেন্ট, টাস্ক, নোটস, কাউন্টডাউন, অ্যাপ লক ও ডাটা প্রাইভেসির পূর্ণ নিশ্চয়তা।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Developer Note
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(AppRadius.md),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.md)) {
                        Text(
                            text = "Developer Note (ডেভেলপার বার্তা):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = "“শব্দের প্রতি ভালোবাসা থেকেই এই ছোট্ট প্রয়াস—সময়, তারিখ আর স্মৃতিগুলোকে একটু সুন্দরভাবে ধরে রাখার জন্য।”",
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Text(
                    text = "What's New in v6.0.0",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "• Dual Bangla & Hijri Moon Calendar Integration\n• Complete Offline-First Architecture & Local JSON Backup/Restore\n• ICS Calendar Import/Export\n• Biometric, PIN, and Password App Lock Security\n• Custom Cover Banner Photo & Quick Add System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = { showPrivacyPolicy = true },
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Privacy Policy", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { showTerms = true },
                        shape = RoundedCornerShape(AppRadius.md),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Terms of Service", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text("Close (ঠিক আছে)")
            }
        }
    )

    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "Privacy Policy (গোপনীয়তা নীতি)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "১. সকল ডাটা শুধুমাত্র আপনার লোকাল ডিভাইসে সংরক্ষিত থাকে।\n" +
                            "২. কোনো ডাটা সার্ভারে আপলোড বা শেয়ার করা হয় না।\n" +
                            "৩. অ্যাপ ব্যবহারের জন্য কোনো অ্যাকাউন্ট খোলা বা অনলাইন সংযোগ বাধ্যতামূলক নয়।\n" +
                            "৪. আপনার সম্পূর্ণ গোপনীয়তা নিশ্চিত করা হয়েছে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showPrivacyPolicy = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            shape = RoundedCornerShape(AppRadius.dialog),
            title = {
                Text(
                    text = "Terms of Service (ব্যবহারের শর্তাবলী)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "১. এই অ্যাপটি ব্যক্তিগত ব্যবহারের জন্য সম্পূর্ণ বিনামূল্যে প্রদান করা হয়েছে।\n" +
                            "২. লোকাল ব্যাকআপ নিয়মিত ডিভাইসে সংরক্ষণ করার পরামর্শ দেওয়া হচ্ছে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showTerms = false },
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var feedbackType by remember { mutableStateOf("Feedback") } // Feedback, Bug Report, Feature Request
    var messageText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(AppRadius.dialog),
        modifier = Modifier.testTag("support_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.SupportAgent,
                            contentDescription = "Support",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AppDimensions.iconMedium)
                        )
                    }
                }
                Text(
                    text = "Support & Feedback",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Text(
                    text = "Select Support Category:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    listOf("Feedback", "Bug Report", "Feature Request").forEach { cat ->
                        val isSelected = feedbackType == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { feedbackType = cat },
                            label = {
                                Text(
                                    text = if (cat == "Feature Request") "Feature" else cat,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(AppRadius.full)
                        )
                    }
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Write your message / feedback here") },
                    placeholder = { Text("Describe your feedback or suggestion...") },
                    shape = RoundedCornerShape(AppRadius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "$feedbackType Submitted Locally! Thank you. ✓", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                enabled = messageText.isNotBlank(),
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text("Submit Message")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text("Cancel")
            }
        }
    )
}

