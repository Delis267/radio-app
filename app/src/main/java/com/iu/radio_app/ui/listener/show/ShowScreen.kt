
package com.iu.radio_app.ui.listener.show

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iu.radio_app.R
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import com.iu.radio_app.ui.components.CoverImage
import com.iu.radio_app.ui.components.HeroContainer
import com.iu.radio_app.ui.components.HeroTitle
import com.iu.radio_app.ui.components.LiveTag
import com.iu.radio_app.ui.components.PrimaryButton
import com.iu.radio_app.ui.components.StarRating
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShowRoute(
    modifier: Modifier = Modifier,
    viewModel: ShowViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val message = state.message?.let { stringResource(it) }

    LaunchedEffect(message) {
        if (message != null) {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT,
            ).show()
            viewModel.onMessageShown()
        }
    }

    ShowScreen(
        state = state,
        onRate = viewModel::onRate,
        onSubmitRating = viewModel::submitRating,
        modifier = modifier,
    )
}

@Composable
fun ShowScreen(
    state: ShowUiState,
    onRate: (Int) -> Unit,
    onSubmitRating: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .verticalScroll(rememberScrollState()),
    ) {
        ShowHeader(state.show)

        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.show_rate_title),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    StarRating(
                        rating = state.rating,
                        onRate = onRate,
                    )

                    PrimaryButton(
                        text = stringResource(R.string.show_rate_submit),
                        onClick = onSubmitRating,
                        enabled = state.show != null &&
                                state.rating != null &&
                                !state.sending,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.show_playlist_title),
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(Modifier.height(16.dp))

            if (state.playlist.isEmpty()) {
                Text(
                    text = stringResource(R.string.show_no_tracks),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                state.playlist.forEach { played ->
                    PlaylistItem(played)

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowHeader(show: Show?) {
    HeroContainer {
        LiveTag(
            text = stringResource(R.string.show_current_tag),
        )

        Spacer(Modifier.height(18.dp))

        HeroTitle(
            title = show?.title
                ?: stringResource(R.string.show_no_show),
            showLine = true,
        )

        if (show != null) {
            Spacer(Modifier.height(14.dp))

            Text(
                text = "${formatTime(show.startsAt.toEpochMilliseconds())} – " +
                        "${formatTime(show.endsAt.toEpochMilliseconds())} Uhr",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PlaylistItem(played: PlayedTrack) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoverImage(played.track.coverUrl, modifier = Modifier.size(48.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = played.track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = played.track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = formatTime(
                played.startedAt.toEpochMilliseconds(),
            ) + " Uhr",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    return SimpleDateFormat("HH:mm", Locale.GERMANY)
        .format(Date(milliseconds))
}
