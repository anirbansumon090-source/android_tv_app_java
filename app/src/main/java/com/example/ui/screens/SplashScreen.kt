package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SplashState
import com.example.ui.TvViewModel
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.FocusCyan
import com.example.ui.theme.MidnightBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: TvViewModel,
    onNavigateNext: (bootInPlayer: Boolean) -> Unit
) {
    var loadingText by remember { mutableStateOf("ফায়ারবেস কনফিগারেশন লোড হচ্ছে...") }
    val pulseScale = remember { Animatable(1f) }

    // Pulse animation
    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Loader text simulation matching user flow (Firebase -> API Catalog -> Boot check)
    LaunchedEffect(Unit) {
        viewModel.startSplashScreen { bootInPlayer ->
            onNavigateNext(bootInPlayer)
        }
        
        delay(700)
        loadingText = "ক্যাটালগ সার্ভার থেকে এপিআই ডাটা ফেচ করা হচ্ছে..."
        delay(800)
        loadingText = "সিকিউরিটি HMAC কী ভ্যালিডেশন করা হচ্ছে..."
        delay(700)
        loadingText = "রিমোট বুট-ইন-প্লেয়ার কন্ডিশন যাচাই করা হচ্ছে..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(DarkCardBackground, MidnightBlue),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(pulseScale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(FocusCyan.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "App Logo",
                    tint = FocusCyan,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SMART TV LIVE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    color = Color.White
                )
            )
            Text(
                text = "সরাসরি সম্প্রচার ও স্মার্ট বিনোদন প্ল্যাটফর্ম",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = FocusCyan,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.75f)
                )
            )
        }
    }
}
