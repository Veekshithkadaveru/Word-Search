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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.wordsearch.viewmodel.HomeViewModel
import app.krafted.wordsearch.viewmodel.PuzzleStatus

private val SurfaceDark = Color(0xFF030201)
private val PanelElevated = Color(0xFF14100C)
private val PanelDeep = Color(0xFF0C0A08)
private val GoldLight = Color(0xFFF6DA7B)
private val GoldMid = Color(0xFFE0B43E)
private val GoldDark = Color(0xFFA07B1D)
private val GoldGlint = Color(0xFFFFF8E1)

@Composable
fun PuzzleSelectScreen(
    categoryId: Int,
    viewModel: HomeViewModel,
    onPuzzleSelected: (categoryId: Int, puzzleNumber: Int) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.puzzleSelectUiState.collectAsState()

    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId)
        appeared = true
    }

    val accent = remember(state.accentColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(state.accentColorHex)) }
            .getOrDefault(Color(0xFFB71C1C))
    }

    val context = LocalContext.current
    val backgroundResId = remember(state.background) {
        if (state.background.isBlank()) 0
        else context.resources.getIdentifier(
            state.background,
            "drawable",
            context.packageName
        )
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

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldMid)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInset),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = 32.dp + bottomInset
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PuzzleSelectHeader(
                        categoryId = state.categoryId,
                        categoryName = state.categoryName,
                        accentColor = accent,
                        appeared = appeared,
                        onBack = onBack
                    )
                }
                itemsIndexed(state.puzzles, key = { _, puzzle -> puzzle.puzzleNumber }) { index, puzzle ->
                    val delayMs = 120 + (index * 60)
                    AnimatedPuzzleTile(
                        puzzle = puzzle,
                        accentColor = accent,
                        appeared = appeared,
                        delayMs = delayMs,
                        infiniteTransition = infiniteTransition,
                        onClick = { onPuzzleSelected(state.categoryId, puzzle.puzzleNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PuzzleSelectHeader(
    categoryId: Int,
    categoryName: String,
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
            .padding(bottom = 24.dp)
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
                    modifier = Modifier.fillMaxSize().background(
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
            
            Spacer(modifier = Modifier.size(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CATEGORY  $categoryId  /  10",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.8.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = categoryName.ifBlank { " " }.uppercase(),
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
private fun AnimatedPuzzleTile(
    puzzle: PuzzleStatus,
    accentColor: Color,
    appeared: Boolean,
    delayMs: Int,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "tileProgress"
    )

    val bounceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tileBounce"
    )
    
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1500 + (delayMs % 500), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tileShine"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = progress
                scaleX = bounceScale
                scaleY = bounceScale
                translationY = 50f * (1f - progress)
            }
    ) {
        PuzzleTile(
            puzzle = puzzle,
            accentColor = accentColor,
            shineOffset = shineOffset,
            onClick = onClick
        )
    }
}

@Composable
private fun PuzzleTile(
    puzzle: PuzzleStatus,
    accentColor: Color,
    shineOffset: Float,
    onClick: () -> Unit
) {
    val borderColor = when {
        !puzzle.isUnlocked -> Color.White.copy(alpha = 0.08f)
        puzzle.isCompleted -> GoldMid.copy(alpha = 0.7f)
        else -> accentColor.copy(alpha = 0.45f)
    }
    val borderWidth = if (puzzle.isCompleted) 1.5.dp else 1.2.dp
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .clip(shape)
            .clickable(enabled = puzzle.isUnlocked) { onClick() }
            .background(
                Brush.verticalGradient(
                    if (!puzzle.isUnlocked) {
                        listOf(Color(0xFF161616), Color(0xFF0C0C0C))
                    } else {
                        listOf(Color(0xFF1C130D).copy(alpha = 0.8f), PanelElevated, PanelDeep)
                    }
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    if (puzzle.isCompleted) {
                        listOf(GoldLight.copy(alpha=0.9f), GoldMid.copy(alpha=0.5f), GoldDark.copy(alpha=0.3f))
                    } else if (puzzle.isUnlocked) {
                        listOf(accentColor.copy(alpha=0.6f), accentColor.copy(alpha=0.2f), Color.Transparent)
                    } else {
                        listOf(Color.White.copy(alpha=0.15f), Color.Transparent)
                    }
                ),
                shape = shape
            )
    ) {
        if (puzzle.isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (puzzle.isCompleted) GoldMid.copy(alpha = 0.15f) else accentColor.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            radius = 350f,
                            center = Offset(0f, 50f)
                        )
                    )
            )
        }
        
        if (puzzle.isCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.75f)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, GoldGlint.copy(alpha = 0.5f), Color.Transparent),
                            start = Offset(x = shineOffset * 800f - 150f, y = 0f),
                            end = Offset(x = shineOffset * 800f + 150f, y = 800f)
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
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
                    .background(
                        Brush.verticalGradient(listOf(GoldLight, GoldMid)),
                        CircleShape
                    )
                    .border(1.dp, GoldDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PanelDeep,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else if (puzzle.isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, accentColor.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
        }

        if (!puzzle.isUnlocked) {
            LockedContent()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PUZZLE",
                    color = if (puzzle.isCompleted) GoldLight.copy(alpha=0.8f) else Color.White.copy(alpha=0.65f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = puzzle.puzzleNumber.toString(),
                    color = if (puzzle.isCompleted) GoldLight else Color.White,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        fontSize = 46.sp,
                        shadow = Shadow(
                            color = if(puzzle.isCompleted) GoldDark.copy(alpha=0.6f) else Color.Transparent,
                            offset = Offset(0f, 4f),
                            blurRadius = 6f
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (puzzle.isCompleted) {
                    val label = if (puzzle.bestTimeSeconds > 0) {
                        "BEST  ${formatTime(puzzle.bestTimeSeconds)}"
                    } else {
                        "COMPLETED"
                    }
                    Text(
                        text = label,
                        color = GoldLight.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                } else {
                    Text(
                        text = "TAP TO PLAY",
                        color = accentColor.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "LOCKED",
            color = Color.White.copy(alpha = 0.40f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
