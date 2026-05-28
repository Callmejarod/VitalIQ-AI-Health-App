package com.vitaliq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitaliq.app.ui.theme.VitalColors

@Composable
fun LineChart(
    points: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = VitalColors.accent,
    dotColor: Color = VitalColors.accent
) {
    if (points.isEmpty()) return

    val minY = points.minOf { it.second }
    val maxY = points.maxOf { it.second }
    val yRange = if (maxY - minY < 0.001f) 1f else maxY - minY

    Box(modifier = modifier.height(180.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingTop = 24.dp.toPx()
            val paddingBottom = 32.dp.toPx()
            val paddingHorizontal = 48.dp.toPx()
            val chartWidth = size.width - paddingHorizontal * 2
            val chartHeight = size.height - paddingTop - paddingBottom
            val n = points.size

            if (n < 2) return@Canvas

            fun xAt(i: Int) = paddingHorizontal + i * (chartWidth / (n - 1))
            fun yAt(v: Float) = paddingTop + chartHeight - ((v - minY) / yRange) * chartHeight

            // Grid lines (3 horizontal)
            repeat(3) { i ->
                val y = paddingTop + (i / 2f) * chartHeight
                drawLine(
                    color = VitalColors.border,
                    start = Offset(paddingHorizontal, y),
                    end = Offset(size.width - paddingHorizontal, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Line path
            val path = Path().apply {
                moveTo(xAt(0), yAt(points[0].second))
                for (i in 1 until n) {
                    lineTo(xAt(i), yAt(points[i].second))
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Dots
            for (i in points.indices) {
                drawCircle(
                    color = dotColor,
                    radius = 4.dp.toPx(),
                    center = Offset(xAt(i), yAt(points[i].second))
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(xAt(i), yAt(points[i].second))
                )
            }
        }

        // Y-axis labels
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .padding(top = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = maxY.toInt().toString(),
                    fontSize = 10.sp,
                    color = VitalColors.textMuted
                )
                Text(
                    text = minY.toInt().toString(),
                    fontSize = 10.sp,
                    color = VitalColors.textMuted
                )
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 44.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = maxOf(1, points.size / 5)
            points.forEachIndexed { i, (label, _) ->
                if (i % step == 0 || i == points.lastIndex) {
                    Text(
                        text = label.take(5),
                        fontSize = 9.sp,
                        color = VitalColors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
fun DualLineChart(
    series1: List<Pair<String, Float>>,
    series2: List<Pair<String, Float>>,
    color1: Color = VitalColors.accent,
    color2: Color = Color(0xFFFFB347),
    modifier: Modifier = Modifier
) {
    val allValues = series1.map { it.second } + series2.map { it.second }
    if (allValues.isEmpty()) return
    val minY = allValues.min()
    val maxY = allValues.max()
    val yRange = if (maxY - minY < 0.001f) 1f else maxY - minY
    val n = maxOf(series1.size, series2.size)

    Box(modifier = modifier.height(180.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingTop = 24.dp.toPx()
            val paddingBottom = 32.dp.toPx()
            val paddingHorizontal = 48.dp.toPx()
            val chartWidth = size.width - paddingHorizontal * 2
            val chartHeight = size.height - paddingTop - paddingBottom

            fun xAt(i: Int, total: Int) = if (total <= 1) paddingHorizontal
            else paddingHorizontal + i * (chartWidth / (total - 1))
            fun yAt(v: Float) = paddingTop + chartHeight - ((v - minY) / yRange) * chartHeight

            // Grid
            repeat(3) { i ->
                val y = paddingTop + (i / 2f) * chartHeight
                drawLine(
                    color = VitalColors.border,
                    start = Offset(paddingHorizontal, y),
                    end = Offset(size.width - paddingHorizontal, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            listOf(series1 to color1, series2 to color2).forEach { (pts, col) ->
                if (pts.size >= 2) {
                    val path = Path().apply {
                        moveTo(xAt(0, pts.size), yAt(pts[0].second))
                        for (i in 1 until pts.size) lineTo(xAt(i, pts.size), yAt(pts[i].second))
                    }
                    drawPath(path, color = col, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    pts.forEachIndexed { i, (_, v) ->
                        drawCircle(col, 4.dp.toPx(), Offset(xAt(i, pts.size), yAt(v)))
                        drawCircle(Color.White, 2.dp.toPx(), Offset(xAt(i, pts.size), yAt(v)))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.width(44.dp).fillMaxHeight().padding(top = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(maxY.toInt().toString(), fontSize = 10.sp, color = VitalColors.textMuted)
                Text(minY.toInt().toString(), fontSize = 10.sp, color = VitalColors.textMuted)
            }
        }
    }
}
