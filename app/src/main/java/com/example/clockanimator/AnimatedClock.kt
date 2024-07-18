package com.example.clockanimator


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimatedClock() {
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var secondRotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            dateTime = LocalDateTime.now()
            secondRotation = (dateTime.second * 6).toFloat() // 360 / 60 = 6 degrees per second
            delay(1000L)
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("EEEE dd")
    val currentDate = dateTime.format(timeFormatter)
    val currentTime = dateTime

    Column(
        modifier = Modifier
            .fillMaxSize()

            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentDate,
            style = TextStyle(color = Color(0xFFD14572), fontSize = 24.sp)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
            WavesAnimation()
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawClock(secondRotation, currentTime)
                }
            }
        }
    }
}

@Composable
fun WavesAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val wave1Phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        )
    )

    val wave2Phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        )
    )

    val wave3Phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing)
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val waveRadius = size.minDimension / 2
        val colors = listOf(
            Color(0xFFD14572).copy(alpha = 0.4f),
            Color(0xFF1598D8).copy(alpha = 0.4f),
            Color(0xFF52D18F).copy(alpha = 0.4f)
        )

        drawWave(center, waveRadius + 20, wave1Phase, colors[0])
        drawWave(center, waveRadius + 40, wave2Phase, colors[1])
        drawWave(center, waveRadius + 60, wave3Phase, colors[2])
    }
}

fun DrawScope.drawWave(center: Offset, baseRadius: Float, phase: Float, color: Color) {
    val wavePath = Path().apply {
        val amplitude = 10.dp.toPx()
        val frequency = 4

        for (angle in 0..360 step 5) {
            val radian = Math.toRadians((angle + phase).toDouble())
            val radiusVariation = (sin(radian * frequency) * amplitude).toFloat()
            val radius = baseRadius + radiusVariation
            val x = (center.x + cos(Math.toRadians(angle.toDouble())) * radius).toFloat()
            val y = (center.y + sin(Math.toRadians(angle.toDouble())) * radius).toFloat()
            if (angle == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }

    drawPath(
        path = wavePath,
        color = color,
        style = Stroke(width = 3.dp.toPx())
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun DrawScope.drawClock(seconds: Float, time: LocalDateTime) {
    val radius = size.minDimension / 2
    val center = Offset(size.width / 2, size.height / 2)
    val secondHandLength = radius * 0.9f
    val minuteHandLength = radius * 0.7f
    val hourHandLength = radius * 0.5f

    // Draw the circular gradient border with shadow
    val gradientColors = listOf(Color(0xFFD14572), Color(0xFF1598D8))
    drawCircle(
        brush = Brush.sweepGradient(gradientColors),
        radius = radius,
        center = center,
        style = Stroke(width = 4.dp.toPx())
    )
    drawCircle(
        color = Color.Gray,
        radius = radius - 4.dp.toPx(),
        center = center,
        style = Stroke(width = 8.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), phase = 0f))
    )

    // Draw hour marks
    for (i in 0 until 12) {
        val angle = Math.toRadians(i * 30.0 - 90)
        val startOffset = Offset(
            (center.x + cos(angle) * (radius * 0.8f)).toFloat(),
            (center.y + sin(angle) * (radius * 0.8f)).toFloat()
        )
        val endOffset = Offset(
            (center.x + cos(angle) * radius).toFloat(),
            (center.y + sin(angle) * radius).toFloat()
        )
        drawLine(
            color = Color(0xFFD14572),
            start = startOffset,
            end = endOffset,
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Calculate hand positions
    val secondAngle = Math.toRadians(seconds.toDouble() - 90)
    val secondHandEnd = Offset(
        (center.x + cos(secondAngle) * secondHandLength).toFloat(),
        (center.y + sin(secondAngle) * secondHandLength).toFloat()
    )

    val minute = time.minute
    val minuteAngle = Math.toRadians((minute * 6).toDouble() - 90)
    val minuteHandEnd = Offset(
        (center.x + cos(minuteAngle) * minuteHandLength).toFloat(),
        (center.y + sin(minuteAngle) * minuteHandLength).toFloat()
    )

    val hour = time.hour % 12
    val hourAngle = Math.toRadians((hour * 30 + minute * 0.5).toDouble() - 90)
    val hourHandEnd = Offset(
        (center.x + cos(hourAngle) * hourHandLength).toFloat(),
        (center.y + sin(hourAngle) * hourHandLength).toFloat()
    )

    // Draw hands with shadows
    drawLine(
        color = Color(0xFFD14572),
        start = center,
        end = hourHandEnd,
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF1598D8),
        start = center,
        end = minuteHandEnd,
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF52D18F),
        start = center,
        end = secondHandEnd,
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Draw central circle
    drawCircle(
        color = Color(0xFFD14572),
        radius = 8.dp.toPx(),
        center = center
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewAnimatedClock() {
    AnimatedClock()
}