package com.example.absensijumat.ui.yasin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.absensijumat.R
import com.example.absensijumat.response.Ayat
import com.example.absensijumat.ui.home.DarkEmerald
import com.example.absensijumat.ui.home.ModernGreen
import com.example.absensijumat.ui.theme.AbsensiJumatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YasinScreen(
    modifier: Modifier = Modifier,
    viewModel: YasinViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val yasin = viewModel.yasinData
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val isPlaying = viewModel.isPlaying
    val currentAyat = viewModel.currentlyPlayingAyat

    LaunchedEffect(Unit) {
        viewModel.fetchYasin()
    }

    Scaffold(
        containerColor = Color(0xFFF8FAF9),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "SURAH YASIN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color(0xFF2D3436)
                        )
                        if (yasin != null) {
                            Text(
                                "36 • ${yasin.namaLatin} • ${yasin.jumlahAyat} Ayat",
                                style = MaterialTheme.typography.labelSmall,
                                color = ModernGreen.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopAudio()
                        onBackClick()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color(0xFF2D3436)
                        )
                    }
                },
                actions = {
                    if (yasin != null) {
                        val audioUrl = yasin.audioFull["06"] ?: yasin.audioFull.values.firstOrNull()
                        if (audioUrl != null) {
                            FilledIconButton(
                                onClick = { viewModel.playAudio(audioUrl, 0) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isPlaying && currentAyat == 0) Color.Red.copy(alpha = 0.1f) else ModernGreen.copy(alpha = 0.1f),
                                    contentColor = if (isPlaying && currentAyat == 0) Color.Red else ModernGreen
                                ),
                                modifier = Modifier.padding(end = 8.dp).size(40.dp)
                            ) {
                                val playPainter = if (isPlaying && currentAyat == 0) {
                                    painterResource(id = R.drawable.pause_svgrepo_com)
                                } else {
                                    rememberVectorPainter(Icons.Default.PlayArrow)
                                }
                                val iconTint = if (isPlaying && currentAyat == 0) {
                                   ModernGreen
                                } else {
                                    ModernGreen
                                }
                                Icon(
                                    painter = playPainter,
                                    contentDescription = "Play Full",
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = ModernGreen, strokeWidth = 3.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("Menyiapkan ayat...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else if (errorMessage.isNotEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage, color = Color.Red, textAlign = TextAlign.Center)
                    Button(
                        onClick = { viewModel.fetchYasin() },
                        modifier = Modifier.padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Coba Lagi")
                    }
                }
            } else if (yasin != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        SurahHeroSection(yasin.nama, yasin.arti)
                    }

                    items(yasin.ayat, key = { it.nomorAyat }) { ayat ->
                        AyatCard(
                            ayat = ayat,
                            isPlaying = isPlaying && currentAyat == ayat.nomorAyat,
                            onPlayClick = {
                                val audioUrl = ayat.audio["01"] ?: ayat.audio.values.firstOrNull()
                                if (audioUrl != null) {
                                    viewModel.playAudio(audioUrl, ayat.nomorAyat)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurahHeroSection(nama: String, arti: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(180.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = ModernGreen)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(colors = listOf(ModernGreen, DarkEmerald))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                nama,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                arti,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AyatCard(
    ayat: Ayat,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val highlightColor by animateColorAsState(
        targetValue = if (isPlaying) ModernGreen.copy(alpha = 0.05f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow), label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlightColor)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ayat Number with Modern Badge
            Surface(
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(10.dp),
                color = ModernGreen.copy(alpha = 0.1f),
                contentColor = ModernGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        ayat.nomorAyat.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }

            // Audio Play/Stop Button
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(32.dp)
            ) {
                if (isPlaying) {
                    Icon(
                        painter = painterResource(id = R.drawable.pause_svgrepo_com),
                        contentDescription = "Stop",
                        tint = ModernGreen,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = ModernGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Arabic text - High contrast and elegant
        Text(
            text = ayat.teksArab,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 30.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF2D3436),
            textAlign = TextAlign.Right
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Latin Transliteration
        Text(
            text = ayat.teksLatin,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            ),
            color = ModernGreen
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Translation
        Text(
            text = ayat.teksIndonesia,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                color = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
    }
}

@Preview(showBackground = true)
@Composable
private fun YasinPreview() {
    AbsensiJumatTheme {
        YasinScreen()
    }
}
