package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// Theme Gradients
object ThemeGradients {
    val CherryPeach = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800))
    )
    val SaffronCream = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF9800), Color(0xFFFFEB3B))
    )
    val RoyalPurple = Brush.horizontalGradient(
        colors = listOf(Color(0xFF673AB7), Color(0xFFE91E63))
    )
    val EmeraldMint = Brush.horizontalGradient(
        colors = listOf(Color(0xFF009688), Color(0xFF4CAF50))
    )
    val OceanBlue = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2196F3), Color(0xFF00BCD4))
    )
}

// Custom Styled Card
@Composable
fun StylizedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

// Gorgeous Dashboard Stat Card
@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    StylizedCard(
        modifier = modifier.height(130.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Standard Button with Test Tag
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "primary_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// Custom Outline input fields with testTag
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylizedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    testTag: String = "styled_text_field",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

// Interactive custom Canvas Line Trend Graph
@Composable
fun InteractiveTrendGraph(
    points: List<Double>,
    labels: List<String>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val maxVal = (points.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val spacing = width / (points.size - 1).coerceAtLeast(1)

            // Draw Grid Lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height - (i * (height / gridLines))
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Generate path
            val path = Path()
            points.forEachIndexed { idx, valPoint ->
                val x = idx * spacing
                val ratio = valPoint / maxVal
                val y = height - (ratio * height * animatedProgress.value).toFloat()

                if (idx == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Draw line
            drawPath(
                path = path,
                color = Color(0xFFFF9800),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw area under line
            val fillPath = Path().apply {
                addPath(path)
                lineTo((points.size - 1) * spacing, height)
                lineTo(0f, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF9800).copy(alpha = 0.3f), Color.Transparent)
                )
            )

            // Draw points
            points.forEachIndexed { idx, valPoint ->
                val x = idx * spacing
                val ratio = valPoint / maxVal
                val y = height - (ratio * height * animatedProgress.value).toFloat()

                drawCircle(
                    color = Color(0xFFE91E63),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

// Custom Pie / Donut Chart for Expense distribution
@Composable
fun DonutChart(
    slices: List<Pair<String, Double>>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val totalVal = slices.sumOf { it.second }.coerceAtLeast(1.0)
    val colors = listOf(
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFF4CAF50),
        Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFF00BCD4)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .size(150.dp)
                .weight(1f)
        ) {
            var startAngle = 0f
            slices.forEachIndexed { idx, slice ->
                val angle = ((slice.second / totalVal) * 360f).toFloat()
                drawArc(
                    color = colors[idx % colors.size],
                    startAngle = startAngle,
                    sweepAngle = angle,
                    useCenter = false,
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
                )
                startAngle += angle
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.Center
        ) {
            slices.take(4).forEachIndexed { idx, slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors[idx % colors.size])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${slice.first}: $currencySymbol${slice.second.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
