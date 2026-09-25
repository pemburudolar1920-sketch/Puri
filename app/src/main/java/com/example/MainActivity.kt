package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.EmergencyDialog
import com.example.ui.screens.*
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RtViewModel
import com.example.ui.viewmodel.RtViewModelFactory
import com.example.ui.viewmodel.ScreenRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val app = application as RtPuriPratamaApp
                val rtViewModel: RtViewModel = viewModel(
                    factory = RtViewModelFactory(app.repository)
                )

                MainAppRoot(viewModel = rtViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppRoot(viewModel: RtViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showEmergencyDialog by remember { mutableStateOf(false) }

    // Handle system back navigation: return to HOME if currently on another screen
    BackHandler(enabled = currentScreen != ScreenRoute.HOME) {
        viewModel.navigateTo(ScreenRoute.HOME)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RT PURI PRATAMA",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (currentUser?.role == UserRole.PENGURUS_RT) "Mode: Pengurus RT" else "Mode: Warga",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimaryDark,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // Tombol Darurat SOS in TopBar
                    IconButton(
                        onClick = { showEmergencyDialog = true },
                        modifier = Modifier.testTag("topbar_sos_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CoralDanger),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = "Tombol Darurat",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Profile / Account
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenRoute.AKUN) },
                        modifier = Modifier.testTag("topbar_account_button")
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Akun & Profil",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavBarItem(
                    selected = currentScreen == ScreenRoute.HOME,
                    onClick = { viewModel.navigateTo(ScreenRoute.HOME) },
                    icon = Icons.Default.Home,
                    label = "Beranda",
                    tag = "nav_home"
                )
                NavBarItem(
                    selected = currentScreen == ScreenRoute.KAS_IURAN,
                    onClick = { viewModel.navigateTo(ScreenRoute.KAS_IURAN) },
                    icon = Icons.Default.Payments,
                    label = "Kas & Iuran",
                    tag = "nav_kas"
                )
                NavBarItem(
                    selected = currentScreen == ScreenRoute.DATA_WARGA,
                    onClick = { viewModel.navigateTo(ScreenRoute.DATA_WARGA) },
                    icon = Icons.Default.PeopleAlt,
                    label = "Data Warga",
                    tag = "nav_warga"
                )
                NavBarItem(
                    selected = currentScreen == ScreenRoute.CCTV,
                    onClick = { viewModel.navigateTo(ScreenRoute.CCTV) },
                    icon = Icons.Default.Videocam,
                    label = "CCTV RT",
                    tag = "nav_cctv"
                )
                NavBarItem(
                    selected = currentScreen == ScreenRoute.PELAPORAN ||
                            currentScreen == ScreenRoute.FORUM ||
                            currentScreen == ScreenRoute.PENGUMUMAN ||
                            currentScreen == ScreenRoute.AKUN,
                    onClick = {
                        // Cycles through the services
                        when (currentScreen) {
                            ScreenRoute.PELAPORAN -> viewModel.navigateTo(ScreenRoute.FORUM)
                            ScreenRoute.FORUM -> viewModel.navigateTo(ScreenRoute.PENGUMUMAN)
                            ScreenRoute.PENGUMUMAN -> viewModel.navigateTo(ScreenRoute.AKUN)
                            else -> viewModel.navigateTo(ScreenRoute.PELAPORAN)
                        }
                    },
                    icon = Icons.Default.Widgets,
                    label = "Layanan",
                    tag = "nav_layanan"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "screen_crossfade"
            ) { screen ->
                when (screen) {
                    ScreenRoute.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenEmergency = { showEmergencyDialog = true }
                    )
                    ScreenRoute.KAS_IURAN -> IuranKasScreen(viewModel = viewModel)
                    ScreenRoute.DATA_WARGA -> DataWargaScreen(viewModel = viewModel)
                    ScreenRoute.CCTV -> CctvScreen(viewModel = viewModel)
                    ScreenRoute.PELAPORAN -> PelaporanScreen(viewModel = viewModel)
                    ScreenRoute.FORUM -> ForumScreen(viewModel = viewModel)
                    ScreenRoute.PENGUMUMAN -> PengumumanScreen(viewModel = viewModel)
                    ScreenRoute.AKUN -> AuthScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Emergency SOS Dialog
    if (showEmergencyDialog) {
        EmergencyDialog(
            user = currentUser,
            onDismiss = { showEmergencyDialog = false }
        )
    }
}

@Composable
fun RowScope.NavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    tag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmeraldPrimary,
            selectedTextColor = EmeraldPrimary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag(tag)
    )
}
