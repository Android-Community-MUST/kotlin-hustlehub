package must.kdroiders.hustlehub.ui.auth.presentation.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CyanColor = Color(0xFF00E5FF)
private val TealColor = Color(0xFF00BCD4)
private val BlueColor = Color(0xFF1565C0)
private val RoyalBlue = Color(0xFF1976D2)
private val HandshakeDark = Color(0xFF0D1B3E)

@Composable
fun HustleHubLogo(size: Dp = 90.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val leftGradient = Brush.linearGradient(
            colors = listOf(CyanColor, TealColor, RoyalBlue),
            start = Offset(0f, 0f),
            end = Offset(w * 0.45f, h)
        )
        val rightGradient = Brush.linearGradient(
            colors = listOf(RoyalBlue, BlueColor),
            start = Offset(w * 0.55f, 0f),
            end = Offset(w, h)
        )

        // Left pillar of H
        val leftPillar = Path().apply {
            val left = w * 0.04f
            val right = w * 0.35f
            val top = h * 0.05f
            val crossbarTop = h * 0.42f
            val crossbarBottom = h * 0.62f
            val bottom = h * 0.95f
            val radius = w * 0.10f

            moveTo(left + radius, top)
            lineTo(right - radius, top)
            quadraticTo(right, top, right, top + radius)
            lineTo(right, crossbarTop)
            quadraticTo(right, crossbarTop + radius, right - radius, crossbarTop + radius)
            lineTo(left + radius, crossbarTop + radius)
            quadraticTo(left, crossbarTop + radius, left, crossbarTop + radius * 2)
            lineTo(left, crossbarBottom - radius)
            quadraticTo(left, crossbarBottom, left + radius, crossbarBottom)
            lineTo(right - radius, crossbarBottom)
            quadraticTo(right, crossbarBottom, right, crossbarBottom + radius)
            lineTo(right, bottom - radius)
            quadraticTo(right, bottom, right - radius, bottom)
            lineTo(left + radius, bottom)
            quadraticTo(left, bottom, left, bottom - radius)
            close()
        }
        drawPath(leftPillar, leftGradient)

        // Right pillar of H
        val rightPillar = Path().apply {
            val left = w * 0.65f
            val right = w * 0.96f
            val top = h * 0.05f
            val crossbarTop = h * 0.42f
            val crossbarBottom = h * 0.62f
            val bottom = h * 0.95f
            val radius = w * 0.10f

            moveTo(left + radius, top)
            lineTo(right - radius, top)
            quadraticTo(right, top, right, top + radius)
            lineTo(right, bottom - radius)
            quadraticTo(right, bottom, right - radius, bottom)
            lineTo(left + radius, bottom)
            quadraticTo(left, bottom, left, bottom - radius)
            lineTo(left, crossbarBottom + radius)
            quadraticTo(left, crossbarBottom, left + radius, crossbarBottom)
            lineTo(right - radius, crossbarBottom)
            quadraticTo(right, crossbarBottom, right, crossbarBottom - radius)
            lineTo(right, crossbarTop + radius)
            quadraticTo(right, crossbarTop, right - radius, crossbarTop)
            lineTo(left + radius, crossbarTop)
            quadraticTo(left, crossbarTop, left, crossbarTop - radius)
            lineTo(left, top + radius)
            quadraticTo(left, top, left + radius, top)
            close()
        }
        drawPath(rightPillar, rightGradient)

        // Handshake icon in the crossbar center
        drawHandshake(w, h)
    }
}

private fun DrawScope.drawHandshake(w: Float, h: Float) {
    val cx = w * 0.50f
    val cy = h * 0.52f
    val strokeWidth = w * 0.045f
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Left hand (coming from left pillar)
    val leftHand = Path().apply {
        moveTo(cx - w * 0.22f, cy + h * 0.06f)
        cubicTo(
            cx - w * 0.18f, cy + h * 0.12f,
            cx - w * 0.10f, cy + h * 0.09f,
            cx - w * 0.02f, cy + h * 0.04f
        )
        // fingers
        lineTo(cx + w * 0.04f, cy)
        lineTo(cx + w * 0.01f, cy - h * 0.04f)
        moveTo(cx - w * 0.04f, cy + h * 0.07f)
        lineTo(cx + w * 0.02f, cy + h * 0.01f)
        moveTo(cx - w * 0.08f, cy + h * 0.09f)
        lineTo(cx - w * 0.01f, cy + h * 0.03f)
    }
    drawPath(leftHand, HandshakeDark, style = stroke)

    // Right hand (coming from right pillar)
    val rightHand = Path().apply {
        moveTo(cx + w * 0.22f, cy - h * 0.06f)
        cubicTo(
            cx + w * 0.18f, cy - h * 0.12f,
            cx + w * 0.10f, cy - h * 0.09f,
            cx + w * 0.02f, cy - h * 0.04f
        )
        lineTo(cx - w * 0.04f, cy)
        lineTo(cx - w * 0.01f, cy + h * 0.04f)
        moveTo(cx + w * 0.04f, cy - h * 0.07f)
        lineTo(cx - w * 0.02f, cy - h * 0.01f)
        moveTo(cx + w * 0.08f, cy - h * 0.09f)
        lineTo(cx + w * 0.01f, cy - h * 0.03f)
    }
    drawPath(rightHand, HandshakeDark, style = stroke)
}
