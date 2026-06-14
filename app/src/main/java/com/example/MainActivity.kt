package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F0F0F)),
                    containerColor = Color(0xFF0F0F0F)
                ) { innerPadding ->
                    GatewayMainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GatewayMainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var inputIpAddress by remember { mutableStateOf("192.168.1.254") }
    var activeUrlToLoad by remember { mutableStateOf("") }
    var isWebViewVisible by remember { mutableStateOf(false) }
    var isWifiEnabled by remember { mutableStateOf(false) }
    var isCredentialsExpanded by remember { mutableStateOf(false) }
    var isIpSettingsExpanded by remember { mutableStateOf(false) }

    // Periodically scanner network status
    LaunchedEffect(Unit) {
        while (true) {
            isWifiEnabled = isDeviceWifiConnected(context)
            delay(3000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        if (isWebViewVisible && activeUrlToLoad.isNotEmpty()) {
            RouterWebView(
                url = activeUrlToLoad,
                onClose = { isWebViewVisible = false },
                onExternalLaunch = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeUrlToLoad))
                    context.startActivity(intent)
                }
            )
        } else {
            // Dashboard Panel scrollable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header (Responsive / Elegant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gateway",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFD0BCFE)
                            ),
                            modifier = Modifier.testTag("app_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "LINK PRO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E5).copy(alpha = 0.6f)
                            )
                        )
                    }
                    
                    // Elegant header icon placeholder
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF2B2930), RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .border(2.dp, Color(0xFFD0BCFE), RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Centered Pulse Wave element
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RouterPulseCanvas()
                }

                // Primary Gateway Connection Portal Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C1B1F)
                    ),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header info in Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "TARGET ADDRESS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Color(0xFFD0BCFE)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = inputIpAddress,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            Surface(
                                color = Color(0xFF381E72),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "LOCAL HUB",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFFEADDFF),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Status elements list
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row 1: Wi-Fi dynamic connectivity
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(
                                                (if (isWifiEnabled) Color(0xFF4ADE80) else Color(0xFFEF4444)).copy(alpha = 0.2f),
                                                CircleShape
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (isWifiEnabled) Color(0xFF4ADE80) else Color(0xFFEF4444),
                                                CircleShape
                                            )
                                    )
                                }
                                Text(
                                    text = if (isWifiEnabled) "Network connection stable" else "No physical Wi-Fi detected",
                                    color = Color(0xFFE6E1E5).copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Row 2: Encrypted Tunnel indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(Color(0xFF60A5FA).copy(alpha = 0.2f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF60A5FA), CircleShape)
                                    )
                                }
                                Text(
                                    text = "Encrypted tunnel active",
                                    color = Color(0xFFE6E1E5).copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Open Portal CTA button
                        Button(
                            onClick = {
                                val formattedUrl = if (!inputIpAddress.startsWith("http://") && !inputIpAddress.startsWith("https://")) {
                                    "http://$inputIpAddress"
                                } else {
                                    inputIpAddress
                                }
                                activeUrlToLoad = formattedUrl
                                isWebViewVisible = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFE),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("connect_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Open Dashboard",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Link Arrow",
                                    tint = Color(0xFF381E72),
                                    modifier = Modifier.size(18.dp).rotate(-45f)
                                )
                            }
                        }
                    }
                }

                // Quick Stats Row matching the mock design grid layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "SIGNAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE6E1E5).copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isWifiEnabled) "-42 dBm" else "Offline",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFFE6E1E5)
                                )
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "UPTIME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE6E1E5).copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isWifiEnabled) "14d 2h" else "0d 0h",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFFE6E1E5)
                                )
                            )
                        }
                    }
                }

                // Config log and tools buttons (from Secondary Actions at bottom of list)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { isIpSettingsExpanded = !isIpSettingsExpanded },
                        border = BorderStroke(1.dp, Color(0xFFD0BCFE).copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFE)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.weight(1f).testTag("config_toggle_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = if (isIpSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand Config Tools",
                                tint = Color(0xFFD0BCFE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Config Tools", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = { isCredentialsExpanded = !isCredentialsExpanded },
                        border = BorderStroke(1.dp, Color(0xFFD0BCFE).copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFE)),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = if (isCredentialsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand Passwords guide",
                                tint = Color(0xFFD0BCFE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Security Log", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Collapsible settings fields inside the beautiful card
                AnimatedVisibility(visible = isIpSettingsExpanded) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "IP Gateways Setup",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )

                            val keyboardController = LocalSoftwareKeyboardController.current
                            OutlinedTextField(
                                value = inputIpAddress,
                                onValueChange = { inputIpAddress = it.trim() },
                                label = { Text("Gateway Web Address", color = Color.LightGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFD0BCFE),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedLabelColor = Color(0xFFD0BCFE),
                                    unfocusedLabelColor = Color.LightGray
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { keyboardController?.hide() }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ip_input")
                            )

                            Text(
                                text = "Quick IP Address Presets:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("192.168.1.254", "192.168.1.1", "192.168.0.1").forEach { ip ->
                                    Surface(
                                        color = if (inputIpAddress == ip) Color(0xFFD0BCFE) else Color(0xFF0F0F0F),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, if (inputIpAddress == ip) Color(0xFFD0BCFE) else Color.White.copy(alpha = 0.05f)),
                                        modifier = Modifier
                                            .clickable { inputIpAddress = ip }
                                            .testTag("preset_$ip")
                                    ) {
                                        Text(
                                            text = ip,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (inputIpAddress == ip) Color(0xFF381E72) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Alternative Launch External button (pill/outline card layout)
                OutlinedButton(
                    onClick = {
                        val formattedUrl = if (!inputIpAddress.startsWith("http://") && !inputIpAddress.startsWith("https://")) {
                            "http://$inputIpAddress"
                        } else {
                            inputIpAddress
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                        context.startActivity(intent)
                    },
                    border = BorderStroke(1.dp, Color(0xFFD0BCFE).copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD0BCFE)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("external_button")
                ) {
                    Text(
                        text = "open in dynamic browser",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    )
                }

                // Credentials guide card
                AnimatedVisibility(visible = isCredentialsExpanded) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1C1B1F)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Credentials Info",
                                    tint = Color(0xFFD0BCFE),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Common Router Credentials",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Standard admin credentials combinations used by common physical routers:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )

                            // Clean table grid of rows
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CredentialRow(brand = "Default / Huawei", user = "admin", pass = "admin")
                                CredentialRow(brand = "Netgear", user = "admin", pass = "password")
                                CredentialRow(brand = "Linksys", user = "admin", pass = "[leave blank]")
                                CredentialRow(brand = "TP-Link", user = "admin", pass = "admin")
                                CredentialRow(brand = "ZTE", user = "admin", pass = "admin")
                            }

                            Text(
                                text = "Note: Standard passwords are often printed on the physical label on the bottom / back of your physical router device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun CredentialRow(brand: String, user: String, pass: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0F0F), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.03f)), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = brand, fontWeight = FontWeight.SemiBold, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(text = "user: $user", color = Color(0xFFD0BCFE), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(text = "pass: $pass", color = Color.LightGray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
    }
}

@Composable
fun RouterPulseCanvas() {
    val transition = rememberInfiniteTransition()
    val pulseAlpha1 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val pulseRadius1 by transition.animateFloat(
        initialValue = 10f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val pulseAlpha2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val pulseRadius2 by transition.animateFloat(
        initialValue = 10f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        
        // Circular waves
        drawCircle(
            color = Color(0xFFD0BCFE).copy(alpha = pulseAlpha1 * 0.4f),
            radius = pulseRadius1 * density,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFD0BCFE).copy(alpha = pulseAlpha2 * 0.4f),
            radius = pulseRadius2 * density,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Outer core housing
        drawCircle(
            color = Color(0xFFD0BCFE).copy(alpha = 0.15f),
            radius = 28.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color(0xFFD0BCFE),
            radius = 16.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color(0xFF0F0F0F),
            radius = 8.dp.toPx(),
            center = center
        )
        
        // Antenna bars style
        drawRect(
            color = Color(0xFFD0BCFE),
            topLeft = Offset(center.x - 4.dp.toPx(), center.y - 32.dp.toPx()),
            size = Size(8.dp.toPx(), 18.dp.toPx()),
            style = Fill
        )
    }
}

// Helpers for float constants in remember transition
private fun Float.toDpValue() = this

fun isDeviceWifiConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}

@Composable
fun RouterWebView(
    url: String,
    onClose: () -> Unit,
    onExternalLaunch: () -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorDescription by remember { mutableStateOf("") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Control panel
            Surface(
                color = Color(0xFF1C1B1F),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("wv_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Leave webview",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { webView?.goBack() },
                        enabled = canGoBack,
                        modifier = Modifier.testTag("wv_back")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back in history",
                            tint = if (canGoBack) Color.White else Color.DarkGray
                        )
                    }
                    
                    // Rotating the ArrowBack to serve as the ArrowForward
                    IconButton(
                        onClick = { webView?.goForward() },
                        enabled = canGoForward,
                        modifier = Modifier
                            .rotate(180f)
                            .testTag("wv_forward")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate forward in history",
                            tint = if (canGoForward) Color.White else Color.DarkGray
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            hasError = false
                            webView?.reload()
                        },
                        modifier = Modifier.testTag("wv_refresh")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Portal page",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = url.removePrefix("http://").removePrefix("https://").trimEnd('/'),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Surface(
                        color = Color(0xFFD0BCFE).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable { onExternalLaunch() }
                            .testTag("wv_external")
                    ) {
                        Text(
                            text = "BROWSER",
                            color = Color(0xFFD0BCFE),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // progress indicator bar
            if (isLoading && !hasError) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFFD0BCFE),
                    trackColor = Color(0xFF0F0F0F)
                )
            }

            if (hasError) {
                TroubleshootingErrorCard(
                    url = url,
                    errorMsg = errorDescription,
                    onRetry = {
                        hasError = false
                        webView?.reload()
                    },
                    onOpenExternal = onExternalLaunch
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webView = this
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                databaseEnabled = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    hasError = false
                                }

                                override fun onPageFinished(view: WebView?, loadedUrl: String?) {
                                    isLoading = false
                                    canGoBack = view?.canGoBack() ?: false
                                    canGoForward = view?.canGoForward() ?: false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    errorCode: Int,
                                    description: String?,
                                    failingUrl: String?
                                ) {
                                    hasError = true
                                    errorDescription = "$description (Code: $errorCode)"
                                }

                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?
                                ) {
                                    // Router admin setups usually use custom self-signed SSL/TLS certificates; proceed past them
                                    handler?.proceed()
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress
                                }
                            }

                            loadUrl(url)
                        }
                    },
                    update = { view ->
                        if (view.url != url) {
                            view.loadUrl(url)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
fun TroubleshootingErrorCard(
    url: String,
    errorMsg: String,
    onRetry: () -> Unit,
    onOpenExternal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert connection error",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Loading Portal Failed",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = "Unable to connect to: $url",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
        
        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Troubleshooting Tips",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                TroubleshootingStepItem(
                    step = "1",
                    text = "Verify Wi-Fi Network Connection: Ensure your mobile device is actively connected to the physical Wi-Fi network hosted by the specific router or modem, rather than mobile cellular data (LTE/5G)."
                )
                
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.08f)))
                
                TroubleshootingStepItem(
                    step = "2",
                    text = "Check Target Link Gateway Address: Verify if the default IP address is printed on the hardware model. Modems occasionally run on 192.168.1.1, 192.168.0.1, or 10.0.0.1. (You can customize the target IP address in the config panel)."
                )
                
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.08f)))
                
                TroubleshootingStepItem(
                    step = "3",
                    text = "Reboot Router Hardware: If the broadband router has just completed a reboot, give it up to 180 seconds to fully initialize its system administration portals."
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFE), contentColor = Color(0xFF381E72)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Try Loading Again", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onOpenExternal,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFE)),
            border = BorderStroke(1.dp, Color(0xFFD0BCFE)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Open in External Browser", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TroubleshootingStepItem(step: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFFD0BCFE).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = Color(0xFFD0BCFE),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = text,
            color = Color.LightGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}
