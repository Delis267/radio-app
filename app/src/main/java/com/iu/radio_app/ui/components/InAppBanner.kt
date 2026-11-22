package com.iu.radio_app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iu.radio_app.R
import com.iu.radio_app.ui.theme.RadioappTheme

/** Hinweis oben in der App, wie eine Push-Nachricht, solange die App offen ist. */
@Composable
fun InAppBanner(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.small,
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
            Icon(painterResource(R.drawable.ic_heart), contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDismiss) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.in_app_banner_close))
            }
        }
    }
}

@Preview
@Composable
private fun InAppBannerPreview() {
    RadioappTheme {
        InAppBanner(title = "Dein Songwunsch läuft gerade", text = "Queen – Bohemian Rhapsody", onDismiss = {})
    }
}
