package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.R

@Composable
fun SSKNEmblem(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.sskn_logo),
        contentDescription = "SSKN NEET Biology",
        modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.28f)),
        contentScale = ContentScale.Crop
    )
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
