package com.example.mindmoving.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mindmoving.R

val interFamily = FontFamily(
    Font(R.font.inter_24pt_black, FontWeight.Normal),
    Font(R.font.inter_24pt_bold, FontWeight.Bold),
    Font(R.font.inter_24pt_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_18pt_blackitalic, FontWeight.Light)
)

val orbitronFamily = FontFamily(
    Font(R.font.orbitron_black, FontWeight.Normal),
    Font(R.font.orbitron_bold, FontWeight.Bold),
    Font(R.font.orbitron_extrabold, FontWeight.ExtraBold),
    Font(R.font.orbitron_semibold, FontWeight.Bold),
    Font(R.font.orbitron_regular, FontWeight.Normal),

    )

// Set of Material typography styles to start with
val Typography = Typography(
    bodySmall = TextStyle(
        fontFamily = interFamily,
        fontWeight = FontWeight.Thin,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = orbitronFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = orbitronFamily,
        fontWeight = FontWeight.Thin,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

/* Other default text styles to override
titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
),
labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)
*/
)