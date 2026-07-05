package com.elendheim.chords.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elendheim.chords.model.Note

private const val ROLL_LOW_MIDI = 36
private const val ROLL_HIGH_MIDI = 96

/**
 * A tiny, zoomed-out piano roll: one column per bar, one thin block per note,
 * placed vertically by pitch.
 */
@Composable
fun PianoRoll(
    bars: List<List<Int>>,
    selectedBar: Int?,
    onBarTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bars.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Tap Set to drop your chord here as bar 1",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        bars.forEachIndexed { index, bar ->
            BarCell(
                index = index,
                bar = bar,
                selected = index == selectedBar,
                noteColor = MaterialTheme.colorScheme.primary,
                onTap = onBarTap
            )
        }
    }
}

@Composable
private fun BarCell(
    index: Int,
    bar: List<Int>,
    selected: Boolean,
    noteColor: Color,
    onTap: (Int) -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val label = Note.chordLabel(bar)

    Box(
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable { onTap(index) }
            .semantics { contentDescription = "Bar ${index + 1}: $label" }
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .padding(start = 7.dp, end = 7.dp, top = 8.dp, bottom = 18.dp)
        ) {
            val blockHeight = 3.dp.toPx()
            val range = (ROLL_HIGH_MIDI - ROLL_LOW_MIDI).toFloat()
            for (midi in bar) {
                val pitch = midi.coerceIn(ROLL_LOW_MIDI, ROLL_HIGH_MIDI)
                val y = (ROLL_HIGH_MIDI - pitch) / range * size.height
                drawRoundRect(
                    color = noteColor,
                    topLeft = Offset(0f, (y - blockHeight / 2).coerceIn(0f, size.height - blockHeight)),
                    size = Size(size.width, blockHeight),
                    cornerRadius = CornerRadius(blockHeight / 2)
                )
            }
        }
        Text(
            text = "${index + 1}",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 3.dp),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
