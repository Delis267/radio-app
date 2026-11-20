package com.iu.radio_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Preview
@Composable
private fun HeroWithLinePreview() {
    RadioappTheme {
        HeroContainer {
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
