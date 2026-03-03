package com.example.absensijumat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.absensijumat.ui.auth.LoginScreen
import com.example.absensijumat.ui.home.Home
import com.example.absensijumat.ui.profile.ProfileScreen
import com.example.absensijumat.ui.theme.AbsensiJumatTheme
import com.example.absensijumat.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(this)
        enableEdgeToEdge()
        setContent {
            var startDestination by remember{
                mutableStateOf(if(sessionManager.fetchAuthToken() != null) "home" else "login")
            }
            AbsensiJumatTheme {
                if(startDestination == "home") {
                    AbsensiJumatApp()
                } else {
                    LoginScreen(onLoginSuccess = {token ->
                        startDestination = "home"
                    })
                }
            }
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
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
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
            AppDestinations.JURNAL -> PlaceholderScreen("Halaman Jurnal Riwayat")
            AppDestinations.PROFILE -> ProfileScreen()
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    JURNAL("Jurnal", Icons.Default.DateRange),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun PlaceholderScreen(name: String) {
    Scaffold { innerPadding ->
        Text(
            text = name,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}