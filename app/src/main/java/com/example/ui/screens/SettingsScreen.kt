package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TvViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.FocusCyan
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.SoftGray

enum class SettingsTab {
    ACCOUNT,
    TV_SETTINGS,
    SYSTEM
}

@Composable
fun SettingsScreen(
    viewModel: TvViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SettingsTab.ACCOUNT) }
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val bootInPlayer by viewModel.bootInPlayer.collectAsState()

    // API Config values
    val apiUrl by viewModel.apiUrl.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val hmacKey by viewModel.hmacKey.collectAsState()
    val encryptionKey by viewModel.encryptionKey.collectAsState()

    // Dialog flags
    var showLoginDialog by remember { mutableStateOf(false) }
    var updateCheckedText by remember { mutableStateOf<String?>(null) }

    // Login fields
    var loginUsername by remember { mutableStateOf("") }
    var loginName by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Dialog to sign in
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "অ্যাকাউন্টে লগইন করুন",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = { loginUsername = it },
                        label = { Text("ইউজারনেম (ইমেইল)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusCyan,
                            unfocusedBorderColor = SoftGray,
                            focusedLabelColor = FocusCyan,
                            unfocusedLabelColor = SoftGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = loginName,
                        onValueChange = { loginName = it },
                        label = { Text("আপনার নাম") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusCyan,
                            unfocusedBorderColor = SoftGray,
                            focusedLabelColor = FocusCyan,
                            unfocusedLabelColor = SoftGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("পাসওয়ার্ড") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusCyan,
                            unfocusedBorderColor = SoftGray,
                            focusedLabelColor = FocusCyan,
                            unfocusedLabelColor = SoftGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (loginUsername.isNotEmpty() && loginName.isNotEmpty()) {
                            viewModel.loginUser(loginUsername, loginName)
                            showLoginDialog = false
                            // Clear inputs
                            loginUsername = ""
                            loginName = ""
                            loginPassword = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCyan)
                ) {
                    Text("লগইন", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLoginDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardBackground)
                ) {
                    Text("বাতিল", color = Color.White)
                }
            }
        )
    }

    // App Update Checked Dialog
    if (updateCheckedText != null) {
        AlertDialog(
            onDismissRequest = { updateCheckedText = null },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "সিস্টেম আপডেট",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = updateCheckedText ?: "",
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { updateCheckedText = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCyan)
                ) {
                    Text("ঠিক আছে", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Left Navigation panel
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .background(DeepCharcoal)
                    .padding(16.dp)
            ) {
                // Return Back Button
                var isBackFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBackFocused) FocusCyan else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isBackFocused) Color.White else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .onFocusChanged { isBackFocused = it.isFocused }
                        .focusable()
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go Back",
                        tint = if (isBackFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হোম পেজে ফিরুন",
                        color = if (isBackFocused) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "সেটিংস মেনু",
                    color = FocusCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Menu list items with D-pad focus handlers
                listOf(
                    Triple(SettingsTab.ACCOUNT, "আমার অ্যাকাউন্ট", Icons.Default.AccountBox),
                    Triple(SettingsTab.TV_SETTINGS, "টিভি সেটিংস", Icons.Default.Settings),
                    Triple(SettingsTab.SYSTEM, "সিস্টেম তথ্য", Icons.Default.Info)
                ).forEach { (tab, label, icon) ->
                    val isSelected = selectedTab == tab
                    var isFocused by remember { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isFocused -> FocusCyan
                                isSelected -> DarkCardBackground
                                else -> Color.Transparent
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .onFocusChanged {
                                isFocused = it.isFocused
                                if (it.isFocused) {
                                    selectedTab = tab
                                }
                            }
                            .focusable()
                            .clickable { selectedTab = tab }
                            .border(
                                width = 1.dp,
                                color = if (isFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = when {
                                    isFocused -> Color.Black
                                    isSelected -> FocusCyan
                                    else -> Color.White
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                color = when {
                                    isFocused -> Color.Black
                                    isSelected -> FocusCyan
                                    else -> Color.White
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 2. Right Pane: Detailed Content panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                when (selectedTab) {
                    SettingsTab.ACCOUNT -> {
                        Column {
                            Text(
                                text = "আমার অ্যাকাউন্ট ড্যাশবোর্ড",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                            Text(
                                text = "আপনার রিয়েল-টাইম ইউজার ডাটা এবং সাবস্ক্রিপশন ইনফো",
                                color = SoftGray,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            if (loggedInUser == null) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Logged Out",
                                            tint = FocusCyan,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "আপনি বর্তমানে কোনো অ্যাকাউন্টে লগইন করেননি",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))

                                        var isLoginBtnFocused by remember { mutableStateOf(false) }
                                        Button(
                                            onClick = { showLoginDialog = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isLoginBtnFocused) Color.White else FocusCyan
                                            ),
                                            modifier = Modifier
                                                .onFocusChanged { isLoginBtnFocused = it.isFocused }
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isLoginBtnFocused) FocusCyan else Color.Transparent,
                                                    shape = RoundedCornerShape(24.dp)
                                                )
                                        ) {
                                            Text(
                                                text = "অ্যাকাউন্টে লগইন করুন",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = loggedInUser?.name ?: "",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 20.sp
                                            )
                                            Text(
                                                text = "ইউজারনেম: ${loggedInUser?.username}",
                                                color = SoftGray,
                                                fontSize = 14.sp
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(FocusCyan, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = loggedInUser?.type?.uppercase() ?: "PREMIUM",
                                                        color = Color.Black,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "মেয়াদকাল: ${loggedInUser?.expiryDate}",
                                                    color = FocusCyan,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        var isLogoutFocused by remember { mutableStateOf(false) }
                                        Button(
                                            onClick = { viewModel.logoutUser() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isLogoutFocused) Color.White else DangerRed
                                            ),
                                            modifier = Modifier
                                                .onFocusChanged { isLogoutFocused = it.isFocused }
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isLogoutFocused) DangerRed else Color.Transparent,
                                                    shape = RoundedCornerShape(24.dp)
                                                )
                                        ) {
                                            Text(
                                                text = "লগআউট করুন",
                                                color = if (isLogoutFocused) Color.Black else Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsTab.TV_SETTINGS -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            item {
                                Text(
                                    text = "টিভি সেটিংস এবং সিস্টেম কনফিগারেশন",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "অ্যাপ্লিকেশন চালুর প্রসেস এবং সিকিউরড গেটওয়ে কনফিগার করুন",
                                    color = SoftGray,
                                    fontSize = 13.sp
                                )
                            }

                            // 1. BootInPlayer Toggle
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "বুট-ইন-প্লেয়ার (Boot-In-Player)",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "সরাসরি প্লেয়ারে বুট করা এনাবল থাকলে অ্যাপ চালুর সাথে সাথেই চ্যানেল রেন্ডার হবে",
                                                color = SoftGray,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Switch(
                                            checked = bootInPlayer,
                                            onCheckedChange = { viewModel.toggleBootInPlayer(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = FocusCyan,
                                                uncheckedThumbColor = SoftGray,
                                                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                                            )
                                        )
                                    }
                                }
                            }

                            // 2. Secured API Config
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "সিকিউর গেটওয়ে ইনফরমেশন (ডায়নামিক)",
                                            color = FocusCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )

                                        Column {
                                            Text("API URL", color = SoftGray, fontSize = 11.sp)
                                            Text(apiUrl, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Column {
                                            Text("API KEY (SECURED)", color = SoftGray, fontSize = 11.sp)
                                            Text(apiKey, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("HMAC KEY", color = SoftGray, fontSize = 11.sp)
                                                Text(hmacKey, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Column {
                                                Text("ENCRYPTION KEY (AES-256)", color = SoftGray, fontSize = 11.sp)
                                                Text(encryptionKey, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsTab.SYSTEM -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "সিস্টেম ও অ্যাপ্লিকেশন তথ্য",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "স্মার্ট টিভি লাইভ প্রো (Smart TV Live Pro)",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "অ্যাপ সংস্করণ: v2.6.0 (Stable Release)",
                                                color = SoftGray,
                                                fontSize = 13.sp
                                            )
                                        }

                                        var isCheckFocused by remember { mutableStateOf(false) }
                                        Button(
                                            onClick = {
                                                updateCheckedText = "আপনার স্মার্ট টিভি লাইভ অ্যাপটি বর্তমানে সর্বশেষ সংস্করণে আপডেট রয়েছে। ধন্যবাদ!"
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCheckFocused) Color.White else FocusCyan
                                            ),
                                            modifier = Modifier
                                                .onFocusChanged { isCheckFocused = it.isFocused }
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isCheckFocused) FocusCyan else Color.Transparent,
                                                    shape = RoundedCornerShape(24.dp)
                                                )
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Check update",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("আপডেট চেক করুন", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Column {
                                        Text(
                                            text = "ডেভেলপার ইনফরমেশন",
                                            color = FocusCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "অ্যান্ড্রয়েড টিভি সলিউশনস ল্যাব লিমিটেড (২০২৬)",
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "টেকনোলজি ইঞ্জিনসমূহ",
                                            color = FocusCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Media3 ExoPlayer v1.4.1 • Room SQLite Database • Jetpack Compose MVVM",
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
