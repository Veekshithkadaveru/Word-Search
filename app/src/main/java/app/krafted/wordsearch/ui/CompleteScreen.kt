package app.krafted.wordsearch.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.wordsearch.data.PuzzleRepository
import app.krafted.wordsearch.data.QuoteEvent
import app.krafted.wordsearch.data.db.PuzzleDao
import app.krafted.wordsearch.ui.components.JokerReaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SurfaceDark = Color(0xFF0A0A0A)
private val PanelElevated = Color(0xFF141414)
private val PanelDeep = Color(0xFF0C0C0C)
private val DividerDark = Color(0xFF262626)
private val TextMuted = Color.White.copy(alpha = 0.62f)
private val GoldLight = Color(0xFFF6DA7B)
private val GoldMid = Color(0xFFE0B43E)
private val GoldDark = Color(0xFFA07B1D)
private val GoldInkText = Color(0xFF1A1203)
private val GoldGlint = Color(0xFFFFF8E1)
private const val SCORE_ANIM_DURATION_MS = 1100
private const val LAST_PUZZLE_NUMBER = 10
private const val WORDS_PER_PUZZLE = 8
private const val QUOTE_REVEAL_DELAY_MS = 620L
private const val NEW_BEST_REVEAL_DELAY_MS = 880L

@Composable
fun CompleteScreen(
    categoryId: Int,
    puzzleNumber: Int,
    score: Int,
    timeSeconds: Int,
    isNewBest: Boolean,
    repository: PuzzleRepository,
    dao: PuzzleDao,
    onNextPuzzle: () -> Unit,
    onHome: () -> Unit
) {
    val category = remember(categoryId) { repository.getCategory(categoryId) }
    val accent = remember(category?.accentColor) {
        runCatching { Color(android.graphics.Color.parseColor(category?.accentColor)) }
            .getOrDefault(Color(0xFFB71C1C))
    }
    val quote = remember(categoryId) {
        repository.getRandomQuote(categoryId, QuoteEvent.COMPLETE).orEmpty()
    }

    val context = LocalContext.current
    val backgroundResId = remember(category?.background) {
        val name = category?.background.orEmpty()
        if (name.isBlank()) 0
        else context.resources.getIdentifier(name, "drawable", context.packageName)
    }
    val symbolResId = remember(category?.symbol) {
        val name = category?.symbol.orEmpty()
        if (name.isBlank()) 0
        else context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    var playerName by remember { mutableStateOf(TextFieldValue("")) }
    var nameSaved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var appeared by remember { mutableStateOf(false) }
    var quoteVisible by remember { mutableStateOf(false) }
    var newBestReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appeared = true
        delay(QUOTE_REVEAL_DELAY_MS)
        quoteVisible = true
    }
    LaunchedEffect(isNewBest) {
        if (isNewBest) {
            delay(NEW_BEST_REVEAL_DELAY_MS)
            newBestReady = true
        } else {
            newBestReady = false
        }
    }

    val heroScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.35f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
        label = "heroScale"
    )
    val heroAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "heroAlpha"
    )
    val labelProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = 140, easing = FastOutSlowInEasing),
        label = "label"
    )
    val titleProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 220, easing = FastOutSlowInEasing),
        label = "title"
    )
    val subtitleProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = 320, easing = FastOutSlowInEasing),
        label = "subtitle"
    )
    val dividerProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 380, easing = FastOutSlowInEasing),
        label = "divider"
    )
    val statsProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 520, delayMillis = 460, easing = FastOutSlowInEasing),
        label = "stats"
    )
    val quoteProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 600, easing = FastOutSlowInEasing),
        label = "quote"
    )
    val buttonsProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 520, delayMillis = 760, easing = FastOutSlowInEasing),
        label = "buttons"
    )
    val newBestProgress by animateFloatAsState(
        targetValue = if (newBestReady) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.48f, stiffness = Spring.StiffnessMedium),
        label = "newBest"
    )

    val animatedScore by animateIntAsState(
        targetValue = if (statsProgress > 0.1f) score else 0,
        animationSpec = tween(durationMillis = SCORE_ANIM_DURATION_MS, easing = FastOutSlowInEasing),
        label = "score"
    )

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isLastPuzzle = puzzleNumber >= LAST_PUZZLE_NUMBER

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
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
                            Color.Black.copy(alpha = 0.90f),
                            Color.Black.copy(alpha = 0.74f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                        radius = 640f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topInset, bottom = bottomInset)
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            HeroSymbol(
                symbolResId = symbolResId,
                accentColor = accent,
                scale = heroScale,
                alphaProgress = heroAlpha
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "PUZZLE COMPLETE",
                color = GoldLight,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge,
                letterSpacing = 3.sp,
                modifier = Modifier.staggerEntry(labelProgress, 14f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${category?.name.orEmpty()}  \u00B7  Puzzle $puzzleNumber of $LAST_PUZZLE_NUMBER",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp,
                modifier = Modifier.staggerEntry(subtitleProgress, 12f)
            )

            Spacer(modifier = Modifier.height(2.dp))
            OrnateDivider(progress = dividerProgress)
            Spacer(modifier = Modifier.height(2.dp))

            StatsCard(
                timeSeconds = timeSeconds,
                scoreDisplay = animatedScore,
                modifier = Modifier.staggerEntry(statsProgress, 24f)
            )

            if (isNewBest) {
                Spacer(modifier = Modifier.height(4.dp))
                NewBestBadge(entryProgress = newBestProgress)
            }

            Spacer(modifier = Modifier.height(2.dp))

            NameEntryCard(
                playerName = playerName,
                onNameChange = { playerName = it },
                nameSaved = nameSaved,
                onSave = {
                    val name = playerName.text.trim()
                    if (name.isNotEmpty()) {
                        scope.launch {
                            dao.updatePlayerName(categoryId, puzzleNumber, name)
                            nameSaved = true
                        }
                    }
                },
                modifier = Modifier.staggerEntry(quoteProgress, 20f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            JokerReaction(
                quote = if (quoteVisible) quote else "",
                accentColor = accent,
                modifier = Modifier.staggerEntry(quoteProgress, 20f)
            )

            Spacer(modifier = Modifier.weight(1f))

            ActionRow(
                nextEnabled = !isLastPuzzle,
                nextLabel = if (isLastPuzzle) "CATEGORY COMPLETE" else "NEXT PUZZLE",
                onNextPuzzle = onNextPuzzle,
                onHome = onHome,
                modifier = Modifier.staggerEntry(buttonsProgress, 24f)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NameEntryCard(
    playerName: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    nameSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF181410),
                        PanelElevated,
                        PanelDeep
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        GoldLight.copy(alpha = 0.5f),
                        GoldMid.copy(alpha = 0.25f),
                        GoldDark.copy(alpha = 0.15f)
                    )
                ),
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "ENTER YOUR NAME",
                color = GoldLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(
                            1.dp,
                            if (nameSaved) GoldMid.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (playerName.text.isEmpty() && !nameSaved) {
                        Text(
                            text = "Your name...",
                            color = Color.White.copy(alpha = 0.28f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    BasicTextField(
                        value = playerName,
                        onValueChange = { if (!nameSaved && it.text.length <= 20) onNameChange(it) },
                        enabled = !nameSaved,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val saveShape = RoundedCornerShape(12.dp)
                Button(
                    onClick = onSave,
                    enabled = playerName.text.trim().isNotEmpty() && !nameSaved,
                    shape = saveShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .background(
                            brush = if (playerName.text.trim().isNotEmpty() && !nameSaved) {
                                Brush.verticalGradient(listOf(GoldLight, GoldMid, GoldDark))
                            } else {
                                Brush.verticalGradient(
                                    listOf(GoldMid.copy(alpha = 0.25f), GoldDark.copy(alpha = 0.15f))
                                )
                            },
                            shape = saveShape
                        )
                ) {
                    Text(
                        text = if (nameSaved) "SAVED" else "SAVE",
                        color = if (playerName.text.trim().isNotEmpty() && !nameSaved) GoldInkText
                        else GoldInkText.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.8.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSymbol(
    symbolResId: Int,
    accentColor: Color,
    scale: Float,
    alphaProgress: Float
) {
    val infinite = rememberInfiniteTransition(label = "hero")
    val haloAlpha by infinite.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )
    val ringRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )
    val innerRingRotation by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRing"
    )
    val sway by infinite.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )
    val breath by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(96.dp)
            .graphicsLayer {
                scaleX = scale * breath
                scaleY = scale * breath
                alpha = alphaProgress
                rotationZ = sway * alphaProgress
            }
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = haloAlpha),
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(82.dp)
                .rotate(ringRotation)
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0f),
                            accentColor.copy(alpha = 0.95f),
                            accentColor.copy(alpha = 0f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .rotate(innerRingRotation)
                .border(
                    width = 1.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            GoldMid.copy(alpha = 0.55f),
                            Color.Transparent,
                            GoldLight.copy(alpha = 0.35f),
                            Color.Transparent,
                            GoldMid.copy(alpha = 0.55f)
                        )
                    ),
                    shape = CircleShape
                )
        )
        Surface(
            shape = CircleShape,
            color = PanelElevated,
            border = BorderStroke(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(GoldLight, GoldMid, GoldDark)
                )
            ),
            modifier = Modifier.size(58.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                if (symbolResId != 0) {
                    Image(
                        painter = painterResource(id = symbolResId),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(10.dp)
                    )
                } else {
                    Text(
                        text = "\u2605",
                        color = accentColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                endY = 90f
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun OrnateDivider(progress: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(0.72f * progress.coerceIn(0f, 1f))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldMid.copy(alpha = 0.78f))
                    )
                )
        )
        Text(
            text = "  \u25C6  ",
            color = GoldLight.copy(alpha = progress),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldMid.copy(alpha = 0.78f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun StatsCard(
    timeSeconds: Int,
    scoreDisplay: Int,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF181410),
                        PanelElevated,
                        PanelDeep
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        GoldLight.copy(alpha = 0.75f),
                        GoldMid.copy(alpha = 0.45f),
                        GoldDark.copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            GoldLight.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "FINAL SCORE",
                color = GoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%,d".format(scoreDisplay),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GoldMid.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryStat(
                    label = "TIME",
                    value = formatTime(timeSeconds),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    GoldMid.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                SecondaryStat(
                    label = "WORDS",
                    value = "$WORDS_PER_PUZZLE / $WORDS_PER_PUZZLE",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SecondaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = GoldLight.copy(alpha = 0.92f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.8.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun NewBestBadge(entryProgress: Float) {
    val infinite = rememberInfiniteTransition(label = "newBestAnim")
    
    val shineOffset by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineOffset"
    )
    
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val combinedScale = entryProgress * pulseScale
    val shape = RoundedCornerShape(12.dp)
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = combinedScale
                scaleY = combinedScale
                alpha = entryProgress.coerceIn(0f, 1f)
            }
            .background(
                Brush.radialGradient(
                    colors = listOf(GoldMid.copy(alpha = 0.15f * entryProgress), Color.Transparent),
                    radius = 300f
                )
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(GoldLight, GoldMid, GoldDark)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.6f),
                        GoldDark.copy(alpha = 0.8f)
                    )
                ),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        0.0f to Color.Transparent,
                        0.45f to Color.Transparent,
                        0.5f to GoldGlint.copy(alpha = 0.6f),
                        0.55f to Color.Transparent,
                        1.0f to Color.Transparent,
                        start = androidx.compose.ui.geometry.Offset(x = shineOffset * 500f, y = 0f),
                        end = androidx.compose.ui.geometry.Offset(x = shineOffset * 500f + 100f, y = 200f)
                    )
                )
        )
        
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "\u2727",
                color = GoldInkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "NEW PERSONAL BEST",
                color = GoldInkText,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 2.5.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "\u2727",
                color = GoldInkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ActionRow(
    nextEnabled: Boolean,
    nextLabel: String,
    onNextPuzzle: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrimaryActionButton(
            label = nextLabel,
            enabled = nextEnabled,
            onClick = onNextPuzzle,
            modifier = Modifier.weight(1.3f)
        )
        SecondaryActionButton(
            label = "HOME",
            onClick = onHome,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PrimaryActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val fillBrush = if (enabled) {
        Brush.verticalGradient(
            listOf(
                GoldLight,
                GoldMid,
                GoldDark
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                GoldMid.copy(alpha = 0.32f),
                GoldDark.copy(alpha = 0.22f)
            )
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        modifier = modifier.height(46.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(fillBrush)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.55f),
                            GoldDark.copy(alpha = 0.4f)
                        )
                    ),
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(22.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.38f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(22.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.32f)
                            )
                        )
                    )
            )
            Text(
                text = label,
                color = if (enabled) GoldInkText else GoldInkText.copy(alpha = 0.45f),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 2.6.sp
            )
        }
    }
}

@Composable
private fun SecondaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    OutlinedButton(
        onClick = onClick,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    GoldDark.copy(alpha = 0.45f),
                    GoldLight.copy(alpha = 0.9f),
                    GoldDark.copy(alpha = 0.45f)
                )
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.Transparent
        ),
        modifier = modifier.height(46.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GoldMid.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = GoldLight,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 2.6.sp
            )
        }
    }
}

private fun Modifier.staggerEntry(progress: Float, slideDp: Float): Modifier =
    this.graphicsLayer {
        alpha = progress.coerceIn(0f, 1f)
        translationY = (1f - progress.coerceIn(0f, 1f)) * slideDp.dp.toPx()
    }

private fun formatTime(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}
