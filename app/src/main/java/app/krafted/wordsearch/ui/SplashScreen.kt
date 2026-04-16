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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val SurfaceDark = Color(0xFF030201)
private val GoldLight = Color(0xFFF6DA7B)
private val GoldMid = Color(0xFFE0B43E)
private val GoldDark = Color(0xFFA07B1D)
private val GoldGlint = Color(0xFFFFF8E1)
private val PanelElevated = Color(0xFF14100C)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val artResId = remember {
        context.resources.getIdentifier("jokag4_splash_3", "drawable", context.packageName)
    }

    var flipPhase by remember { mutableStateOf(0) }
    var showTitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        flipPhase = 1
        delay(500)
        flipPhase = 2
        delay(500)
        showTitle = true
        delay(1500)
        onFinished()
    }

    val cardScaleX by animateFloatAsState(
        targetValue = when (flipPhase) {
            0 -> 1f
            1 -> 0f
            else -> 1f
        },
        animationSpec = when (flipPhase) {
            1 -> tween(durationMillis = 400, easing = FastOutSlowInEasing)
            else -> spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
        },
        label = "cardFlip"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )
    val titleTranslateY by animateFloatAsState(
        targetValue = if (showTitle) 0f else 40f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "titleSlide"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )
    
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatCenter"
    )
    
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )
    val innerRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRing"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark),
        contentAlignment = Alignment.Center
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
                        radius = 1400f,
                        center = Offset(Float.MAX_VALUE / 2, Float.MAX_VALUE / 2)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { translationY = floatY }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GoldMid.copy(alpha = pulseGlow),
                                    GoldDark.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .rotate(ringRotation)
                        .border(
                            width = 2.dp,
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
                        .size(180.dp)
                        .rotate(innerRingRotation)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    GoldMid.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    GoldLight.copy(alpha = 0.5f),
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
                        width = 2.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(GoldLight, GoldMid, GoldDark)
                        )
                    ),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer { scaleX = cardScaleX }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (artResId != 0 && flipPhase >= 2) {
                            Image(
                                painter = painterResource(id = artResId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.2f),
                                            Color.Black.copy(alpha = 0.6f)
                                        ),
                                        radius = 300f
                                    )
                                )
                        )
                        if (flipPhase < 2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                GoldDark.copy(alpha = 0.5f),
                                                PanelElevated
                                            ),
                                            radius = 250f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "W",
                                    color = GoldGlint,
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = GoldDark.copy(alpha = 0.8f),
                                            offset = Offset(0f, 4f),
                                            blurRadius = 8f
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "WORD SEARCH",
                color = GoldGlint,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    fontSize = 32.sp,
                    letterSpacing = 8.sp,
                    shadow = Shadow(
                        color = GoldDark.copy(alpha = 0.9f),
                        offset = Offset(0f, 8f),
                        blurRadius = 18f
                    )
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha
                    translationY = titleTranslateY
                    scaleX = 0.95f + (0.05f * titleAlpha)
                    scaleY = 0.95f + (0.05f * titleAlpha)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .graphicsLayer { alpha = titleAlpha }
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
                    color = GoldLight.copy(alpha = titleAlpha),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        shadow = Shadow(
                            color = GoldDark,
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        )
                    )
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
}
