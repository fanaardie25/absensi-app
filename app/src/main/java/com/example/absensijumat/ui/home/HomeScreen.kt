package com.example.absensijumat.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.absensijumat.BuildConfig
import com.example.absensijumat.MainActivity
import com.example.absensijumat.R
import com.example.absensijumat.ui.components.ErrorDialog
import com.example.absensijumat.ui.components.SuccessDialog
import com.example.absensijumat.ui.components.shimmerEffect
import com.example.absensijumat.ui.theme.AbsensiJumatTheme
import com.google.firebase.messaging.FirebaseMessaging


val ModernGreen = Color(0xFF00A36C)
val DarkEmerald = Color(0xFF006B3E)
val SoftGold = Color(0xFFFFD700)
val LightBg = Color(0xFFF8FAF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val userData = viewModel.userData
    val activityData = viewModel.activityData
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage
    val agendaName = userData?.agenda_name ?: "Kegiatan"
    val startTime = userData?.start_absensi?.take(5) ?: "00:00"
    val endTime = userData?.end_absensi?.take(5) ?: "00:00"

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var scheduleId by remember { mutableIntStateOf(0) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imageBitmap = bitmap
            scheduleId = userData?.schedule_id ?:   0

            viewModel.submitAttendance(
                context = context,
                scheduleId = scheduleId,
                bitmap = bitmap,
                latitude = latitude,
                longtitude = longitude
            ) {
                viewModel.successMessage = "Absen Berhasil!"
            }
        }
    }


    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false

        if (locationGranted && cameraGranted) {
            viewModel.getCurrentLocation(context) { lat, lon ->
                latitude = lat
                longitude = lon
                cameraLauncher.launch(null)
            }
        } else {
            Toast.makeText(context, "Izin Kamera & Lokasi wajib diberikan", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(userData) {
        userData?.let { user ->

            // 1. PENGAMANAN DATA (Cleaning String)
            // Kita buang spasi (kalau ada) dan jadikan huruf kecil semua biar formatnya seragam
            val classId = user.class_id.toString().replace(" ", "_").lowercase()
            val gender = user.gender.replace(" ", "_").lowercase()
            val religion = user.religion.replace(" ", "_").lowercase()

            val messaging = FirebaseMessaging.getInstance()

            // Level 1: Topic Umum (Contoh: "class_1")
            val topicLevel1 = "class_$classId"
            messaging.subscribeToTopic(topicLevel1).addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d("FCM_TOPIC", "Sukses subscribe: $topicLevel1")
                else Log.e("FCM_TOPIC", "Gagal subscribe: $topicLevel1")
            }

            // Level 2: Topic Gender (Contoh: "class_1_l")
            val topicLevel2 = "class_${classId}_$gender"
            messaging.subscribeToTopic(topicLevel2).addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d("FCM_TOPIC", "Sukses subscribe: $topicLevel2")
            }

            // Level 3: Topic Agama (Contoh: "class_1_l_islam")
            val topicLevel3 = "class_${classId}_${gender}_$religion"
            messaging.subscribeToTopic(topicLevel3).addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d("FCM_TOPIC", "Sukses subscribe: $topicLevel3")
            }

            val topicLevel4 = "class_${classId}_$religion"
            messaging.subscribeToTopic(topicLevel4).addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d("FCM_TOPIC", "Sukses subscribe: $topicLevel4")
                else Log.e("FCM_TOPIC", "Gagal subscribe: $topicLevel4")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getCurrentUser(context)
        viewModel.getLatestActivity(context)
    }

    // Modal Error Modern
    ErrorDialog(
        errorMessage = errorMessage,
        onDismiss = { viewModel.clearError() }
    )

    // Modal Success Modern
    SuccessDialog(
        message = successMessage,
        onDismiss = { 
            viewModel.clearSuccess()
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    )

    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = {
                    Column() {
                        Text(
                            "SMKN Tengaran",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = ModernGreen
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, ModernGreen.copy(alpha = 0.2f), CircleShape)
                            .clickable { /* Profile */ },
                        contentAlignment = Alignment.Center
                    ) {
                        val photoUrl = userData?.profile_photo_path
                        val placeholderPainter = rememberVectorPainter(Icons.Default.Person)

                        AsyncImage(
                            model = "${BuildConfig.BASE_STORAGE}${photoUrl}",
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = placeholderPainter,
                            placeholder = placeholderPainter,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBg)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                HomeSkeleton()
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
                ) {

                    item {
                        Column {
                            Text(
                                "Selamat datang",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            Text(
                                userData?.name ?: "User",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color(0xFF2D3436)
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    12.dp,
                                    RoundedCornerShape(28.dp),
                                    ambientColor = ModernGreen
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(ModernGreen, DarkEmerald)
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val hasSchedule = userData?.is_schedule_open == true
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (hasSchedule) "JADWAL ${agendaName.uppercase()}" else "STATUS HARI INI",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                val hasSchedule = userData?.is_schedule_open == true
                                val isDone = userData?.is_absent_today == true

                                Text(
                                    text = when {
                                        !hasSchedule -> "Tidak Ada Jadwal Hari Ini"
                                        isDone -> "Sudah Absen $agendaName"
                                        else -> "Ada Jadwal $agendaName, Yuk Absen!"
                                    },
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = when{
                                        !hasSchedule -> "Tidak Ada Kegiatan Buat Kamu Hari Ini"
                                        isDone -> "Mantap, kehadiranmu sudah tercatat!"
                                        else -> "Batas absen: $startTime - $endTime WIB"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val hasSchedule = userData?.is_schedule_open == true
                                val isDone = userData?.is_absent_today == true

                                val isButtonEnabled = hasSchedule && !isDone

                                val buttonMainColor = if (isButtonEnabled) ModernGreen else Color.Gray
                                val buttonDarkColor = if (isButtonEnabled) DarkEmerald else Color(0xFF757575)
                                val shadowColor = if (isButtonEnabled) ModernGreen else Color.Transparent

                                Surface(
                                    onClick = {
                                        if (isButtonEnabled) {
                                            // Cek apakah semua izin sudah dikasih
                                            val hasCamera = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                            val hasLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                                            if (hasCamera && hasLocation) {
                                                viewModel.getCurrentLocation(context) { lat, lon ->
                                                    latitude = lat
                                                    longitude = lon
                                                    cameraLauncher.launch(null)
                                                }
                                            } else {
                                                requestPermissionsLauncher.launch(
                                                    arrayOf(
                                                        android.Manifest.permission.CAMERA,
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            }
                                        } else {
                                            val message = if (!hasSchedule) "Tidak Ada Jadwal Hari Ini" else "Anda Sudah Melakukan Absen"
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(140.dp)
                                        .shadow(if (isButtonEnabled) 24.dp else 0.dp, CircleShape, spotColor = shadowColor),
                                    shape = CircleShape,
                                    color = Color.White,
                                    border = BorderStroke(8.dp, buttonMainColor.copy(alpha = 0.1f))
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(buttonMainColor, buttonDarkColor)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.camera_add_svgrepo_com),
                                                contentDescription = "Tap",
                                                tint = Color.White,
                                                modifier = Modifier.size(42.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = if (isDone) "KAMU SUDAH ABSEN" else if (!hasSchedule) "TIDAK ADA JADWAL KEGIATAN" else "TAP UNTUK MULAI ABSEN",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = buttonMainColor
                                )
                            }
                        }
                    }


                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ModernStatItem(
                                modifier = Modifier.weight(1f),
                                label = "Total Hadir",
                                value = userData?.stats?.hadir?.toString() ?: "0",
                                icon = Icons.Default.CheckCircle,
                                color = ModernGreen
                              )
                            ModernStatItem(
                                modifier = Modifier.weight(1f),
                                label = "Total Alpa",
                                value = userData?.stats?.tidak_hadir?.toString() ?: "0",
                                icon = Icons.Default.Warning,
                                color = Color(0xFFE74C3C)
                            )
                        }
                    }


                    item {
                        Text(
                            "Aktivitas Terakhir",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }

                    if (activityData != null) {
                        item {
                            ModernActivityItem(
                                date = activityData.date,
                                status = activityData.status
                            )
                        }
                    } else {
                        item {
                            Text("Belum ada aktivitas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // Welcome Text Skeleton
        item {
            Column {
                Box(modifier = Modifier.size(100.dp, 20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.size(200.dp, 32.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
        }

        // Card Skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .shimmerEffect()
            )
        }

        // Button Skeleton
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
        }

        // Stats Row Skeleton
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect())
                Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(24.dp)).shimmerEffect())
            }
        }

        // Activity Skeleton Header
        item {
            Box(modifier = Modifier.size(150.dp, 24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
        // Activity Item Skeleton
        items(2) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(20.dp)).shimmerEffect())
        }
    }
}


@Composable
fun ModernStatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                value, 
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF2D3436)
            )
            Text(
                label, 
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ModernActivityItem(date: String?, status: String) {
    // Menentukan teks, ikon, dan warna dasar berdasarkan status
    val (displayStatus, iconVector, itemColor) = when (status) {
        "hadir" -> Triple("Hadir", Icons.Default.CheckCircle, ModernGreen)
        "tidak_hadir" -> Triple("Alpa", Icons.Default.Close, Color(0xFFE74C3C)) // Merah
        "izin" -> Triple("Izin", Icons.Default.Info, Color(0xFFF39C12)) // Oranye
        "sakit" -> Triple("Sakit", Icons.Default.AddCircle, Color(0xFF3498DB)) // Biru (AddCircle menyerupai logo medis/kesehatan)
        else -> Triple("Status Tidak Diketahui", Icons.Default.Warning, Color.Gray)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    // Menggunakan warna utama dengan transparansi 15% untuk latar belakang
                    .background(color = itemColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = displayStatus,
                    tint = itemColor // Warna ikon mengikuti status
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = displayStatus,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = itemColor // Warna teks status mengikuti warna utama
                )
                Text(
                    text = date ?: "Tanggal tidak tersedia",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    AbsensiJumatTheme {
        Home()
    }
}
