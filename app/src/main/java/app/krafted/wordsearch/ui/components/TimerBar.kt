package app.krafted.wordsearch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TimerGreen = Color(0xFF2E7D32)
private val TimerAmber = Color(0xFFF9A825)
private val TimerRed = Color(0xFFC62828)
private val TimerTrack = Color(0xFF1F1F1F)
private const val BAR_FULL_SECONDS = 180f
private const val AMBER_THRESHOLD_SECONDS = 30
private const val RED_THRESHOLD_SECONDS = 120

@Composable
fun TimerBar(
    timeElapsedSeconds: Int,
    isWarning: Boolean,
    modifier: Modifier = Modifier
) {
    val zoneColor = when {
        timeElapsedSeconds >= RED_THRESHOLD_SECONDS -> TimerRed
        timeElapsedSeconds >= AMBER_THRESHOLD_SECONDS || isWarning -> TimerAmber
        else -> TimerGreen
    }
    val animatedColor by animateColorAsState(
        targetValue = zoneColor,
        animationSpec = tween(durationMillis = 400),
        label = "timerColor"
    )
    val progress by animateFloatAsState(
        targetValue = (timeElapsedSeconds / BAR_FULL_SECONDS).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "timerProgress"
    )
    val minutes = timeElapsedSeconds / 60
    val seconds = timeElapsedSeconds % 60
    val formatted = "%02d:%02d".format(minutes, seconds)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "TIME",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatted,
                color = animatedColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TimerTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(animatedColor)
            )
        }
    }
}
