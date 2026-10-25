package com.nuvio.app.core.ui

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val TvFocusAccent = Color(0xFF8B5CF6)

/**
 * TV support (v1): app-wide indication that makes remote/D-pad focus visible.
 *
 * The app was built for touch, so clickable components were focusable but showed
 * nothing when focused — a TV remote user could not see the current selection.
 * Providing this as the [androidx.compose.foundation.LocalIndication] means every
 * default `clickable` / `combinedClickable` draws a clear white + accent border
 * while focused, and a light overlay while pressed (touch feedback on phones).
 *
 * Implemented with the IndicationNodeFactory API: the legacy IndicationInstance
 * API no longer exists in this Compose version.
 */
object TvFocusIndication : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        TvFocusIndicationNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = 7919

    private class TvFocusIndicationNode(
        private val interactionSource: InteractionSource,
    ) : Modifier.Node(), DrawModifierNode {

        private var isFocused = false
        private var isPressed = false

        override fun onAttach() {
            coroutineScope.launch {
                var focusCount = 0
                var pressCount = 0
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is FocusInteraction.Focus -> focusCount++
                        is FocusInteraction.Unfocus -> focusCount--
                        is PressInteraction.Press -> pressCount++
                        is PressInteraction.Release, is PressInteraction.Cancel -> pressCount--
                    }
                    isFocused = focusCount > 0
                    isPressed = pressCount > 0
                }
            }
        }

        override fun ContentDrawScope.draw() {
            drawContent()
            val radius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            if (isPressed) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.14f),
                    cornerRadius = radius,
                )
            }
            if (isFocused) {
                val strokeWidth = 3.dp.toPx()
                val inset = strokeWidth / 2
                val innerSize = Size(
                    width = (size.width - strokeWidth).coerceAtLeast(0f),
                    height = (size.height - strokeWidth).coerceAtLeast(0f),
                )
                // Soft accent glow just under the main border.
                drawRoundRect(
                    color = TvFocusAccent.copy(alpha = 0.30f),
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
