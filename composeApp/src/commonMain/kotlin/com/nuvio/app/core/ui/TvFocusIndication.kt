package com.nuvio.app.core.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * TV support (v1): app-wide indication that makes remote/D-pad focus visible.
 *
 * The app was built for touch, so clickable components were focusable but showed
 * nothing when focused — a TV remote user could not see the current selection.
 * Providing this as the [androidx.compose.foundation.LocalIndication] means every
 * default `clickable` / `combinedClickable` draws a clear white + accent border
 * while focused, and a light overlay while pressed (touch feedback on phones).
 */
object TvFocusIndication : Indication {

    private val FocusAccent = Color(0xFF8B5CF6)

    override suspend fun CoroutineScope.createInstance(
        interactionSource: InteractionSource,
    ): IndicationInstance {
        val focusedState = mutableStateOf(false)
        val pressedState = mutableStateOf(false)
        launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> focusedState.value = true
                    is FocusInteraction.Unfocus, is FocusInteraction.Cancel -> focusedState.value = false
                    is PressInteraction.Press -> pressedState.value = true
                    is PressInteraction.Release, is PressInteraction.Cancel -> pressedState.value = false
                }
            }
        }
        return TvFocusIndicationInstance(focusedState, pressedState)
    }

    private class TvFocusIndicationInstance(
        private val focusedState: State<Boolean>,
        private val pressedState: State<Boolean>,
    ) : IndicationInstance {
        override fun ContentDrawScope.drawIndication() {
            drawContent()
            val radius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            if (pressedState.value) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.14f),
                    cornerRadius = radius,
                )
            }
            if (focusedState.value) {
                val strokeWidth = 3.dp.toPx()
                val inset = strokeWidth / 2
                val innerSize = Size(
                    width = (size.width - strokeWidth).coerceAtLeast(0f),
                    height = (size.height - strokeWidth).coerceAtLeast(0f),
                )
                // Soft accent glow just under the main border.
                drawRoundRect(
                    color = FocusAccent.copy(alpha = 0.30f),
                    topLeft = Offset(inset, inset),
                    size = innerSize,
                    cornerRadius = radius,
                    style = Stroke(width = strokeWidth * 2),
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(inset, inset),
                    size = innerSize,
                    cornerRadius = radius,
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    }
}
