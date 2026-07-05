package com.elendheim.chords.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.elendheim.chords.model.Note
import kotlin.math.roundToInt

private const val ROLL_LOW_MIDI = 36
private const val ROLL_HIGH_MIDI = 96
private val CELL_WIDTH = 52.dp
private val CELL_SPACING = 6.dp

/**
 * A tiny, zoomed-out piano roll: one column per bar, one thin block per note,
 * placed vertically by pitch. Long-press a bar and drag sideways to reorder.
 */
@Composable
fun PianoRoll(
    bars: List<List<Int>>,
    selectedBar: Int?,
    onBarTap: (Int) -> Unit,
    onMoveBar: (Int, Int) -> Unit,
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

    val view = LocalView.current
    val cellStridePx = with(LocalDensity.current) { (CELL_WIDTH + CELL_SPACING).toPx() }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    fun targetOf(from: Int): Int =
        (from + (dragOffset / cellStridePx).roundToInt()).coerceIn(0, bars.lastIndex)

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(CELL_SPACING)
    ) {
        bars.forEachIndexed { index, bar ->
            val dragging = draggingIndex
            val isDragged = index == dragging
            // While a neighbour is being dragged past this cell, slide out of its way.
            val shift = if (dragging == null || isDragged) {
                0f
            } else {
                val target = targetOf(dragging)
                when {
                    dragging < index && index <= target -> -cellStridePx
                    target <= index && index < dragging -> cellStridePx
                    else -> 0f
                }
            }
            val animatedShift by animateFloatAsState(shift, label = "barShift")

            BarCell(
                index = index,
                bar = bar,
                selected = index == selectedBar,
                noteColor = MaterialTheme.colorScheme.primary,
                onTap = onBarTap,
                modifier = Modifier
                    .zIndex(if (isDragged) 1f else 0f)
                    .graphicsLayer {
                        translationX = if (isDragged) dragOffset else animatedShift
                    }
                    .pointerInput(index, bars.size) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                draggingIndex = index
                                dragOffset = 0f
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                dragOffset += amount.x
                            },
                            onDragEnd = {
                                draggingIndex?.let { from ->
                                    val to = targetOf(from)
                                    if (to != from) onMoveBar(from, to)
                                }
                                draggingIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggingIndex = null
                                dragOffset = 0f
                            }
                        )
                    }
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
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val label = Note.chordLabel(bar)

    Box(
        modifier = modifier
            .width(CELL_WIDTH)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable { onTap(index) }
            .semantics {
                contentDescription = "Bar ${index + 1}: $label. Long press and drag to move."
            }
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
