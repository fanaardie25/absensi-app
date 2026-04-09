package com.example.absensijumat.ui.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.absensijumat.R
import com.example.absensijumat.ui.components.ErrorDialog
import com.example.absensijumat.ui.components.SuccessDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel = viewModel(),
    initialEmail: String = "",
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(initialEmail) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isEmailError = email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()

    val primaryEmerald = Color(0xFF10B981)
    val lightBg = Color(0xFFF8FAFC)

    ErrorDialog(
        errorMessage = viewModel.errorMessage,
        onDismiss = { viewModel.clearError() }
    )

    SuccessDialog(
        message = viewModel.successMessage,
        onDismiss = { 
            viewModel.clearSuccess()
            onSuccess()
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Lengkapi Data Akun", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = lightBg
                )
            )
        },
        containerColor = lightBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Keamanan & Profil",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Silakan perbarui email dan password Anda untuk melanjutkan",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Baru") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = primaryEmerald) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = isEmailError,

                supportingText = {
                    if (isEmailError) {
                        Text(
                            text = "Format email tidak valid",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Password Lama") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryEmerald) },
                trailingIcon = {
                    val image = if (oldPasswordVisible) painterResource(R.drawable.eye_svgrepo_com) else painterResource(R.drawable.eye_slash_svgrepo_com)
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(painter = image, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Password Baru") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryEmerald) },
                trailingIcon = {
                    val image = if (newPasswordVisible) painterResource(R.drawable.eye_svgrepo_com) else painterResource(R.drawable.eye_slash_svgrepo_com)
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(painter = image, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password Baru") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryEmerald) },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) painterResource(R.drawable.eye_svgrepo_com) else painterResource(R.drawable.eye_slash_svgrepo_com)
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(painter = image, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

            Button(
                onClick = {
                    viewModel.changePassword(context, oldPassword, newPassword, confirmPassword, email) {
                        // Success handled via SuccessDialog
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryEmerald),
                enabled = !viewModel.isLoading && 
                          oldPassword.isNotEmpty() && 
                          newPassword.isNotEmpty() && 
                          confirmPassword.isNotEmpty() &&
                          isEmailValid
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
