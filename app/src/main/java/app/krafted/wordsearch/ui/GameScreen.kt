package app.krafted.wordsearch.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.krafted.wordsearch.data.PuzzleRepository
import app.krafted.wordsearch.data.db.PuzzleDao
import app.krafted.wordsearch.ui.components.JokerReaction
import app.krafted.wordsearch.ui.components.TimerBar
import app.krafted.wordsearch.ui.components.WordGrid
import app.krafted.wordsearch.ui.components.WordList
import app.krafted.wordsearch.viewmodel.GameViewModel
import kotlinx.coroutines.delay

private const val COMPLETE_NAV_DELAY_MS = 600L
private val SurfaceDark = Color(0xFF0A0A0A)
private val PanelDark = Color(0xFF101010)
private val DividerDark = Color(0xFF262626)

@Composable
fun GameScreen(
    categoryId: Int,
    puzzleNumber: Int,
    isTimedMode: Boolean,
    repository: PuzzleRepository,
    dao: PuzzleDao,
    onBack: () -> Unit,
    onComplete: (score: Int, timeSeconds: Int, isNewBest: Boolean) -> Unit
) {
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(repository, dao))
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId, puzzleNumber, isTimedMode) {
        viewModel.startPuzzle(categoryId, puzzleNumber, isTimedMode)
    }

    LaunchedEffect(state.isError) {
        if (state.isError) onBack()
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            delay(COMPLETE_NAV_DELAY_MS)
            onComplete(state.score, state.timeElapsedSeconds, state.isNewBest)
        }
    }

    val accent = remember(state.accentColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(state.accentColorHex)) }
            .getOrDefault(Color(0xFFB71C1C))
    }

    val context = LocalContext.current
    val backgroundResId = remember(state.backgroundDrawable) {
        if (state.backgroundDrawable.isBlank()) 0
        else context.resources.getIdentifier(
            state.backgroundDrawable,
            "drawable",
            context.packageName
        )
    }

    val totalWords = state.placedWords.size
    val foundCount = state.placedWords.count { it.isFound }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(SurfaceDark)) {
        if (backgroundResId != 0) {
            Image(
                painter = painterResource(id = backgroundResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.78f),
                            Color.Black.copy(alpha = 0.62f),
                            Color.Black.copy(alpha = 0.82f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = topInset, bottom = bottomInset)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            GameHeader(
                categoryName = state.categoryName,
                puzzleNumber = state.puzzleNumber,
                foundCount = foundCount,
                totalWords = totalWords,
                accentColor = accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            JokerReaction(quote = state.jokerQuote, accentColor = accent)

            if (state.isTimedMode) {
                Spacer(modifier = Modifier.height(18.dp))
                TimerBar(
                    timeElapsedSeconds = state.timeElapsedSeconds,
                    isWarning = state.timerWarningTriggered
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PanelDark,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    WordGrid(
                        grid = state.grid,
                        selectedCells = state.selectedCells,
                        foundCells = state.foundCells,
                        accentColor = accent,
                        isWrongFlash = state.isWrongFlash,
                        onDragStart = viewModel::onDragStart,
                        onDragMove = viewModel::onDragMove,
                        onDragEnd = viewModel::onDragEnd
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionDivider()

            Spacer(modifier = Modifier.height(14.dp))

            WordList(
                remainingWords = state.remainingWords,
                foundWords = state.foundWords,
                accentColor = accent
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GameHeader(
    categoryName: String,
    puzzleNumber: Int,
    foundCount: Int,
    totalWords: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PUZZLE  $puzzleNumber  /  10",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = categoryName.ifBlank { " " }.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineSmall,
                letterSpacing = 0.5.sp
            )
        }
        ProgressPill(foundCount = foundCount, totalWords = totalWords, accentColor = accentColor)
    }
}

@Composable
private fun ProgressPill(foundCount: Int, totalWords: Int, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accentColor.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = foundCount.toString(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Text(
                text = " / $totalWords",
                color = Color.White.copy(alpha = 0.55f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FOUND",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerDark)
    )
}

