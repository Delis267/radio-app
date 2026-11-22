package com.iu.radio_app.ui.listener.songrequest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iu.radio_app.R
import com.iu.radio_app.model.SongRequest
import com.iu.radio_app.model.Track
import com.iu.radio_app.ui.components.PrimaryButton
import com.iu.radio_app.ui.components.ScreenWithBottomSection
import com.iu.radio_app.ui.theme.RadioappTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Instant

/** Verbindet den Bildschirm „Songwunsch“ mit dem ViewModel und zeigt dessen Rückmeldungen an. */
@Composable
fun SongRequestRoute(
    modifier: Modifier = Modifier,
    viewModel: SongRequestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Prototyp: Toast als Rückmeldung, danach dem ViewModel melden, dass sie angezeigt wurde
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }
    // Ab Android 13 braucht die Benachrichtigung eine Berechtigung; abgelehnt bleibt der Schalter aus
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.onNotifyChange(true)
        }
    val onNotifyChange: (Boolean) -> Unit = { enabled ->
        val needsPermission =
            enabled &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else viewModel.onNotifyChange(enabled)
    }
    SongRequestScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onClearSearch = viewModel::onClearSearch,
        onRequest = viewModel::onRequest,
        onToggleSuggestion = viewModel::onToggleSuggestion,
        onSuggestionChange = viewModel::onSuggestionChange,
        onSubmitSuggestion = viewModel::submitSuggestion,
        onNotifyChange = onNotifyChange,
        modifier = modifier,
    )
}

/** Zustandslos: zeigt nur [state] an und meldet Eingaben nach oben. */
@Composable
fun SongRequestScreen(
    state: SongRequestUiState,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRequest: (Track) -> Unit,
    onToggleSuggestion: () -> Unit,
    onSuggestionChange: (String) -> Unit,
    onSubmitSuggestion: () -> Unit,
    onNotifyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithBottomSection(
        bottom = {
            if (state.isSearching) {
                SuggestionSection(state, onToggleSuggestion, onSuggestionChange, onSubmitSuggestion)
            } else {
                NotifySection(state.notifyWhenPlayed, onNotifyChange)
            }
        },
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)) {
            Text(stringResource(R.string.song_request_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            SearchField(state.query, state.isSearching, onQueryChange, onClearSearch)
            Spacer(Modifier.height(16.dp))
            if (state.isSearching) {
                SearchResults(state.searchResults, onRequest)
            } else {
                OpenRequests(state.openRequests)
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.song_request_search_placeholder)) },
        singleLine = true,
        leadingIcon =
            if (isSearching) {
                {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onClearSearch()
                    }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.song_request_search_back))
                    }
                }
            } else {
                null
            },
        trailingIcon = {
            if (query.isEmpty()) {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null)
            } else {
                IconButton(onClick = onClearSearch) {
                    Icon(painterResource(R.drawable.ic_close), stringResource(R.string.song_request_search_clear))
                }
            }
        },
        // Suche läuft schon beim Tippen, Enter schließt nur die Tastatur
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun OpenRequests(requests: List<SongRequest>) {
    SectionLabel(stringResource(R.string.song_request_open_requests))
    if (requests.isEmpty()) {
        Text(
            stringResource(R.string.song_request_no_open_requests),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        return
    }
    TrackList(requests, itemKey = { it.id }) { request ->
        TrackInfo(request.track)
        Column(horizontalAlignment = Alignment.End) {
            Text(formatDate(request.requestedAt), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.song_request_time, formatTime(request.requestedAt)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** [results] ist `null`, solange die erste Suche läuft, dann steht noch nichts da. */
@Composable
private fun SearchResults(
    results: List<Track>?,
    onRequest: (Track) -> Unit,
) {
    if (results == null) return
    SectionLabel(pluralStringResource(R.plurals.song_request_hits, results.size, results.size))
    TrackList(results, itemKey = { it.id }, onClick = onRequest) { track ->
        TrackInfo(track)
        Icon(
            painterResource(R.drawable.ic_arrow_forward),
            contentDescription = stringResource(R.string.song_request_request_track, track.title, track.artist),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

/** Zeilen mit Trennlinie dazwischen; mit [onClick] ist die ganze Zeile antippbar. */
@Composable
private fun <T> TrackList(
    items: List<T>,
    itemKey: (T) -> String,
    onClick: ((T) -> Unit)? = null,
    row: @Composable RowScope.(T) -> Unit,
) {
    // Liste ist kurz und steht in einem scrollenden Screen, daher Column statt LazyColumn
    items.forEachIndexed { index, item ->
        key(itemKey(item)) {
            if (index > 0) HorizontalDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(if (onClick != null) Modifier.clickable { onClick(item) } else Modifier)
                        .padding(vertical = 12.dp),
            ) { row(item) }
        }
    }
}

@Composable
private fun RowScope.TrackInfo(track: Track) {
    // Platzhalter, bis Cover über coverUrl geladen werden
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(54.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Icon(painterResource(R.drawable.ic_list_music), contentDescription = null)
    }
    Column(modifier = Modifier.weight(1f)) {
        Text(track.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(track.artist, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NotifySection(
    enabled: Boolean,
    onNotifyChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.song_request_notify_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.song_request_notify_subtitle), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = enabled, onCheckedChange = onNotifyChange)
    }
}

@Composable
private fun SuggestionSection(
    state: SongRequestUiState,
    onToggleSuggestion: () -> Unit,
    onSuggestionChange: (String) -> Unit,
    onSubmitSuggestion: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleSuggestion),
    ) {
        Text(
            stringResource(R.string.song_request_suggestion_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painterResource(R.drawable.ic_expand_more),
            contentDescription =
                stringResource(
                    if (state.suggestionExpanded) R.string.song_request_suggestion_collapse else R.string.song_request_suggestion_expand,
                ),
            modifier = Modifier.rotate(if (state.suggestionExpanded) 180f else 0f),
        )
    }
    if (!state.suggestionExpanded) return
    Text(stringResource(R.string.song_request_suggestion_text), style = MaterialTheme.typography.bodyMedium)
    TextField(
        value = state.suggestion,
        onValueChange = onSuggestionChange,
        placeholder = { Text(stringResource(R.string.song_request_suggestion_placeholder)) },
        singleLine = true,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.fillMaxWidth(),
    )
    PrimaryButton(
        text = stringResource(R.string.song_request_suggestion_submit),
        onClick = onSubmitSuggestion,
        enabled = state.suggestion.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    )
}

// SimpleDateFormat statt java.time, das gibt es erst ab API 26 (minSdk ist 25)
private fun formatDate(instant: Instant) = format("dd.MM.yyyy", instant)

private fun formatTime(instant: Instant) = format("HH:mm", instant)

private fun format(
    pattern: String,
    instant: Instant,
) = SimpleDateFormat(pattern, Locale.GERMANY).format(Date(instant.toEpochMilliseconds()))

private val previewTracks =
    listOf(
        Track("1", "Erster Titel", "John Doe", null, null, null),
        Track("2", "Zweiter Titel", "John Doe", null, null, null),
    )

@Composable
private fun Preview(state: SongRequestUiState) {
    RadioappTheme {
        SongRequestScreen(state, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun StartPreview() {
    val requests =
        previewTracks.mapIndexed { i, track ->
            SongRequest("$i", track, Instant.parse("2026-10-01T09:0${i}:00Z"), SongRequest.Status.OPEN)
        }
    Preview(SongRequestUiState(openRequests = requests, notifyWhenPlayed = true))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SearchPreview() {
    Preview(SongRequestUiState(query = "John Doe", searchResults = previewTracks, suggestionExpanded = true))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun EmptyPreview() {
    Preview(SongRequestUiState(query = "xyz", searchResults = emptyList(), suggestionExpanded = true))
}
