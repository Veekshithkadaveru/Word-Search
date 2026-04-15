package app.krafted.wordsearch.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.krafted.wordsearch.game.PlacedWord

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordList(
    remainingWords: List<PlacedWord>,
    foundWords: List<PlacedWord>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val allWords = (remainingWords + foundWords).sortedBy { it.word }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Words to find:",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            allWords.forEach { word ->
                val alpha by animateFloatAsState(
                    targetValue = if (word.isFound) 0.5f else 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "wordAlpha"
                )
                val background = if (word.isFound) {
                    accentColor.copy(alpha = 0.25f)
                } else {
                    Color(0xFF2A2A2A)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = background
                ) {
                    Text(
                        text = word.word,
                        color = Color.White.copy(alpha = alpha),
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            textDecoration = if (word.isFound) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
