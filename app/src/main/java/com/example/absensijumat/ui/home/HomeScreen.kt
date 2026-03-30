package com.example.absensijumat.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.absensijumat.BuildConfig
import com.example.absensijumat.MainActivity
import com.example.absensijumat.R
import com.example.absensijumat.ui.components.ErrorDialog
import com.example.absensijumat.ui.theme.AbsensiJumatTheme
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File


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

    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var scheduleId by remember { mutableIntStateOf(0) }
    
    var showPermissionDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            scheduleId = userData?.schedule_id ?:   0

            viewModel.submitAttendance(
                context = context,
                scheduleId = scheduleId,
                bitmap = bitmap,
                latitude = latitude,
                longtitude = longitude
            ) {
                Toast.makeText(context, "Absen Berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
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
            val classId = user.class_id
            val topicName = "class_$classId"

            FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FCM_HOME", "Berhasil subscribe ke: $topicName")
                    } else {
                        Log.e("FCM_HOME", "Gagal subscribe topik")
                    }
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

    if (showPermissionDialog) {
        PermissionDialog(
            isLoading = isLoading,
            onDismiss = { if (!isLoading) showPermissionDialog = false },
            onSubmit = { status, desc, file ->
                val sId = userData?.schedule_id ?: 0
                viewModel.submitPermission(context, sId, status, desc, file) {
                    showPermissionDialog = false
                    Toast.makeText(context, "Pengajuan $status Berhasil dikirim!", Toast.LENGTH_SHORT).show()
                    // Refresh data home
                    viewModel.getCurrentUser(context)
                    viewModel.getLatestActivity(context)
                }
            }
        )
    }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
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
            if (isLoading && !showPermissionDialog) { // Jangan tampilkan loader tengah kalau sedang di dialog izin
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ModernGreen
                )
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
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "STATUS HARI INI",
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
                                        isDone -> "Kamu Dapat Jadwal & Sudah Absen"
                                        else -> "Kamu Dapat Jadwal, Yuk Absen Sekarang!"
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
                                        !hasSchedule -> "Hari ini kamu tidak bisa absen"
                                        isDone -> "jangan sampai terlambat ya"
                                        else -> "jangan sampai lupa absen ya"
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
                                val isInactive = !hasSchedule || isDone

                                Surface(
                                    onClick = {
                                        if (hasSchedule && !isDone) {
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
                                            Toast.makeText(context, "Kamu Tidak Dapat Absen Hari Ini", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(140.dp)
                                        .shadow(24.dp, CircleShape, spotColor = if (isInactive) Color.Gray else ModernGreen),
                                    shape = CircleShape,
                                    color = Color.White,
                                    border = BorderStroke(8.dp, (if (isInactive) Color.Gray else ModernGreen).copy(alpha = 0.1f))
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
                                                        colors = if (isInactive) listOf(Color.Gray, Color.DarkGray) else listOf(ModernGreen, DarkEmerald)
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
                                    if (isDone) "KAMU SUDAH ABSEN" else if (!hasSchedule) "TIDAK ADA JADWAL" else "TAP UNTUK MULAI ABSEN",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = if (isInactive) Color.Gray else ModernGreen
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Tombol Izin / Sakit
                                OutlinedButton(
                                    onClick = { 
                                        if (hasSchedule && !isDone) {
                                            showPermissionDialog = true 
                                        } else {
                                            Toast.makeText(context, "Tidak ada jadwal aktif", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isInactive) Color.Gray else ModernGreen),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isInactive) Color.Gray else ModernGreen)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ajukan Izin / Sakit", fontWeight = FontWeight.Bold)
                                }
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
fun PermissionDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (status: String, description: String, file: File) -> Unit
) {
    val context = LocalContext.current
    var selectedStatus by remember { mutableStateOf("sakit") }
    var description by remember { mutableStateOf("") }
    
    var photoFile by remember { mutableStateOf<File?>(null) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            showPreview = true
        } else {
            showPreview = false
            photoFile = null
            capturedUri = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pengajuan Izin / Sakit",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Toggle Sakit / Izin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F2F6))
                        .padding(4.dp)
                ) {
                    listOf("sakit", "izin").forEach { status ->
                        val isSelected = selectedStatus == status
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ModernGreen else Color.Transparent)
                                .clickable(enabled = !isLoading) { selectedStatus = status }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = status.uppercase(),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Photo Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F2F6))
                        .clickable(enabled = !isLoading) { 
                            val file = File(context.cacheDir, "temp_permission_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            photoFile = file
                            capturedUri = uri
                            showPreview = false
                            cameraLauncher.launch(uri)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (showPreview && photoFile != null) {
                        AsyncImage(
                            model = photoFile,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painterResource(R.drawable.camera_add_svgrepo_com), contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.Gray)
                            Text("Ambil Foto Bukti", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (description.isNotEmpty() && photoFile != null) {
                            onSubmit(selectedStatus, description, photoFile!!)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ModernGreen),
                    enabled = !isLoading && description.isNotEmpty() && showPreview && photoFile != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Kirim Pengajuan", fontWeight = FontWeight.Bold)
                    }
                }
            }
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
fun ModernActivityItem(date: String?,status: String) {
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
                    .background(ModernGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(status) {
                    "hadir" -> Icons.Default.CheckCircle
                    "sakit", "izin" -> Icons.Default.Email
                    else -> Icons.Default.Warning
                }
                Icon(icon, contentDescription = null, tint = ModernGreen)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val displayStatus = when (status) {
                    "hadir" -> "Hadir"
                    "tidak_hadir" -> "Tidak Hadir"
                    "izin" -> "Izin"
                    "sakit" -> "Sakit"
                    else -> "Status Tidak Diketahui"
                }
                Text(
                    displayStatus,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ModernGreen
                )
                Text(
                    "Jumat, ${date ?: "Tanggal tidak tersedia"}",
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
