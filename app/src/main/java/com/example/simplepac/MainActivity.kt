package com.example.simplepac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.simplepac.data.Flashcard
import com.example.simplepac.network.AudioPlayerHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                FlashcardScreen()
            }
        }
    }
}

@Composable
fun FlashcardScreen(viewModel: FlashcardViewModel = hiltViewModel()) {
    val state by viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                ErrorView(error = state.error!!, onRetry = { viewModel.loadFlashcards() })
            }
            state.flashcards.isEmpty() -> {
                EmptyView()
            }
            else -> {
                val currentCard = state.flashcards[state.currentCardIndex]
                FlashcardItem(
                    card = currentCard,
                    isFlipped = state.isCardFlipped,
                    onFlipClick = { viewModel.toggleCardFlip() },
                    modifier = Modifier.weight(1f)
                )

                NavigationControls(
                    currentIndex = state.currentCardIndex,
                    totalCards = state.flashcards.size,
                    onNext = { viewModel.nextCard() },
                    onPrevious = { viewModel.previousCard() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun FlashcardItem(
    card: Flashcard,
    isFlipped: Boolean,
    onFlipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPlayerHelper = remember { AudioPlayerHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerHelper.stop()
            audioPlayerHelper.release()
        }
    }
    LaunchedEffect(card.audioUrl) {
        audioPlayerHelper.preparePlayer(card.audioUrl ?: "")
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clickable { onFlipClick() }
            .shadow(5.dp, RoundedCornerShape(12.dp))
            .background(Color(0xFF313131), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isFlipped) {
            QuestionSide(card, audioPlayerHelper)
        } else {
            AnswerSide(card)
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun QuestionSide(
    card: Flashcard,
    audioPlayerHelper: AudioPlayerHelper
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        card.imageUrl?.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Flashcard image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = card.question,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        card.audioUrl?.let { audioUrl ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)  // Standard height for player controls
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            audioPlayerHelper.preparePlayer(audioUrl)
                            audioPlayerHelper.bindPlayerView(this)
                            setShowPreviousButton(false)
                            setShowNextButton(false)
                            setShowRewindButton(false)
                            setShowFastForwardButton(false)
                        }
                    },
                    update = { playerView ->
                        if (isPlaying) {
                            audioPlayerHelper.play()
                        } else {
                            audioPlayerHelper.pause()
                        }
                    }
                )
            }
        }
        Text("Tap to flip", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun AnswerSide(card: Flashcard) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Answer",
            color = Color(0xFFFF7B2E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = card.answer,
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Tap to flip back", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun NavigationControls(
    currentIndex: Int,
    totalCards: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPrevious,
            enabled = currentIndex > 0
        ) {
            Text("Previous")
        }

        Text("${currentIndex + 1}/$totalCards", color = Color.White)

        Button(
            onClick = onNext,
            enabled = currentIndex < totalCards - 1
        ) {
            Text("Next")
        }
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = error, color = Color.Red)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No flashcards available", color = Color.White)
    }
}


