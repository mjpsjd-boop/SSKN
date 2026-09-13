package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SSKNEmblem(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MidnightNavy, SecondaryNavy, Color(0xFF1E293B)),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height

            // Open book base - Left page
            val leftPage = Path().apply {
                moveTo(w * 0.12f, h * 0.76f)
                quadraticBezierTo(w * 0.28f, h * 0.68f, w * 0.48f, h * 0.74f)
                lineTo(w * 0.48f, h * 0.84f)
                quadraticBezierTo(w * 0.28f, h * 0.78f, w * 0.12f, h * 0.86f)
                close()
            }
            drawPath(leftPage, color = PrimaryBlue)

            // Open book base - Right page
            val rightPage = Path().apply {
                moveTo(w * 0.88f, h * 0.76f)
                quadraticBezierTo(w * 0.72f, h * 0.68f, w * 0.52f, h * 0.74f)
                lineTo(w * 0.52f, h * 0.84f)
                quadraticBezierTo(w * 0.72f, h * 0.78f, w * 0.88f, h * 0.86f)
                close()
            }
            drawPath(rightPage, color = BrightBlue)

            // Dynamic 'S' spine and curve
            val sPath = Path().apply {
                // Top curve of S
                moveTo(w * 0.70f, h * 0.22f)
                cubicTo(w * 0.55f, h * 0.12f, w * 0.32f, h * 0.16f, w * 0.28f, h * 0.32f)
                cubicTo(w * 0.24f, h * 0.46f, w * 0.52f, h * 0.48f, w * 0.62f, h * 0.54f)
                cubicTo(w * 0.76f, h * 0.62f, w * 0.66f, h * 0.80f, w * 0.34f, h * 0.76f)
            }
            drawPath(
                sPath,
                color = Color.White,
                style = Stroke(width = w * 0.11f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Subtle Biology leaf accent budding at top right of S
            val leafPath = Path().apply {
                moveTo(w * 0.65f, h * 0.18f)
                quadraticBezierTo(w * 0.80f, h * 0.12f, w * 0.78f, h * 0.28f)
                quadraticBezierTo(w * 0.68f, h * 0.26f, w * 0.65f, h * 0.18f)
                close()
            }
            drawPath(leafPath, color = Color(0xFF38BDF8))
        }
    }
}

@Composable
fun SSKNLogo(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 38.dp,
    showTagline: Boolean = false,
    showFullName: Boolean = true,
    isDarkBackground: Boolean = false
) {
    Row(
        modifier = modifier.testTag("sskn_logo_component"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SSKNEmblem(size = emblemSize)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SSKN",
                    fontSize = if (emblemSize > 40.dp) 22.sp else 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.2.sp,
                    color = if (isDarkBackground) Color.White else PrimaryText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDarkBackground) PrimaryBlue.copy(alpha = 0.3f) else VeryLightBlue)
                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = "NEET BIO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightBlue,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            if (showFullName) {
                Text(
                    text = "Saleem Sir Ki NCERT",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkBackground) Color(0xFF94A3B8) else SecondaryText
                )
            }

            if (showTagline) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Read NCERT. Master Every Page. Crack NEET.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isDarkBackground) SoftBlue else PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun SSKNFullBrandCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MidnightNavy, SecondaryNavy, Color(0xFF0F172A))
                )
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SSKNEmblem(size = 52.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SSKN",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OFFICIAL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Text(
                        text = "Saleem Sir Ki NCERT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SoftBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF1E293B))
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Read NCERT. Master Every Page. Crack NEET.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0)
                )
            }
        }
    }
}
