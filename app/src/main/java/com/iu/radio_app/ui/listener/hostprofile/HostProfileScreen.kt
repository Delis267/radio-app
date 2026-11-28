package com.iu.radio_app.ui.listener.hostprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.iu.radio_app.R
import com.iu.radio_app.model.Host
import com.iu.radio_app.ui.components.HeroContainer
import com.iu.radio_app.ui.components.HeroTitle
import com.iu.radio_app.ui.components.LiveTag
import com.iu.radio_app.ui.components.PrimaryButton
import com.iu.radio_app.ui.components.ScreenWithBottomSection
import com.iu.radio_app.ui.components.StarRating
import com.iu.radio_app.ui.listener.hostprofile.HostProfileViewModel.Companion.MAX_OPINION_LENGTH
import com.iu.radio_app.ui.theme.RadioappTheme

/** Verbindet den Bildschirm „Moderation“ mit dem ViewModel und zeigt dessen Rückmeldungen als Snackbar an. */
@Composable
fun HostProfileRoute(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HostProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message = state.message?.let { stringResource(it) }
    val retry = stringResource(R.string.host_rating_retry)
    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        // Long statt Indefinite: mit Aktion bliebe sie sonst stehen, bis man „Erneut versuchen“ tippt
        val result = snackbarHostState.showSnackbar(message, actionLabel = retry, duration = SnackbarDuration.Long)
        viewModel.onMessageShown()
        if (result == SnackbarResult.ActionPerformed) viewModel.submit()
    }
    HostProfileScreen(
        state = state,
        onRate = viewModel::onRate,
        onOpinionChange = viewModel::onOpinionChange,
        onOpinionFocusChange = viewModel::onOpinionFocusChange,
        onSubmit = viewModel::submit,
        modifier = modifier,
    )
}

/** Zustandslos: zeigt nur [state] an und meldet Eingaben nach oben. */
@Composable
fun HostProfileScreen(
    state: HostProfileUiState,
    onRate: (Int) -> Unit,
    onOpinionChange: (String) -> Unit,
    onOpinionFocusChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val host = state.host ?: return
    // TODO: Vorname = erstes Wort des Namens, eigenes Feld im Modell, falls das nicht reicht
    val firstName = host.name.substringBefore(' ')
    if (state.submitted) {
        // Nach dem Abschicken ersetzt die Dankeskarte das Formular dauerhaft
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            LiveHeader(host, firstName)
            AboutSection(host, firstName)
            ThanksCard()
        }
        return
    }
    ScreenWithBottomSection(
        bottom = { FeedbackSection(firstName, state, onRate, onOpinionChange, onOpinionFocusChange, onSubmit) },
        modifier = modifier,
    ) {
        LiveHeader(host, firstName)
        AboutSection(host, firstName)
    }
}

@Composable
private fun LiveHeader(
    host: Host,
    firstName: String,
) {
    HeroContainer {
        // IntrinsicSize.Min: Row wird so hoch wie ihr höchstes Kind, damit die linke Spalte diese Höhe kennt
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                LiveTag()
                Spacer(Modifier.height(12.dp))
                // Titel, Strich und Unterzeile mittig im restlichen Platz unter dem Live-Tag
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    HeroTitle(host.name, showLine = true)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.host_title), style = MaterialTheme.typography.bodyMedium)
                }
            }
            // Foto vom Sender. Beim Laden leer, ohne Foto oder bei einem Fehler das Platzhalterbild
            AsyncImage(
                model = host.photoUrl,
                error = painterResource(R.drawable.placeholder_host_photo),
                fallback = painterResource(R.drawable.placeholder_host_photo),
                contentDescription = stringResource(R.string.host_photo_description, firstName),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .size(140.dp)
                        .clip(MaterialTheme.shapes.small),
            )
        }
    }
}

@Composable
private fun AboutSection(
    host: Host,
    firstName: String,
) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 29.dp, bottom = 34.dp)) {
        Text(stringResource(R.string.host_about, firstName), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(host.description, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
    }
}

@Composable
private fun FeedbackSection(
    firstName: String,
    state: HostProfileUiState,
    onRate: (Int) -> Unit,
    onOpinionChange: (String) -> Unit,
    onOpinionFocusChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    Text(stringResource(R.string.host_feedback_title, firstName), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.host_feedback_subtitle), style = MaterialTheme.typography.bodyMedium)
    StarRating(rating = state.rating, onRate = onRate)
    TextField(
        value = state.opinion,
        onValueChange = onOpinionChange,
        label = { Text(stringResource(R.string.host_feedback_opinion)) },
        supportingText =
            if (state.opinion.length >= COUNTER_FROM) {
                {
                    Text(stringResource(R.string.host_feedback_counter, state.opinion.length, MAX_OPINION_LENGTH))
                }
            } else {
                null
            },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.fillMaxWidth().onFocusChanged { onOpinionFocusChange(it.isFocused) },
    )
    PrimaryButton(
        text = stringResource(R.string.host_feedback_submit),
        onClick = onSubmit,
        enabled = state.rating != null && !state.sending,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Ab dieser Länge zeigt das Kommentarfeld einen Zähler. */
private const val COUNTER_FROM = 700

@Composable
private fun ThanksCard() {
    Column(
        modifier =
            Modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                .padding(20.dp),
    ) {
        val color = MaterialTheme.colorScheme.onPrimary
        Text(stringResource(R.string.host_thanks_title), color = color, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.host_thanks_text), color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HostProfileScreenPreview() {
    RadioappTheme {
        HostProfileScreen(
            state =
                HostProfileUiState(
                    host = Host("elise", "Elise Müller", "Kurzer Text über Elise.", photoUrl = null),
                    rating = 4,
                ),
            onRate = {},
            onOpinionChange = {},
            onOpinionFocusChange = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HostProfileScreenSubmittedPreview() {
    RadioappTheme {
        HostProfileScreen(
            state =
                HostProfileUiState(
                    host = Host("elise", "Elise Müller", "Kurzer Text über Elise.", photoUrl = null),
                    submitted = true,
                ),
            onRate = {},
            onOpinionChange = {},
            onOpinionFocusChange = {},
            onSubmit = {},
        )
    }
}
