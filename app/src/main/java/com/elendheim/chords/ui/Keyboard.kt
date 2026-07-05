package com.elendheim.chords.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elendheim.chords.model.Note
import com.elendheim.chords.ui.theme.KeyEbony
import com.elendheim.chords.ui.theme.KeyIvory

private val WHITE_KEY_WIDTH = 54.dp
private val BLACK_KEY_WIDTH = 34.dp
private const val BLACK_KEY_HEIGHT_FRACTION = 0.58f

// C2 through C7 gives plenty of room without feeling endless.
private const val LOW_MIDI = 36
private const val HIGH_MIDI = 96
private const val MIDDLE_C = 60

/**
 * A scrollable piano keyboard. Tapping a key toggles it in the current chord.
 */
@Composable
fun Keyboard(
    selectedNotes: Set<Int>,
    onKeyTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val whiteKeys = remember { (LOW_MIDI..HIGH_MIDI).filterNot { Note.isBlack(it) } }
    val blackKeys = remember { (LOW_MIDI..HIGH_MIDI).filter { Note.isBlack(it) } }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Start the keyboard centered near middle C.
    LaunchedEffect(Unit) {
        val whitesBeforeMiddleC = whiteKeys.count { it < MIDDLE_C }
        val target = with(density) { (WHITE_KEY_WIDTH * whitesBeforeMiddleC).toPx() } -
            scrollState.viewportSize / 3f
        scrollState.scrollTo(target.toInt().coerceAtLeast(0))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
    ) {
        Box(Modifier.width(WHITE_KEY_WIDTH * whiteKeys.size)) {
            Row(Modifier.fillMaxSize()) {
                for (midi in whiteKeys) {
                    WhiteKey(
                        midi = midi,
                        selected = midi in selectedNotes,
                        onTap = onKeyTap
                    )
                }
            }
            for (midi in blackKeys) {
                val whitesBelow = whiteKeys.count { it < midi }
                BlackKey(
                    midi = midi,
                    selected = midi in selectedNotes,
                    onTap = onKeyTap,
                    offsetX = WHITE_KEY_WIDTH * whitesBelow - BLACK_KEY_WIDTH / 2
                )
            }
        }
    }
}

@Composable
private fun WhiteKey(
    midi: Int,
    selected: Boolean,
    onTap: (Int) -> Unit
) {
    val view = LocalView.current
    val label = Note.label(midi)
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else KeyIvory,
        label = "whiteKeyColor"
    )

    Box(
        modifier = Modifier
            .width(WHITE_KEY_WIDTH)
            .fillMaxHeight()
            .padding(horizontal = 1.dp)
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(color)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTap(midi)
            }
            .semantics {
                contentDescription = if (selected) "Remove $label from chord" else "Add $label to chord"
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        val showLabel = selected || label.startsWith("C")
        if (showLabel) {
            Text(
                text = label,
                modifier = Modifier.padding(bottom = 10.dp),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else Color(0xFF8A7C7C),
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BoxScope.BlackKey(
    midi: Int,
    selected: Boolean,
    onTap: (Int) -> Unit,
    offsetX: Dp
) {
    val view = LocalView.current
    val label = Note.label(midi)
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else KeyEbony,
        label = "blackKeyColor"
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .width(BLACK_KEY_WIDTH)
            .fillMaxHeight(BLACK_KEY_HEIGHT_FRACTION)
            .align(Alignment.TopStart)
            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
            .background(color)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTap(midi)
            }
            .semantics {
                contentDescription = if (selected) "Remove $label from chord" else "Add $label to chord"
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (selected) {
            Text(
                text = label,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
