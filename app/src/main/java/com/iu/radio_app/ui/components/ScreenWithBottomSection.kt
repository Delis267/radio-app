package com.iu.radio_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iu.radio_app.ui.theme.RadioappTheme

/**
 * Scrollender Screen mit beigem Bereich am Ende. Ist [content] kürzer als der Bildschirm,
 * klebt der Bereich unten, damit kein Spalt zur Navigationsleiste entsteht.
 * Scrollt selbst, daher nicht in einen vertikal scrollenden Container setzen (Absturz).
 */
@Composable
fun ScreenWithBottomSection(
    bottom: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        // Mindesthöhe = sichtbarer Bereich, sonst hat weight in einer scrollenden Spalte keine Wirkung
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight)) {
            content()
            Spacer(Modifier.weight(1f))
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 20.dp, vertical = 25.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
                content = bottom,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun ScreenWithBottomSectionPreview() {
    RadioappTheme {
        ScreenWithBottomSection(
            bottom = {
                Text("Du möchtest etwas anderes hören?", style = MaterialTheme.typography.titleMedium)
                Text("Reiche uns deinen Songwunsch ein!", style = MaterialTheme.typography.bodyMedium)
                PrimaryButton(text = "Song wünschen", onClick = {}, modifier = Modifier.fillMaxWidth())
            },
        ) {
            Text("Kurzer Inhalt", modifier = Modifier.padding(20.dp))
        }
    }
}
