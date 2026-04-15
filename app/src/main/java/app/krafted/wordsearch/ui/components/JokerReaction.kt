package app.krafted.wordsearch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val TYPEWRITER_DELAY_MS = 20L
private val MinCardHeight = 64.dp

@Composable
fun JokerReaction(
    quote: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var displayed by remember(quote) { mutableStateOf("") }
    var isTyping by remember(quote) { mutableStateOf(quote.isNotEmpty()) }

    LaunchedEffect(quote) {
        if (quote.isEmpty()) {
            displayed = ""
            isTyping = false
            return@LaunchedEffect
        }
        displayed = ""
        isTyping = true
        for (i in 1..quote.length) {
            displayed = quote.substring(0, i)
            delay(TYPEWRITER_DELAY_MS)
        }
        isTyping = false
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121212),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinCardHeight)
            .height(IntrinsicSize.Min)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Text(
                text = displayed + if (isTyping) "\u258C" else "",
                color = Color.White.copy(alpha = 0.92f),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
    }
}
