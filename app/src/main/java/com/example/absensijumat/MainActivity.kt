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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.absensijumat.ui.auth.LoginScreen
import com.example.absensijumat.ui.fakegps.FakeGpsScreen
import com.example.absensijumat.ui.history.HistoryScreen
import com.example.absensijumat.ui.home.Home
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

@PreviewScreenSizes
@Composable
fun AbsensiJumatApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val myItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF00796B),
            selectedTextColor = Color(0xFF00796B),
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.Transparent
        )
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        if (it.iconVector != null) {
                            Icon(
                                imageVector = it.iconVector,
                                contentDescription = it.label,
                            )
                        } else {
                           Icon(
                                painter = painterResource(id = it.iconRes!!),
                                contentDescription = it.label,
                           )
                        }
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                    colors = myItemColors
                )
            }
        },
        containerColor = Color.White,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = Color.White,
            navigationRailContainerColor = Color.White,
            navigationDrawerContainerColor = Color.White
        )
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> Home()
            AppDestinations.JURNAL -> HistoryScreen()
            AppDestinations.YASIN -> YasinScreen(onBackClick = { currentDestination = AppDestinations.HOME })
            AppDestinations.PROFILE -> ProfileScreen()
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
