package com.iu.radio_app.ui.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.iu.radio_app.R

/** Platzhalter, bis der Bildschirm „Jetzt läuft“ umgesetzt ist. */
@Composable
fun NowPlayingRoute(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.now_playing_title), style = MaterialTheme.typography.headlineMedium)
    }
}
