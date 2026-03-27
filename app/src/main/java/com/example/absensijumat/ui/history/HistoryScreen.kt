package com.example.absensijumat.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.absensijumat.BuildConfig
import com.example.absensijumat.response.AttendanceDataAll
import com.example.absensijumat.ui.theme.AbsensiJumatTheme

// Warna konsisten dengan Home
val ModernGreen = Color(0xFF00A36C)
val LightBg = Color(0xFFF8FAF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(modifier: Modifier = Modifier, viewModel: HistoryViewModel = viewModel()) {

    val context = LocalContext.current
    val activityData = viewModel.activityList
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.getAllActivity(context)
    }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "RIWAYAT ABSENSI",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = ModernGreen
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LightBg)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ModernGreen
                )
            } else if (errorMessage.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = errorMessage, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.getAllActivity(context) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Coba Lagi")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {

                    Text(
                        "Semua Kehadiran",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (activityData.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat absensi", color = Color.Gray)
                        }
                    } else {
                        // List Riwayat
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(activityData) { data ->
                                HistoryItem(data)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(data: AttendanceDataAll) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo
            AsyncImage(
                model = "${BuildConfig.BASE_STORAGE}${data.photo_path}",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Masjid SMKN Tengaran",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2D3436)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        data.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Badge Status
            Surface(
                color = when (data.status) {
                    "hadir" -> ModernGreen.copy(alpha = 0.1f)
                    "tidak_hadir" -> Color.Red.copy(alpha = 0.1f)
                    "izin" -> Color.Yellow.copy(alpha = 0.1f)
                    else -> Color.Gray.copy(alpha = 0.1f)
                },
                shape = CircleShape
            ) {
                Text(
                    text = when (data.status) {
                        "hadir" -> "Hadir"
                        "tidak_hadir" -> "Tidak Hadir"
                        "izin" -> "Izin"
                        else -> "Status Tidak Diketahui"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (data.status) {
                        "hadir" -> ModernGreen
                        "tidak_hadir" -> Color.Red
                        "izin" -> Color(0xFFFFA000)
                        else -> Color.Gray
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    AbsensiJumatTheme {
        HistoryScreen()
    }
}
