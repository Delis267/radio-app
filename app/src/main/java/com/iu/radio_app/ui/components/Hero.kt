package com.iu.radio_app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iu.radio_app.R
import com.iu.radio_app.ui.theme.RadioappTheme

/** Dunkler Kopfbereich. Definiert nur grobes Layout, Inhalt kommt aus dem jeweiligen Screen */
@Composable
fun HeroContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.inverseSurface,
        contentColor = colors.inverseOnSurface,
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 35.dp), content = content)
    }
}

/** Große Überschrift im [HeroContainer], optional mit kurzem Strich darunter. */
@Composable
fun HeroTitle(
    title: String,
    modifier: Modifier = Modifier,
    showLine: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(title, color = Color.White, style = MaterialTheme.typography.headlineLarge)
        if (showLine) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.size(width = 32.dp, height = 3.dp).background(MaterialTheme.colorScheme.inversePrimary))
        }
    }
}

/** Markierung über dem [HeroTitle] mit pulsierendem Punkt. Default ohne [text]: Live */
@Composable
fun LiveTag(
    modifier: Modifier = Modifier,
    text: String? = null,
) {
    val color = MaterialTheme.colorScheme.inversePrimary
    val dotAlpha by rememberInfiniteTransition(label = "LiveTag").animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                keyframes {
                    durationMillis = 2520
                    1f at 600 using FastOutSlowInEasing
                    0f at 1560 using FastOutSlowInEasing
                },
            ),
        label = "dotAlpha",
    )
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // graphicsLayer: Alpha wird nur beim Zeichnen gelesen, die Animation löst keine Recomposition aus
        Box(Modifier.size(6.dp).graphicsLayer { alpha = dotAlpha }.background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            (text ?: stringResource(R.string.live_tag_default)).uppercase(),
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp),
        )
    }
}

@Preview
@Composable
private fun HeroWithLinePreview() {
    RadioappTheme {
        HeroContainer {
            LiveTag()
            Spacer(Modifier.height(12.dp))
            HeroTitle("Elise\nMüller", showLine = true)
            Spacer(Modifier.height(14.dp))
            Text("Moderation", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
private fun HeroWithoutLinePreview() {
    RadioappTheme {
        HeroContainer { HeroTitle("Dein Songwunsch\nwurde eingereicht") }
    }
}

@Preview
@Composable
private fun LiveTagWithTextPreview() {
    RadioappTheme {
        HeroContainer { LiveTag(text = "Aktuelle Sendung") }
    }
}
