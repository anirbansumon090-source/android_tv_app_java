package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TvViewModel
import com.example.ui.components.VideoPlayer
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.FocusCyan
import com.example.ui.theme.SoftGray
import com.example.ui.theme.TranslucentBlack
import com.example.ui.theme.TranslucentDark

@Composable
fun PlayerScreen(
    viewModel: TvViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentChannel by viewModel.currentPlayingChannel.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val bootInPlayer by viewModel.bootInPlayer.collectAsState()
    val typedNumber by viewModel.typedNumber.collectAsState()
    val showChannelOverlay by viewModel.showChannelOverlay.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Simulated player configurations
    var selectedAudioTrack by remember { mutableStateOf("বাংলা (Primary)") }
    var selectedQuality by remember { mutableStateOf("Auto (1080p)") }

    val focusRequester = remember { FocusRequester() }

    // Request focus on creation to listen to D-pad key events
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        // Auto trigger overlay initially to welcome the user
        viewModel.showOverlayForChannel()
    }

    // Capture exit logic:
    // If BootInPlayer is enabled -> show exit confirmation dialog.
    // If BootInPlayer is disabled -> return to HomeScreen.
    BackHandler {
        if (bootInPlayer) {
            showExitDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Exit confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "অ্যাপ বন্ধ করুন",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "আপনি কি সত্যিই প্লেয়ার বন্ধ করে স্মার্ট টিভি লাইভ অ্যাপ থেকে বের হতে চান?",
                    color = SoftGray,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCyan)
                ) {
                    Text("হ্যাঁ, বন্ধ করুন", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardBackground)
                ) {
                    Text("না", color = Color.White)
                }
            }
        )
    }

    // TV Player Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = DeepCharcoal,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = FocusCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "প্লেয়ার সেটিংস",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Audio Track Option
                    Column {
                        Text(text = "অডিও ল্যাঙ্গুয়েজ সিলেক্ট করুন", color = SoftGray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("বাংলা (Primary)", "English (Stereo)").forEach { track ->
                                val isSel = track == selectedAudioTrack
                                var isFoc by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isFoc) FocusCyan else if (isSel) DarkCardBackground else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isFoc) Color.White else if (isSel) FocusCyan else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .onFocusChanged { isFoc = it.isFocused }
                                        .focusable()
                                        .clickable { selectedAudioTrack = track }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = if (isFoc) Color.Black else FocusCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = track,
                                            color = if (isFoc) Color.Black else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Video Quality
                    Column {
                        Text(text = "ভিডিও স্ট্রিম কোয়ালিটি", color = SoftGray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Auto (1080p)", "HD (720p)", "SD (480p)").forEach { qual ->
                                val isSel = qual == selectedQuality
                                var isFoc by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isFoc) FocusCyan else if (isSel) DarkCardBackground else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isFoc) Color.White else if (isSel) FocusCyan else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .onFocusChanged { isFoc = it.isFocused }
                                        .focusable()
                                        .clickable { selectedQuality = qual }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = if (isFoc) Color.Black else FocusCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = qual,
                                            color = if (isFoc) Color.Black else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Boot in Player Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCardBackground.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "বুট-ইন-প্লেয়ার (Boot-In-Player)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "এনাবল থাকলে অ্যাপ চালুর সাথে সাথে চ্যানেল চালু হবে",
                                color = SoftGray,
                                fontSize = 11.sp
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
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCyan)
                ) {
                    Text("সেটিংস বন্ধ করুন", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val keyCode = keyEvent.nativeKeyEvent.keyCode
                    
                    // 1. D-pad Up/Down Arrow switches channels
                    if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP) {
                        viewModel.nextChannel()
                        viewModel.showOverlayForChannel()
                        return@onKeyEvent true
                    }
                    if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) {
                        viewModel.prevChannel()
                        viewModel.showOverlayForChannel()
                        return@onKeyEvent true
                    }

                    // 2. OK/DPAD Center or Enter button shows overlay channel list
                    if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER || keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                        viewModel.toggleChannelOverlay(!showChannelOverlay)
                        return@onKeyEvent true
                    }

                    // 3. Remote Number keys: directly switches channels
                    if (keyCode >= android.view.KeyEvent.KEYCODE_0 && keyCode <= android.view.KeyEvent.KEYCODE_9) {
                        val char = (keyCode - android.view.KeyEvent.KEYCODE_0).toString().first()
                        viewModel.appendChannelNumberDigit(char)
                        return@onKeyEvent true
                    }
                }
                false
            }
    ) {
        // Video engine
        currentChannel?.let { channel ->
            VideoPlayer(videoUrl = channel.url)
        }

        // Tap/click backdrop to toggle list overlay manually (supporting mouse/touch screens too!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    viewModel.toggleChannelOverlay(!showChannelOverlay)
                }
        )

        // Floating Number Input Overlay
        if (typedNumber.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .background(TranslucentDark, RoundedCornerShape(16.dp))
                    .border(3.dp, FocusCyan, RoundedCornerShape(16.dp))
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "চ্যানেল নম্বর চাপুন",
                        color = SoftGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = typedNumber,
                        color = FocusCyan,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }
            }
        }

        // Left Pane Channel List & Action Overlay
        AnimatedVisibility(
            visible = showChannelOverlay,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(TranslucentDark, Color.Transparent),
                            endX = 1100f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .background(DeepCharcoal.copy(alpha = 0.95f))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    // Header inside overlay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Channels list",
                                tint = FocusCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "চ্যানেল তালিকা",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Close overlay button
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { viewModel.toggleChannelOverlay(false) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Channels list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(channels) { channel ->
                            val isPlaying = channel.id == currentChannel?.id
                            var isFocused by remember { mutableStateOf(false) }

                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isFocused -> FocusCyan
                                        isPlaying -> DarkCardBackground
                                        else -> Color.White.copy(alpha = 0.03f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .focusable()
                                    .clickable {
                                        viewModel.selectChannel(channel)
                                        viewModel.showOverlayForChannel()
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = if (isFocused) Color.White else if (isPlaying) FocusCyan else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isFocused) Color.Black else FocusCyan.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = channel.id.toString(),
                                            color = if (isFocused) FocusCyan else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = channel.name,
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player setting option at bottom of overlay
                    var isSettingsBtnFocused by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSettingsBtnFocused) FocusCyan else DarkCardBackground)
                            .onFocusChanged { isSettingsBtnFocused = it.isFocused }
                            .focusable()
                            .clickable { showSettingsDialog = true }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Player Settings",
                            tint = if (isSettingsBtnFocused) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "প্লেয়ার সেটিংস",
                            color = if (isSettingsBtnFocused) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Toast-like Channel Banner on Up/Down Switch
        AnimatedVisibility(
            visible = showChannelOverlay,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            currentChannel?.let { channel ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TranslucentDark),
                    modifier = Modifier
                        .width(360.dp)
                        .border(1.dp, FocusCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(FocusCyan, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "CH ${channel.id}",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Red, RoundedCornerShape(4.dp))
                                        .size(8.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সরাসরি",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = channel.name,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )

                        Text(
                            text = channel.description,
                            color = SoftGray,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ক্যাটাগরি: ${channel.category}",
                                color = FocusCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "FHD • 1080p • 60 FPS",
                                color = SoftGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Close Player Screen Button
        var isBackBtnFocused by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isBackBtnFocused) FocusCyan else TranslucentBlack)
                .onFocusChanged { isBackBtnFocused = it.isFocused }
                .focusable()
                .clickable {
                    if (bootInPlayer) {
                        showExitDialog = true
                    } else {
                        onNavigateBack()
                    }
                }
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = if (isBackBtnFocused) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
