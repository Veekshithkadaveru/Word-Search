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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.wordsearch.data.PuzzleCategory
import app.krafted.wordsearch.data.PuzzleRepository
import app.krafted.wordsearch.data.db.PuzzleDao
import app.krafted.wordsearch.data.db.PuzzleProgress

private val SurfaceDark = Color(0xFF030201)
private val PanelElevated = Color(0xFF14100C)
private val PanelDeep = Color(0xFF0C0A08)
private val GoldLight = Color(0xFFF6DA7B)
private val GoldMid = Color(0xFFE0B43E)
private val GoldDark = Color(0xFFA07B1D)
private val GoldGlint = Color(0xFFFFF8E1)
private val TextMuted = Color.White.copy(alpha = 0.68f)

private data class LeaderboardCategory(
    val category: PuzzleCategory,
    val entries: List<PuzzleProgress>
)

@Composable
fun LeaderboardScreen(
    repository: PuzzleRepository,
    dao: PuzzleDao,
    onBack: () -> Unit
) {
    var leaderboardData by remember { mutableStateOf<List<LeaderboardCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val categories = repository.getAllCategories()
        val data = categories.map { category ->
            LeaderboardCategory(
                category = category,
                entries = dao.getBestTimesByCategory(category.id)
            )
        }
        leaderboardData = data
        isLoading = false
    }

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val infiniteTransition = rememberInfiniteTransition(label = "lbGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val headerAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    val headerScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "headerScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color(0xFF110C05).copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldDark.copy(alpha = pulseGlow), Color.Transparent),
                        radius = 1600f,
                        center = Offset(0f, 300f)
                    )
                )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldMid)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInset)
            ) {
                LeaderboardHeader(
                    onBack = onBack,
                    alpha = headerAlpha,
                    scale = headerScale
                )

                if (leaderboardData.isNotEmpty()) {
                    CategoryTabRow(
                        categories = leaderboardData,
                        selectedIndex = selectedIndex,
                        onSelected = { selectedIndex = it },
                        appeared = appeared
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (leaderboardData.isNotEmpty()) {
                    val selected = leaderboardData[selectedIndex]
                    val accent = remember(selected.category.accentColor) {
                        runCatching { Color(android.graphics.Color.parseColor(selected.category.accentColor)) }
                            .getOrDefault(Color(0xFFB71C1C))
                    }

                    if (selected.entries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No timed completions yet",
                                    color = TextMuted,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Complete puzzles in Timed mode to appear here",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 48.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                bottom = 32.dp + bottomInset
                            ),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(
                                selected.entries,
                                key = { _, entry -> "${selected.category.id}_${entry.puzzleNumber}" }
                            ) { index, entry ->
                                val delayMs = 80 + (index * 60)
                                val entryAlpha by animateFloatAsState(
                                    targetValue = if (appeared) 1f else 0f,
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        delayMillis = delayMs,
                                        easing = FastOutSlowInEasing
                                    ),
                                    label = "row$index"
                                )
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        alpha = entryAlpha
                                        translationY = 20f * (1f - entryAlpha)
                                    }
                                ) {
                                    LeaderboardRow(
                                        rank = index + 1,
                                        entry = entry,
                                        accent = accent,
                                        isFirst = index == 0
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

@Composable
private fun LeaderboardHeader(
    onBack: () -> Unit,
    alpha: Float,
    scale: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
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

            Column {
                Text(
                    text = "BEST TIMES",
                    color = GoldMid,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "LEADERBOARD",
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
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
}

@Composable
private fun CategoryTabRow(
    categories: List<LeaderboardCategory>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    appeared: Boolean
) {
    val tabAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "tabAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = tabAlpha }
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEachIndexed { index, item ->
            CategoryTab(
                category = item.category,
                entryCount = item.entries.size,
                isSelected = index == selectedIndex,
                onClick = { onSelected(index) }
            )
        }
    }
}

@Composable
private fun CategoryTab(
    category: PuzzleCategory,
    entryCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val accent = remember(category.accentColor) {
        runCatching { Color(android.graphics.Color.parseColor(category.accentColor)) }
            .getOrDefault(Color(0xFFB71C1C))
    }
    val symbolResId = remember(category.symbol) {
        if (category.symbol.isBlank()) 0
        else context.resources.getIdentifier(category.symbol, "drawable", context.packageName)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "tabScale"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable { onClick() }
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.25f),
                            PanelElevated
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(PanelElevated, PanelDeep)
                    )
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.8f), accent.copy(alpha = 0.3f))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                    )
                },
                shape = shape
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (symbolResId != 0) {
                Image(
                    painter = painterResource(id = symbolResId),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = category.name.uppercase(),
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    maxLines = 1
                )
                Text(
                    text = if (entryCount > 0) "$entryCount record${if (entryCount != 1) "s" else ""}" else "---",
                    color = if (isSelected) accent else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: PuzzleProgress,
    accent: Color,
    isFirst: Boolean
) {
    val shape = RoundedCornerShape(16.dp)
    val isTopThree = rank <= 3

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (isFirst) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1C130D).copy(alpha = 0.8f),
                            PanelElevated,
                            PanelDeep
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(PanelElevated.copy(alpha = 0.6f), PanelDeep.copy(alpha = 0.4f))
                    )
                }
            )
            .border(
                width = if (isFirst) 1.dp else 0.5.dp,
                brush = if (isFirst) {
                    Brush.verticalGradient(
                        listOf(GoldLight.copy(alpha = 0.6f), GoldMid.copy(alpha = 0.2f), Color.Transparent)
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    )
                },
                shape = shape
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        if (isFirst) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoldLight.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rankText = when (rank) {
                1 -> "\uD83E\uDD47"
                2 -> "\uD83E\uDD48"
                3 -> "\uD83E\uDD49"
                else -> "#$rank"
            }

            if (isTopThree) {
                Text(
                    text = rankText,
                    fontSize = 22.sp,
                    modifier = Modifier.width(38.dp)
                )
            } else {
                Text(
                    text = rankText,
                    color = GoldLight.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.width(38.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.playerName.ifBlank { "Puzzle ${entry.puzzleNumber}" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (entry.playerName.isNotBlank()) {
                    Text(
                        text = "Puzzle ${entry.puzzleNumber}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatLeaderboardTime(entry.bestTimeSeconds),
                    color = if (isFirst) GoldLight else accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "%,d pts".format(entry.bestScore),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatLeaderboardTime(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}
