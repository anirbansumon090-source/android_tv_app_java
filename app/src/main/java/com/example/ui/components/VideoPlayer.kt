package com.example.ui.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekParameters(androidx.media3.exoplayer.SeekParameters.CLOSEST_SYNC)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE // For infinite channel loops
                playWhenReady = true
                if (isMuted) {
                    volume = 0f
                }
            }
    }

    // Set up Player listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    playbackError = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackError = "প্লেব্যাক ত্রুটি: ${error.localizedMessage}"
                isBuffering = false
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Update MediaItem when videoUrl changes
    LaunchedEffect(videoUrl) {
        playbackError = null
        isBuffering = true
        try {
            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            playbackError = "ইউআরএল লোড করা সম্ভব হয়নি"
            isBuffering = false
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // No overlay controller
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // TV Fill screen
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 5.dp
                )
            }
        }

        // Error message overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playbackError ?: "অজানা প্লেব্যাক ত্রুটি",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
