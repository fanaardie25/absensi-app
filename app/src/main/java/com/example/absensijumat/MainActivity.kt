package com.example.absensijumat

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.absensijumat.ui.auth.ChangePasswordScreen
import com.example.absensijumat.ui.auth.LoginScreen
import com.example.absensijumat.ui.fakegps.FakeGpsScreen
import com.example.absensijumat.ui.history.HistoryScreen
import com.example.absensijumat.ui.home.Home
import com.example.absensijumat.ui.home.HomeViewModel
import com.example.absensijumat.ui.profile.ProfileScreen
import com.example.absensijumat.ui.theme.AbsensiJumatTheme
import com.example.absensijumat.ui.yasin.YasinScreen
import com.example.absensijumat.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(this)
        enableEdgeToEdge()
        setContent {
            var startDestination by remember {
                mutableStateOf(if (sessionManager.fetchAuthToken() != null) "home" else "login")
            }

            var isFakeGpsDetected by remember { mutableStateOf(false) }

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isFakeGpsDetected = checkIsMockLocationActive(this@MainActivity)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(
                            this,
                            "Izin notifikasi ditolak. Anda mungkin tidak menerima pemberitahuan penting.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                LaunchedEffect(Unit) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            AbsensiJumatTheme {
                if (isFakeGpsDetected) {
                    FakeGpsScreen()
                } else if (startDestination == "home") {
                    AbsensiJumatApp()
                } else {
                    LoginScreen(onLoginSuccess = { token ->
                        Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                        startDestination = "home"
                    })
                }
            }
        }
    }

    private fun checkIsMockLocationActive(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    val isMocked = isLocationMocked(location)

                    if (isMocked) {
                        val ageInMs = System.currentTimeMillis() - location.time
                        if (ageInMs < 1 * 30 * 1000) {
                            return true
                        }
                    }
                }
            }
            false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isLocationMocked(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }
}

@Composable
fun AbsensiJumatApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val primaryColor = Color(0xFF00A36C)
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    
    var showChangePassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getCurrentUser(context)
    }

    LaunchedEffect(viewModel.userData) {
        if (viewModel.userData?.must_change_password == true) {
            showChangePassword = true
        }
    }

    if (showChangePassword) {
        ChangePasswordScreen(onSuccess = {
            showChangePassword = false
            viewModel.getCurrentUser(context)
        })
    } else {
        Scaffold(
            bottomBar = {
                // Melayang (Floating) Bottom Navigation dengan Animasi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .shadow(5.dp, RoundedCornerShape(24.dp), spotColor = primaryColor),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppDestinations.entries.forEach { destination ->
                                val isSelected = currentDestination == destination
                                
                                // Animasi perubahan warna icon & background
                                val iconColor by animateColorAsState(
                                    targetValue = if (isSelected) primaryColor else Color.Gray,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "iconColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { currentDestination = destination }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            painter = if (destination.iconVector != null) {
                                                androidx.compose.ui.graphics.vector.rememberVectorPainter(destination.iconVector)
                                            } else {
                                                painterResource(id = destination.iconRes!!)
                                            },
                                            contentDescription = destination.label,
                                            tint = iconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        
                                        // Animasi teks muncul halus
                                        AnimatedVisibility(
                                            visible = isSelected,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            Text(
                                                text = destination.label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = primaryColor,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFFF8FAF9)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                Crossfade(
                    targetState = currentDestination,
                    animationSpec = tween(durationMillis = 400),
                    label = "pageTransition"
                ) { destination ->
                    when (destination) {
                        AppDestinations.HOME -> Home(viewModel = viewModel)
                        AppDestinations.JURNAL -> HistoryScreen()
                        AppDestinations.YASIN -> YasinScreen(onBackClick = { currentDestination = AppDestinations.HOME })
                        AppDestinations.PROFILE -> ProfileScreen()
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val iconVector: ImageVector? = null,
    val iconRes: Int? = null,
) {
    HOME("Home", iconVector = Icons.Default.Home),
    JURNAL("Riwayat", iconVector = Icons.Default.DateRange),
    YASIN("Yasin", iconRes = R.drawable.book_open_svgrepo_com),
    PROFILE("Profil", iconVector = Icons.Default.Person),
}
