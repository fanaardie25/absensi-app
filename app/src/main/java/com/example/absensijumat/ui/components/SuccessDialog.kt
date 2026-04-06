package com.example.absensijumat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    if (message.isNotEmpty()) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon Section
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = "Berhasil!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3436)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00A36C)
                        )
                    ) {
                        Text(
                            "Selesai",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
