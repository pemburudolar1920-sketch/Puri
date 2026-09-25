package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ForumPost
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.RtViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    viewModel: RtViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val posts by viewModel.forumPosts.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("SEMUA") }

    val filteredPosts = if (selectedCategoryFilter == "SEMUA") {
        posts
    } else {
        posts.filter { it.kategori == selectedCategoryFilter }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PostAdd, contentDescription = null) },
                text = { Text("Tulis Saran / Masukan", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_forum_post_fab")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("forum_screen")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Forum Musyawarah & Saran RT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ruang terbuka musyawarah, penyampaian aspirasi, ide fasilitas baru, dan masukan warga RT Puri Pratama.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == "SEMUA",
                    onClick = { selectedCategoryFilter = "SEMUA" },
                    label = { Text("Semua (${posts.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == "Saran & Masukan",
                    onClick = { selectedCategoryFilter = "Saran & Masukan" },
                    label = { Text("Saran", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == "Keamanan",
                    onClick = { selectedCategoryFilter = "Keamanan" },
                    label = { Text("Keamanan", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategoryFilter == "Fasilitas Umum",
                    onClick = { selectedCategoryFilter = "Fasilitas Umum" },
                    label = { Text("Fasum", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada saran masukan di kategori ini. Jadilah yang pertama menulis!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        ForumPostCard(
                            post = post,
                            currentUserNama = currentUser?.namaLengkap ?: "Warga RT",
                            currentUserRole = if (currentUser?.role == UserRole.PENGURUS_RT) "Pengurus RT" else (currentUser?.blokRumah ?: "Warga"),
                            onUpvote = { viewModel.upvoteForumPost(post.id) },
                            onAddComment = { replyText ->
                                val role = if (currentUser?.role == UserRole.PENGURUS_RT) "Pengurus RT" else (currentUser?.blokRumah ?: "Warga")
                                viewModel.addForumComment(
                                    post = post,
                                    nama = currentUser?.namaLengkap ?: "Warga",
                                    role = role,
                                    komentar = replyText
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateForumPostDialog(
            writerName = currentUser?.namaLengkap ?: "Warga RT",
            writerRole = if (currentUser?.role == UserRole.PENGURUS_RT) "Pengurus RT" else (currentUser?.blokRumah ?: "Warga"),
            blokRumah = currentUser?.blokRumah ?: "",
            onSubmit = { newPost ->
                viewModel.createForumPost(newPost)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost,
    currentUserNama: String,
    currentUserRole: String,
    onUpvote: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var isCommentsExpanded by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }
    val comments = post.parseComments()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category tag & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = post.kategori,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = post.tanggal,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Title & Body
            Text(
                text = post.judul,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.isi,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Author Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${post.penulisNama} (${post.penulisRole})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Upvote & Toggle comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upvote button
                OutlinedButton(
                    onClick = onUpvote,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Dukung", tint = EmeraldPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${post.upvotes} Dukungan", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }

                // Comment toggle button
                TextButton(
                    onClick = { isCommentsExpanded = !isCommentsExpanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ModeComment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${comments.size} Tanggapan", fontSize = 12.sp)
                }
            }

            // Expanded comments section
            AnimatedVisibility(visible = isCommentsExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (comments.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            comments.forEach { c ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${c.nama} (${c.role})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (c.role.contains("RT", ignoreCase = true)) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(text = c.waktu, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(text = c.komentar, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Comment input row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Tulis tanggapan atau saran...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (commentInput.isNotBlank()) {
                                    onAddComment(commentInput.trim())
                                    commentInput = ""
                                }
                            },
                            enabled = commentInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Kirim", tint = EmeraldPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateForumPostDialog(
    writerName: String,
    writerRole: String,
    blokRumah: String,
    onSubmit: (ForumPost) -> Unit,
    onDismiss: () -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var isi by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Saran & Masukan") }

    val categories = listOf("Saran & Masukan", "Fasilitas Umum", "Keamanan", "Usulan Kegiatan", "Musyawarah")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Tulis Saran & Masukan Warga",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Topik / Judul Usulan") },
                    placeholder = { Text("misal: Pengadaan tenda dan kursi RT") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = isi,
                    onValueChange = { isi = it },
                    label = { Text("Isi Saran / Aspirasi Detail") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Column {
                    categories.chunked(2).forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowCats.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (judul.isNotBlank() && isi.isNotBlank()) {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                onSubmit(
                                    ForumPost(
                                        judul = judul.trim(),
                                        isi = isi.trim(),
                                        kategori = selectedCategory,
                                        penulisNama = writerName,
                                        penulisRole = writerRole,
                                        blokRumah = blokRumah,
                                        tanggal = today,
                                        upvotes = 1
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Terbitkan Topik")
                    }
                }
            }
        }
    }
}
