package app.krafted.wordsearch.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.rotate
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
import app.krafted.wordsearch.viewmodel.CategoryProgress
import app.krafted.wordsearch.viewmodel.HomeViewModel

private val SurfaceDark = Color(0xFF030201)
private val PanelElevated = Color(0xFF14100C)
private val PanelDeep = Color(0xFF0C0A08)
private val GoldLight = Color(0xFFF6DA7B)
private val GoldMid = Color(0xFFE0B43E)
private val GoldDark = Color(0xFFA07B1D)
private val GoldGlint = Color(0xFFFFF8E1)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategorySelected: (categoryId: Int) -> Unit,
    onLeaderboardTap: () -> Unit = {}
) {
    val state by viewModel.homeUiState.collectAsState()

    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        appeared = true
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val infiniteTransition = rememberInfiniteTransition(label = "bgGlow")
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
                        center = Offset(0f, 400f)
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
            val visibleCategories = state.categories.filter {
                !it.name.contains("Nature", ignoreCase = true) &&
                !it.name.contains("Space", ignoreCase = true) &&
                !it.name.contains("Travel", ignoreCase = true)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInset),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 24.dp,
                    bottom = 32.dp + bottomInset
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HomeHeader(
                        appeared = appeared,
                        infiniteTransition = infiniteTransition,
                        onLeaderboardTap = onLeaderboardTap
                    )
                }
                itemsIndexed(visibleCategories, key = { _, cat -> cat.categoryId }) { index, category ->
                    val delayMs = 150 + (index * 120)
                    AnimatedCategoryCard(
                        category = category,
                        appeared = appeared,
                        delayMs = delayMs,
                        infiniteTransition = infiniteTransition,
                        onClick = { onCategorySelected(category.categoryId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    appeared: Boolean,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onLeaderboardTap: () -> Unit
) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "headerScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
            .graphicsLayer {
                alpha = alphaAnim
                scaleX = scaleAnim
                scaleY = scaleAnim
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Surface(
                    shape = CircleShape,
                    color = PanelElevated,
                    border = BorderStroke(1.dp, GoldMid.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onLeaderboardTap() }
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
                        Text(
                            text = "\uD83C\uDFC6",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        HeroArtSymbol(infiniteTransition = infiniteTransition)

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "WORD SEARCH",
            color = GoldGlint,
            fontWeight = FontWeight.Black,
            style = TextStyle(
                fontSize = 28.sp,
                letterSpacing = 7.sp,
                shadow = Shadow(
                    color = GoldDark.copy(alpha = 0.8f),
                    offset = Offset(0f, 6f),
                    blurRadius = 14f
                )
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OrnateHeaderDivider()
    }
}

@Composable
private fun OrnateHeaderDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(0.75f)
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
            fontSize = 11.sp,
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
private fun HeroArtSymbol(infiniteTransition: androidx.compose.animation.core.InfiniteTransition) {
    val context = LocalContext.current
    val artResId = remember {
        context.resources.getIdentifier("jokag4_art_3", "drawable", context.packageName)
    }

    if (artResId == 0) return

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroHalo"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heroRing"
    )
    val innerRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heroInnerRing"
    )
    val breath by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroBreath"
    )
    
    val heroSize = 130.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(heroSize + 30.dp)
            .graphicsLayer {
                scaleX = breath
                scaleY = breath
            }
    ) {
        Box(
            modifier = Modifier
                .size(heroSize + 30.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldMid.copy(alpha = haloAlpha),
                            GoldDark.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(heroSize + 16.dp)
                .rotate(ringRotation)
                .border(
                    width = 1.8.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            GoldDark.copy(alpha = 0f),
                            GoldLight.copy(alpha = 0.9f),
                            GoldDark.copy(alpha = 0f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(heroSize + 4.dp)
                .rotate(innerRingRotation)
                .border(
                    width = 1.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            GoldMid.copy(alpha = 0.6f),
                            Color.Transparent,
                            GoldLight.copy(alpha = 0.4f),
                            Color.Transparent,
                            GoldMid.copy(alpha = 0.6f)
                        )
                    ),
                    shape = CircleShape
                )
        )
        Surface(
            shape = CircleShape,
            color = PanelElevated,
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    listOf(GoldLight, GoldMid, GoldDark)
                )
            ),
            modifier = Modifier.size(heroSize)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = artResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                radius = 220f
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun AnimatedCategoryCard(
    category: CategoryProgress,
    appeared: Boolean,
    delayMs: Int,
    infiniteTransition: androidx.compose.animation.core.InfiniteTransition,
    onClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 650, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "cardProgress"
    )

    val bounceScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.75f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardBounce"
    )
    
    val iconSway by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500 + (delayMs % 1000), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconSway"
    )
    
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cardShine"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = progress
                scaleX = bounceScale
                scaleY = bounceScale
                translationY = 60f * (1f - progress)
            }
    ) {
        CategoryCard(
            category = category,
            onClick = onClick,
            iconSway = iconSway,
            shineOffset = shineOffset
        )
    }
}

@Composable
private fun CategoryCard(
    category: CategoryProgress,
    onClick: () -> Unit,
    iconSway: Float,
    shineOffset: Float
) {
    val context = LocalContext.current
    val accent = remember(category.accentColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(category.accentColorHex)) }
            .getOrDefault(Color(0xFFB71C1C))
    }
    val symbolResId = remember(category.symbol) {
        if (category.symbol.isBlank()) 0
        else context.resources.getIdentifier(category.symbol, "drawable", context.packageName)
    }

    val shape = RoundedCornerShape(26.dp)
    val borderWidth = if (category.isFullyComplete) 1.5.dp else 1.2.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.80f)
            .clip(shape)
            .clickable { onClick() }
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1C130D).copy(alpha = 0.85f),
                        PanelElevated,
                        PanelDeep
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    if (category.isFullyComplete) {
                        listOf(GoldLight.copy(alpha=0.9f), GoldMid.copy(alpha=0.5f), GoldDark.copy(alpha=0.3f))
                    } else {
                        listOf(accent.copy(alpha=0.6f), accent.copy(alpha=0.2f), Color.Transparent)
                    }
                ),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                        radius = 450f,
                        center = Offset(0f, 100f)
                    )
                )
        )
        
        if (category.isFullyComplete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.7f)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, GoldGlint.copy(alpha = 0.5f), Color.Transparent),
                            start = Offset(x = shineOffset * 1000f - 200f, y = 0f),
                            end = Offset(x = shineOffset * 1000f + 200f, y = 1000f)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (category.isFullyComplete) GoldLight.copy(alpha=0.8f) else accent.copy(alpha=0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (symbolResId != 0) {
                    Image(
                        painter = painterResource(id = symbolResId),
                        contentDescription = category.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(86.dp)
                            .graphicsLayer { translationY = iconSway }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = category.name.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ProgressPill(
                completed = category.completedCount,
                total = category.totalPuzzles,
                accentColor = accent,
                isFullyComplete = category.isFullyComplete
            )
        }
    }
}

@Composable
private fun ProgressPill(
    completed: Int,
    total: Int,
    accentColor: Color,
    isFullyComplete: Boolean
) {
    val fill = if (isFullyComplete) GoldMid.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f)
    val borderColor = if (isFullyComplete) GoldLight.copy(alpha = 0.6f) else accentColor.copy(alpha = 0.45f)
    val textBaseColor = if (isFullyComplete) GoldLight else Color.White
    
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = fill,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = completed.toString(),
                color = textBaseColor,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Text(
                text = " / $total",
                color = textBaseColor.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFullyComplete) "COMPLETE" else "DONE",
                color = textBaseColor.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}
