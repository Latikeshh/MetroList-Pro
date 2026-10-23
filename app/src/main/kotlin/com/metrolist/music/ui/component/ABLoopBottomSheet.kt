/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.utils.formatPreciseTime
import kotlinx.coroutines.delay

/**
 * Professional A-B Section Loop Editor interface providing real-time timeline visualization,
 * exact position capture, and millisecond-accurate fine adjustment controls.
 *
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ABLoopBottomSheet(
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val player = playerConnection.player

    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val pointA by playerConnection.abLoopPointA.collectAsStateWithLifecycle()
    val pointB by playerConnection.abLoopPointB.collectAsStateWithLifecycle()
    val isEnabled by playerConnection.isAbLoopEnabled.collectAsStateWithLifecycle()

    var currentPosition by remember { mutableStateOf(player.currentPosition) }
    val duration = remember(player.duration) { if (player.duration > 0) player.duration else 180000L }

    // Live position ticker for smooth timeline updates
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = player.currentPosition
            delay(100L)
        }
    }

    val effectiveA = pointA ?: 0L
    val effectiveB = pointB ?: duration
    val isInvalidRange = pointA != null && pointB != null && pointA!! >= pointB!!

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Section A: Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.repeat_one),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp),
                )
                Text(
                    text = stringResource(R.string.ab_looper),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = stringResource(if (isEnabled) R.string.ab_loop_active else R.string.ab_loop_off),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 6.dp),
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { playerConnection.setAbLoopEnabled(it) },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section B: Player & Transport Bar
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledIconButton(
                        onClick = { playerConnection.togglePlayPause() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${formatPreciseTime(currentPosition)} / ${formatPreciseTime(duration)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Playback Position",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Timeline Canvas
                ABLoopTimeline(
                    currentPosition = currentPosition,
                    duration = duration,
                    pointA = pointA,
                    pointB = pointB,
                    onSeek = { targetMs ->
                        player.seekTo(targetMs)
                        currentPosition = targetMs
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Dual Range Slider for Boundary Adjustment
                RangeSlider(
                    value = effectiveA.toFloat()..effectiveB.toFloat(),
                    onValueChange = { range ->
                        val newA = range.start.toLong()
                        val newB = range.endInclusive.toLong()
                        playerConnection.setAbLoopPointA(newA)
                        playerConnection.setAbLoopPointB(newB)
                    },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Validation Error Banner if Start >= End
        if (isInvalidRange) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "⚠️ Start Point A must be before End Point B",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Section C: Point A and Point B Selection Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Point A Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.point_a),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (pointA != null) formatPreciseTime(pointA) else "Unset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = { playerConnection.setAbLoopPointA(player.currentPosition) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.set_point_a), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Point B Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.point_b),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (pointB != null) formatPreciseTime(pointB) else "Unset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = { playerConnection.setAbLoopPointB(player.currentPosition) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.set_point_b), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Section D: Fine Adjustments
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.point_a) + " Fine Adjust",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NudgeChip("-1s") { playerConnection.setAbLoopPointA((effectiveA - 1000L).coerceAtLeast(0L)) }
                NudgeChip("-0.1s") { playerConnection.setAbLoopPointA((effectiveA - 100L).coerceAtLeast(0L)) }
                NudgeChip("+0.1s") { playerConnection.setAbLoopPointA((effectiveA + 100L).coerceAtMost(effectiveB - 100L)) }
                NudgeChip("+1s") { playerConnection.setAbLoopPointA((effectiveA + 1000L).coerceAtMost(effectiveB - 1000L)) }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.point_b) + " Fine Adjust",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                NudgeChip("-1s") { playerConnection.setAbLoopPointB((effectiveB - 1000L).coerceAtLeast(effectiveA + 1000L)) }
                NudgeChip("-0.1s") { playerConnection.setAbLoopPointB((effectiveB - 100L).coerceAtLeast(effectiveA + 100L)) }
                NudgeChip("+0.1s") { playerConnection.setAbLoopPointB((effectiveB + 100L).coerceAtMost(duration)) }
                NudgeChip("+1s") { playerConnection.setAbLoopPointB((effectiveB + 1000L).coerceAtMost(duration)) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section E: Clear Selection / Reset Button
        Button(
            onClick = {
                playerConnection.clearAbLoop()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(stringResource(R.string.clear_ab_loop), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Interactive Timeline Canvas Component rendering Point A, Point B, highlighted loop band, and playhead.
 */
@Composable
private fun ABLoopTimeline(
    currentPosition: Long,
    duration: Long,
    pointA: Long?,
    pointB: Long?,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeDuration = if (duration > 0) duration else 180000L
    val effectiveA = pointA ?: 0L
    val effectiveB = pointB ?: safeDuration

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val trackY = heightPx / 2f

        val posFraction = (currentPosition.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
        val aFraction = (effectiveA.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
        val bFraction = (effectiveB.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

        val posX = widthPx * posFraction
        val aX = widthPx * aFraction
        val bX = widthPx * bFraction

        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(safeDuration, widthPx) {
                    detectTapGestures { offset ->
                        val tapFraction = (offset.x / widthPx).coerceIn(0f, 1f)
                        val targetMs = (tapFraction * safeDuration).toLong()
                        onSeek(targetMs)
                    }
                }
        ) {
            // 1. Draw Inactive Background Track
            drawLine(
                color = trackColor,
                start = Offset(0f, trackY),
                end = Offset(widthPx, trackY),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round,
            )

            // 2. Draw Highlighted Loop Range Band (between A and B)
            if (pointA != null || pointB != null) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.45f),
                    start = Offset(aX, trackY),
                    end = Offset(bX, trackY),
                    strokeWidth = 12.dp.toPx(),
                    cap = StrokeCap.Square,
                )
            }

            // 3. Draw Marker A Pin (Start Boundary)
            if (pointA != null) {
                drawLine(
                    color = primaryColor,
                    start = Offset(aX, trackY - 16.dp.toPx()),
                    end = Offset(aX, trackY + 16.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = primaryColor,
                    center = Offset(aX, trackY - 16.dp.toPx()),
                    radius = 6.dp.toPx(),
                )
            }

            // 4. Draw Marker B Pin (End Boundary)
            if (pointB != null) {
                drawLine(
                    color = secondaryColor,
                    start = Offset(bX, trackY - 16.dp.toPx()),
                    end = Offset(bX, trackY + 16.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = secondaryColor,
                    center = Offset(bX, trackY + 16.dp.toPx()),
                    radius = 6.dp.toPx(),
                )
            }

            // 5. Draw Playhead (Current Playback Position)
            drawLine(
                color = onSurfaceColor,
                start = Offset(posX, trackY - 10.dp.toPx()),
                end = Offset(posX, trackY + 10.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = onSurfaceColor,
                center = Offset(posX, trackY),
                radius = 5.dp.toPx(),
            )
        }
    }
}

@Composable
private fun NudgeChip(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier.height(36.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}
