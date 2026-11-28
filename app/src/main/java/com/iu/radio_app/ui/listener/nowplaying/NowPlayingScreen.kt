package com.iu.radio_app.ui.listener.nowplaying

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iu.radio_app.R
import com.iu.radio_app.model.Track
import com.iu.radio_app.ui.components.CoverImage
import com.iu.radio_app.ui.components.HeroContainer
import com.iu.radio_app.ui.components.HeroTitle
import com.iu.radio_app.ui.components.LiveTag
import com.iu.radio_app.ui.components.PrimaryButton
import com.iu.radio_app.ui.components.ScreenWithBottomSection
import com.iu.radio_app.ui.components.StarRating
import com.iu.radio_app.ui.theme.RadioappTheme

/** Verbindet den Bildschirm „Jetzt läuft“ mit dem ViewModel und zeigt dessen Rückmeldungen an. */
@Composable
fun NowPlayingRoute(
    modifier: Modifier = Modifier,
    onNavigateToSongRequest: () -> Unit = {},
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }

    NowPlayingScreen(
        state = state,
        onRate = viewModel::onRate,
        onSubmitRating = viewModel::submitRating,
        onSongRequestClick = onNavigateToSongRequest,
        modifier = modifier,
    )
}

/** Zustandslos: zeigt nur [state] an und meldet Eingaben nach oben. */
@Composable
fun NowPlayingScreen(
    state: NowPlayingUiState,
    onRate: (Int) -> Unit,
    onSubmitRating: () -> Unit,
    onSongRequestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithBottomSection(
        bottom = {
            Text(
                text = stringResource(R.string.now_playing_bottom_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.now_playing_bottom_subtitle),
                style = MaterialTheme.typography.bodyMedium,
            )
            PrimaryButton(
                text = stringResource(R.string.now_playing_bottom_button),
                onClick = onSongRequestClick,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        modifier = modifier,
    ) {
        state.playedTrack?.track?.let { track ->
            NowPlayingHeader(track = track)
        } ?: EmptyHeader()

        SongRatingSection(
            state = state,
            onRate = onRate,
            onSubmitRating = onSubmitRating,
        )
    }
}

@Composable
private fun NowPlayingHeader(
    track: Track,
    modifier: Modifier = Modifier,
) {
    HeroContainer(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
            ) {
                LiveTag(text = stringResource(R.string.now_playing_live_tag))
                Spacer(Modifier.height(12.dp))
                HeroTitle(title = track.title, showLine = true)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                val albumInfo =
                    listOfNotNull(
                        track.album?.let { stringResource(R.string.now_playing_album_label, it) },
                        track.releaseYear?.toString(),
                    ).joinToString(" | ")
                if (albumInfo.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = albumInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
            CoverImage(track.coverUrl, modifier = Modifier.padding(top = 12.dp).size(130.dp), iconSize = 48.dp)
        }
    }
}

@Composable
private fun EmptyHeader(modifier: Modifier = Modifier) {
    HeroContainer(modifier = modifier) {
        LiveTag(text = stringResource(R.string.now_playing_live_tag))
        Spacer(Modifier.height(12.dp))
        HeroTitle(title = stringResource(R.string.now_playing_no_track), showLine = true)
    }
}


@Composable
private fun SongRatingSection(
    state: NowPlayingUiState,
    onRate: (Int) -> Unit,
    onSubmitRating: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 32.dp),
    ) {
        Text(
            text = stringResource(R.string.now_playing_current_song),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.now_playing_rate_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                StarRating(
                    rating = state.rating,
                    onRate = onRate,
                )
                PrimaryButton(
                    text = stringResource(R.string.now_playing_rate_submit),
                    onClick = onSubmitRating,
                    enabled = state.rating != null && state.playedTrack != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
