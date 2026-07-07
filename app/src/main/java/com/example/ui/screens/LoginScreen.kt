package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.ThemeGradients
import com.example.ui.viewmodel.SweetStockViewModel

@Composable
fun LoginScreen(
    viewModel: SweetStockViewModel,
    onLoginSuccess: () -> Unit
) {
    val shopConfig by viewModel.shopConfig.collectAsState()
    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showForgotDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.sweet_minimalist_logo_1783418252258),
                contentDescription = "SweetStock Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(26.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = shopConfig?.shopName ?: "SweetStock Pro",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Secure Business Terminal",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Enter PIN Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    val filled = pinText.length >= i
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Custom secure numpad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val row1 = listOf("1", "2", "3")
                val row2 = listOf("4", "5", "6")
                val row3 = listOf("7", "8", "9")

                listOf(row1, row2, row3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { num ->
                            Button(
                                onClick = {
                                    if (pinText.length < 4) {
                                        pinText += num
                                        if (pinText.length == 4) {
                                            if (viewModel.loginWithPin(pinText)) {
                                                onLoginSuccess()
                                            } else {
                                                errorMessage = "Incorrect security PIN. Please try again."
                                                pinText = ""
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(num, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Clear button
                    Button(
                        onClick = { pinText = "" },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("C", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    // 0 button
                    Button(
                        onClick = {
                            if (pinText.length < 4) {
                                pinText += "0"
                                if (pinText.length == 4) {
                                    if (viewModel.loginWithPin(pinText)) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Incorrect security PIN. Please try again."
                                        pinText = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("0", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    // Biometric/Fingerprint Mock trigger
                    Button(
                        onClick = {
                            if (viewModel.loginWithPin(shopConfig?.pinCode ?: "1234")) {
                                onLoginSuccess()
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Autologin"
                        )
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { showForgotDialog = true }) {
                Text("Forgot PIN?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Forgotten Security PIN") },
            text = { 
                Text("For security, write down your configuration PIN. Currently, your registered login PIN is: ${shopConfig?.pinCode ?: "1234"}\nKeep it safe and confidential.")
            },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Ok, Got It")
                }
            }
        )
    }
}
