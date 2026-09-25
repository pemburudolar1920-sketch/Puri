package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.RtViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: RtViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }
    var showChangePasswordSection by remember { mutableStateOf(false) }

    // Login Fields
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Register Fields
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regNama by remember { mutableStateOf("") }
    var regHp by remember { mutableStateOf("") }
    var regNik by remember { mutableStateOf("") }
    var regBlok by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf(UserRole.WARGA) }

    // Change Password Fields
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("auth_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Banner if logged in
        if (currentUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser?.namaLengkap ?: "",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${currentUser?.blokRumah} • NIK: ${currentUser?.noNik}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentUser?.role == UserRole.PENGURUS_RT) EmeraldPrimary else Color(0xFF0284C7)
                        ) {
                            Text(
                                text = if (currentUser?.role == UserRole.PENGURUS_RT) "PENGURUS RT" else "WARGA",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Nomor WhatsApp: ${currentUser?.noHp}", fontSize = 12.sp)
                    Text("Username Akun: ${currentUser?.username}", fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChangePasswordSection = !showChangePasswordSection },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ganti Password", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (currentUser?.role == UserRole.PENGURUS_RT) {
                                    viewModel.switchRole(UserRole.WARGA)
                                    Toast.makeText(context, "Beralih ke Akun Warga", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.switchRole(UserRole.PENGURUS_RT)
                                    Toast.makeText(context, "Beralih ke Akun Pengurus RT", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentUser?.role == UserRole.PENGURUS_RT) "Ke Mode Warga" else "Ke Pengurus RT",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Ganti Password Section
            AnimatedVisibility(visible = showChangePasswordSection) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ganti Password Akun",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Password Lama") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Password Baru") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Konfirmasi Password Baru") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (newPassword.isBlank()) {
                                    Toast.makeText(context, "Password baru tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPassword != confirmNewPassword) {
                                    Toast.makeText(context, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                coroutineScope.launch {
                                    val success = viewModel.changePassword(oldPassword, newPassword)
                                    if (success) {
                                        Toast.makeText(context, "Password berhasil diperbarui!", Toast.LENGTH_LONG).show()
                                        oldPassword = ""
                                        newPassword = ""
                                        confirmNewPassword = ""
                                        showChangePasswordSection = false
                                    } else {
                                        Toast.makeText(context, "Password lama salah!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SIMPAN PASSWORD BARU")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Login / Registrasi Akun Khusus Warga dan Pengurus
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isRegisterMode) "Registrasi Akun Baru RT" else "Masuk Akun RT Puri Pratama",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRegisterMode) "Pendaftaran khusus Warga dan Pengurus RT Puri Pratama" else "Login menggunakan username / NIK dan password",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!isRegisterMode) {
                    // LOGIN FORM
                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = { loginUsername = it },
                        label = { Text("Username atau NIK") },
                        placeholder = { Text("admin / warga / NIK") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val ok = viewModel.login(loginUsername, loginPassword)
                                if (ok) {
                                    Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                    loginUsername = ""
                                    loginPassword = ""
                                } else {
                                    Toast.makeText(context, "Username atau Password salah!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("MASUK SEBAGAI WARGA / PENGURUS", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Demo Login Buttons
                    Text(
                        text = "AKSES CEPAT PERCONTOHAN:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.switchRole(UserRole.PENGURUS_RT)
                                Toast.makeText(context, "Masuk sebagai Pengurus RT (Admin)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Demo Pengurus RT", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.switchRole(UserRole.WARGA)
                                Toast.makeText(context, "Masuk sebagai Warga RT", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Demo Warga RT", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { isRegisterMode = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Belum punya akun? Daftar Warga Baru di sini")
                    }
                } else {
                    // REGISTER FORM
                    Text("Pilih Peran Akun:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = regRole == UserRole.WARGA,
                            onClick = { regRole = UserRole.WARGA },
                            label = { Text("Warga Lingkungan") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = regRole == UserRole.PENGURUS_RT,
                            onClick = { regRole = UserRole.PENGURUS_RT },
                            label = { Text("Pengurus RT") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regNama,
                        onValueChange = { regNama = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = regUsername,
                        onValueChange = { regUsername = it },
                        label = { Text("Username Akun") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regNik,
                            onValueChange = { if (it.length <= 16) regNik = it },
                            label = { Text("NIK (16 Digit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = regHp,
                            onValueChange = { regHp = it },
                            label = { Text("No. HP (WA)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = regBlok,
                        onValueChange = { regBlok = it },
                        label = { Text("Blok & No Rumah (misal: Blok B1 No. 04)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Buat Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (regNama.isNotBlank() && regUsername.isNotBlank() && regPassword.isNotBlank()) {
                                coroutineScope.launch {
                                    val ok = viewModel.register(
                                        username = regUsername,
                                        pass = regPassword,
                                        nama = regNama,
                                        hp = regHp,
                                        nik = regNik,
                                        blok = regBlok,
                                        role = regRole
                                    )
                                    if (ok) {
                                        Toast.makeText(context, "Registrasi berhasil! Anda telah masuk.", Toast.LENGTH_LONG).show()
                                        isRegisterMode = false
                                    } else {
                                        Toast.makeText(context, "Gagal mendaftar, coba username lain.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Mohon lengkapi data", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("DAFTAR & MASUK SEKARANG", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { isRegisterMode = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Sudah punya akun? Masuk di sini")
                    }
                }
            }
        }
    }
}
