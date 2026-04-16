package app.krafted.wordsearch.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.wordsearch.data.PuzzleRepository

private val SurfaceDark = Color(0xFF030201)
private val PanelElevated = Color(0xFF14100C)
private val PanelDeep = Color(0xFF0C0A08)
private val PanelMuted = Color(0xFF1C140D)
private val GoldLight = Color(0xFFF6DA7B)
private val GoldMid = Color(0xFFE0B43E)
private val GoldDark = Color(0xFFA07B1D)
private val GoldGlint = Color(0xFFFFF8E1)
private val GoldInkText = Color(0xFF1A1203)
private val TextMuted = Color.White.copy(alpha = 0.68f)

private enum class ModeChoice {
    Timed,
    Relaxed
}

@Composable
fun ModeSelectScreen(
    categoryId: Int,
    puzzleNumber: Int,
    repository: PuzzleRepository,
    onBack: () -> Unit,
    onStartGame: (isTimedMode: Boolean) -> Unit
) {
    val category = remember(categoryId) { repository.getCategory(categoryId) }
    val puzzle = remember(categoryId, puzzleNumber) { repository.getPuzzle(categoryId, puzzleNumber) }
    val accent = remember(category?.accentColor) {
        runCatching { Color(android.graphics.Color.parseColor(category?.accentColor)) }
            .getOrDefault(Color(0xFFB71C1C))
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

    var selectedMode by rememberSaveable { mutableStateOf(ModeChoice.Timed) }
    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appeared = true
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlowUi")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val contentProgress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "contentProgress"
    )
    val contentBounceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "contentBounce"
    )

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
                            Color.Black.copy(alpha = 0.82f),
                            Color(0xFF110C05).copy(alpha = 0.68f),
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
                        colors = listOf(accent.copy(alpha = pulseGlow), Color.Transparent),
                        radius = 1400f,
                        center = Offset(0f, 600f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = topInset, bottom = bottomInset)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            ModeSelectHeader(
                categoryName = category?.name.orEmpty(),
                puzzleNumber = puzzleNumber,
                symbolResId = symbolResId,
                accentColor = accent,
                appeared = appeared,
                onBack = onBack
            )

            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = contentProgress
                    scaleX = contentBounceScale
                    scaleY = contentBounceScale
                    translationY = 40f * (1f - contentProgress)
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF161616).copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = "Choose how you want to tackle this puzzle.",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${puzzle?.words?.size ?: 0} hidden words are waiting.",
                            color = TextMuted,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ModeOptionCard(
                    title = "Timed",
                    subtitle = "Race the clock",
                    description = "The timer runs from the first swipe and your completion time stays eligible for leaderboard ranking.",
                    tag = "Best-time eligible",
                    icon = {
                        Text(
                            text = "T",
                            color = if (selectedMode == ModeChoice.Timed) GoldInkText else Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    },
                    selected = selectedMode == ModeChoice.Timed,
                    accentColor = accent,
                    onClick = { selectedMode = ModeChoice.Timed }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModeOptionCard(
                    title = "Relaxed",
                    subtitle = "No pressure",
                    description = "Play at your own pace with no running timer. Progress and score still save, but leaderboard times stay untouched.",
                    tag = "Progress only",
                    icon = {
                        Text(
                            text = "R",
                            color = if (selectedMode == ModeChoice.Relaxed) GoldInkText else Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    },
                    selected = selectedMode == ModeChoice.Relaxed,
                    accentColor = accent,
                    onClick = { selectedMode = ModeChoice.Relaxed }
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { onStartGame(selectedMode == ModeChoice.Timed) },
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(GoldLight, GoldMid, GoldDark)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Text(
                        text = if (selectedMode == ModeChoice.Timed) {
                            "START TIMED PUZZLE"
                        } else {
                            "START RELAXED PUZZLE"
                        },
                        color = GoldInkText,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.4.sp,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (selectedMode == ModeChoice.Timed) {
                        "Timed mode keeps the pressure on and records your best completion time."
                    } else {
                        "Relaxed mode is perfect for learning the grid without the clock breathing down your neck."
                    },
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeSelectHeader(
    categoryName: String,
    puzzleNumber: Int,
    symbolResId: Int,
    accentColor: Color,
    appeared: Boolean,
    onBack: () -> Unit
) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "headerScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .graphicsLayer {
                alpha = alphaAnim
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = PanelElevated,
                border = BorderStroke(1.dp, GoldMid.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onBack() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldLight
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PUZZLE  $puzzleNumber  •  ${categoryName.uppercase()}",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.4.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MODE SELECT",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        fontSize = 25.sp,
                        letterSpacing = 1.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.9f),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    maxLines = 1
                )
            }

            if (symbolResId != 0) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                ) {
                    Image(
                        painter = painterResource(id = symbolResId),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OrnateHeaderDivider()
    }
}

@Composable
private fun OrnateHeaderDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldMid.copy(alpha = 0.8f))
                    )
                )
        )
        Text(
            text = "  \u25C6  ",
            color = GoldLight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldMid.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    subtitle: String,
    description: String,
    tag: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "modeCardScale"
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (selected) 1f else 0.75f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "modeCardAlpha"
    )

    val shape = RoundedCornerShape(26.dp)
    val borderWidth = if (selected) 1.5.dp else 1.2.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = alphaAnim
            }
            .clip(shape)
            .clickable(onClick = onClick)
            .background(
                Brush.verticalGradient(
                    if (selected) {
                        listOf(Color(0xFF1C130D).copy(alpha = 0.8f), PanelElevated, PanelDeep)
                    } else {
                        listOf(Color(0xFF161616), Color(0xFF0C0C0C))
                    }
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    if (selected) {
                        listOf(GoldLight.copy(alpha=0.9f), GoldMid.copy(alpha=0.5f), GoldDark.copy(alpha=0.3f))
                    } else {
                        listOf(Color.White.copy(alpha=0.15f), Color.Transparent)
                    }
                ),
                shape = shape
            )
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldMid.copy(alpha = 0.15f), Color.Transparent),
                            radius = 450f,
                            center = Offset(0f, 50f)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoldLight.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.12f), Color.Transparent),
                            radius = 450f,
                            center = Offset(0f, 50f)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) GoldMid else PanelElevated,
                    border = BorderStroke(1.dp, if (selected) GoldLight else Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .padding(11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = if (selected) GoldLight else Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) GoldMid.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (selected) GoldLight.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = if (selected) "SELECTED" else tag,
                        color = if (selected) GoldLight else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}
