package com.iu.radio_app.ui.listener.requestplayed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iu.radio_app.R
import com.iu.radio_app.model.Track
import com.iu.radio_app.ui.components.InAppBanner

/** Verbindet den Banner „Dein Songwunsch läuft gerade“ mit dem ViewModel. Liegt über allen Tabs. */
@Composable
fun RequestPlayedRoute(
    modifier: Modifier = Modifier,
    viewModel: RequestPlayedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RequestPlayedBanner(state = state, onDismiss = viewModel::onDismiss, modifier = modifier)
}

/** Zustandslos: schiebt den Banner von oben herein, solange [RequestPlayedUiState.track] gesetzt ist. */
@Composable
fun RequestPlayedBanner(
    state: RequestPlayedUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Letzten Titel merken, damit der Banner beim Hinausschieben nicht leer wird
    var lastTrack by remember { mutableStateOf<Track?>(null) }
    state.track?.let { lastTrack = it }
    AnimatedVisibility(
        visible = state.track != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier,
    ) {
        lastTrack?.let { track ->
            InAppBanner(
                title = stringResource(R.string.notification_request_played_title),
                text = stringResource(R.string.notification_request_played_text, track.artist, track.title),
                onDismiss = onDismiss,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
