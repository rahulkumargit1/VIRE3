package com.example.payoffline

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.payoffline.ui.screens.*
import com.example.payoffline.ui.theme.*
import com.example.payoffline.viewmodel.UssdViewModel
import kotlinx.coroutines.delay

sealed class NavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
) {
    object Pay      : NavTab("pay",      "Pay",      Icons.Outlined.Send,           Icons.Filled.Send)
    object Balance  : NavTab("balance",  "Balance",  Icons.Outlined.AccountBalance, Icons.Filled.AccountBalance)
    object History  : NavTab("history",  "History",  Icons.Outlined.ReceiptLong,    Icons.Filled.ReceiptLong)
    object Settings : NavTab("settings", "Settings", Icons.Outlined.Settings,       Icons.Filled.Settings)
}

val allTabs = listOf(NavTab.Pay, NavTab.Balance, NavTab.History, NavTab.Settings)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: UssdViewModel = viewModel()
            val settings by vm.settings.collectAsState()
            PayOfflineTheme(darkTheme = settings.darkMode) {
                AppRoot(vm = vm)
            }
        }
    }
}

// ─── Splash Screen ─────────────────────────────────────────────────────────
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(400)
        subtitleVisible = true
        delay(300)
        taglineVisible = true
        delay(2200)  // Total splash ≈ 3 seconds
        onFinished()
    }

    val gradient = Brush.verticalGradient(
        listOf(Violet800, Indigo700, Color(0xFF1E1B4B))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glowing logo circle
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.4f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(listOf(Cyan400.copy(alpha = 0.3f), Color.Transparent)),
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        tonalElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.CurrencyRupee,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // App Name
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn(tween(500))
            ) {
                Text(
                    text = "VIRE",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 12.sp,
                        fontSize = 56.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            // Tagline
            AnimatedVisibility(
                visible = subtitleVisible,
                enter = slideInVertically(initialOffsetY = { 20 }) + fadeIn(tween(400))
            ) {
                Text(
                    text = "Offline UPI, Reimagined.",
                    style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 1.sp),
                    color = Cyan400,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(64.dp))

            // Created by
            AnimatedVisibility(
                visible = taglineVisible,
                enter = fadeIn(tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Created by",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Rahul",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// ─── App Root with Splash ──────────────────────────────────────────────────
@Composable
fun AppRoot(vm: UssdViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = {
            if (targetState) {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            } else {
                fadeIn(tween(700)) togetherWith fadeOut(tween(400))
            }
        },
        label = "splash_to_app"
    ) { splashVisible ->
        if (splashVisible) {
            SplashScreen(onFinished = { showSplash = false })
        } else {
            AppContent(vm = vm)
        }
    }
}

// ─── Main App ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(vm: UssdViewModel) {
    val context  = LocalContext.current
    val settings by vm.settings.collectAsState()

    var currentTab        by remember { mutableStateOf<NavTab>(NavTab.Pay) }
    var hasCallPermission by remember { mutableStateOf(false) }
    var isAuthenticated   by remember { mutableStateOf(!settings.biometricEnabled) }

    LaunchedEffect(Unit) {
        hasCallPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        vm.loadSims()
    }

    LaunchedEffect(settings.biometricEnabled) {
        if (!settings.biometricEnabled) isAuthenticated = true
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasCallPermission = results[Manifest.permission.CALL_PHONE] == true
        if (results[Manifest.permission.READ_PHONE_STATE] == true ||
            results[Manifest.permission.READ_PHONE_NUMBERS] == true) {
            vm.loadSims()
        }
    }

    LaunchedEffect(Unit) {
        permLauncher.launch(arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
        ))
    }

    if (!isAuthenticated && settings.biometricEnabled) {
        LaunchedEffect(Unit) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            val bioManager = BiometricManager.from(context)
            if (bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS) {
                val executor = ContextCompat.getMainExecutor(context)
                val prompt = BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            isAuthenticated = true
                        }
                    })
                prompt.authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("VIRE")
                        .setSubtitle("Authenticate to continue")
                        .setNegativeButtonText("Cancel")
                        .build()
                )
            } else {
                isAuthenticated = true
            }
        }
    }

    if (!isAuthenticated) {
        val gradient = Brush.verticalGradient(listOf(Violet800, Indigo700, Color(0xFF1E1B4B)))
        Surface(Modifier.fillMaxSize(), color = Color.Transparent) {
            Box(
                Modifier.fillMaxSize().background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CurrencyRupee, null,
                        tint = Cyan400,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("VIRE",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 8.sp
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Authenticating…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cyan400
                    )
                }
            }
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 8.dp) {
                allTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick  = { currentTab = tab },
                        icon = {
                            Icon(
                                if (currentTab == tab) tab.iconSelected else tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    NavTab.Pay      -> PayScreen(vm, onRequestPermission = {
                        permLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
                    }, hasCallPermission = hasCallPermission)
                    NavTab.Balance  -> BalanceScreen(vm)
                    NavTab.History  -> HistoryScreen(vm)
                    NavTab.Settings -> SettingsScreen(vm)
                }
            }
        }
    }
}
