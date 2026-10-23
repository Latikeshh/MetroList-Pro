/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.utils.formatPreciseTime

/**
 * Bottom sheet interface providing professional A-B section looping with dual range sliders
 * and millisecond-accurate nudge controls.
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
    val duration = remember(player.duration) { if (player.duration > 0) player.duration else 180000L }

    val pointA by playerConnection.abLoopPointA.collectAsStateWithLifecycle()
    val pointB by playerConnection.abLoopPointB.collectAsStateWithLifecycle()
    val isEnabled by playerConnection.isAbLoopEnabled.collectAsStateWithLifecycle()

    val effectiveA = pointA ?: 0L
    val effectiveB = pointB ?: duration

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header Row
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
                    style = MaterialTheme.typography.titleMedium,
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

        // Time Indicator Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.point_a),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (pointA != null) formatPreciseTime(pointA) else "Unset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (pointA != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }

            Text(
                text = "➔",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.point_b),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = if (pointB != null) formatPreciseTime(pointB) else "Unset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (pointB != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dual Range Slider
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

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Drop Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilledTonalButton(
                onClick = { playerConnection.setAbLoopPointA(player.currentPosition) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.set_point_a), style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = { playerConnection.setAbLoopPointB(player.currentPosition) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.set_point_b), style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Point A Nudge Controls Row
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

        // Point B Nudge Controls Row
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

        // Clear / Reset Button
        Button(
            onClick = {
                playerConnection.clearAbLoop()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.clear_ab_loop), style = MaterialTheme.typography.labelLarge)
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
        modifier = Modifier.height(32.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}
