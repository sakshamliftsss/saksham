package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.StylizedTextField
import com.example.ui.components.ThemeGradients
import com.example.ui.viewmodel.SweetStockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeSetupScreen(
    viewModel: SweetStockViewModel,
    onSetupComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Form inputs
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var altMobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Sweet Shop") }
    var currency by remember { mutableStateOf("₹") }
    var language by remember { mutableStateOf("English") }
    var pinCode by remember { mutableStateOf("1234") }

    val scrollState = rememberScrollState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Logo Area
            Image(
                painter = painterResource(id = R.drawable.sweet_minimalist_logo_1783418252258),
                contentDescription = "SweetStock Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "SweetStock Pro",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Business Setup Wizard (Step $step of 3)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> {
                    // Step 1: Basic Profile Info
                    Text(
                        text = "Tell us about your business",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    StylizedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = "Shop Name *",
                        testTag = "setup_shop_name",
                        leadingIcon = { Icon(Icons.Default.Business, null) }
                    )

                    StylizedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = "Owner Name *",
                        testTag = "setup_owner_name",
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )

                    // Business Type Selector
                    Text("Business Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    val types = listOf("Sweet Shop", "Bakery", "Restaurant", "Dairy", "Cafe")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { type ->
                            FilterChip(
                                selected = businessType == type,
                                onClick = { businessType = type },
                                label = { Text(type) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryActionButton(
                        text = "Next Step",
                        onClick = {
                            if (shopName.isNotBlank() && ownerName.isNotBlank()) {
                                step = 2
                            }
                        },
                        testTag = "setup_step1_next"
                    )
                }

                2 -> {
                    // Step 2: Contact & Tax Information
                    Text(
                        text = "Contact & Localizations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    StylizedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = "Mobile Number *",
                        testTag = "setup_mobile",
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )

                    StylizedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        testTag = "setup_email",
                        leadingIcon = { Icon(Icons.Default.Email, null) }
                    )

                    StylizedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address *",
                        testTag = "setup_address",
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StylizedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = "City",
                            modifier = Modifier.weight(1f)
                        )
                        StylizedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = "State",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StylizedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = "Currency Symbol (e.g. ₹, $)",
                            modifier = Modifier.weight(1f)
                        )
                        StylizedTextField(
                            value = language,
                            onValueChange = { language = it },
                            label = "Language",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { step = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                if (mobile.isNotBlank() && address.isNotBlank()) {
                                    step = 3
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Next Step")
                        }
                    }
                }

                3 -> {
                    // Step 3: Security & Completion
                    Text(
                        text = "Security Setup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Create a 4-digit security PIN to restrict unauthorized access to your business summaries.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    StylizedTextField(
                        value = pinCode,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinCode = it },
                        label = "4-Digit Login PIN *",
                        testTag = "setup_pin",
                        leadingIcon = { Icon(Icons.Default.Lock, null) }
                    )

                    StylizedTextField(
                        value = gstNumber,
                        onValueChange = { gstNumber = it },
                        label = "GSTIN Number (Optional)",
                        testTag = "setup_gst"
                    )

                    StylizedTextField(
                        value = panNumber,
                        onValueChange = { panNumber = it },
                        label = "PAN Card Number (Optional)",
                        testTag = "setup_pan"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { step = 2 },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                if (pinCode.length == 4) {
                                    viewModel.completeFirstTimeSetup(
                                        shopName = shopName,
                                        ownerName = ownerName,
                                        mobile = mobile,
                                        altMobile = altMobile,
                                        email = email,
                                        address = address,
                                        city = city,
                                        state = state,
                                        country = country,
                                        gst = gstNumber,
                                        pan = panNumber,
                                        businessType = businessType,
                                        logoUri = "",
                                        currency = currency,
                                        pin = pinCode
                                    )
                                    onSetupComplete()
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Finish Setup")
                        }
                    }
                }
            }
        }
    }
}
