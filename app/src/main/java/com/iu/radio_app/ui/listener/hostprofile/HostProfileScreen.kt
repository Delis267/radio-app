package com.iu.radio_app.ui.listener.hostprofile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iu.radio_app.R
import com.iu.radio_app.model.Host
import com.iu.radio_app.ui.components.HeroContainer
import com.iu.radio_app.ui.components.HeroTitle
import com.iu.radio_app.ui.components.LiveTag
import com.iu.radio_app.ui.components.PrimaryButton
import com.iu.radio_app.ui.components.ScreenWithBottomSection
import com.iu.radio_app.ui.components.StarRating
import com.iu.radio_app.ui.theme.RadioappTheme

/** Verbindet den Bildschirm „Moderation“ mit dem ViewModel und zeigt dessen Rückmeldungen an. */
@Composable
fun HostProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: HostProfileViewModel = hiltViewModel(),
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
    HostProfileScreen(
        state = state,
        onRate = viewModel::onRate,
        onOpinionChange = viewModel::onOpinionChange,
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
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val host = state.host ?: return
    // ponytail: Vorname = erstes Wort des Namens, eigenes Feld im Modell, falls das nicht reicht
    val firstName = host.name.substringBefore(' ')
    ScreenWithBottomSection(
        bottom = { FeedbackSection(firstName, state, onRate, onOpinionChange, onSubmit) },
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
        Row {
            Column(modifier = Modifier.weight(1f)) {
                LiveTag()
                Spacer(Modifier.height(12.dp))
                HeroTitle(host.name, showLine = true)
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.host_title), style = MaterialTheme.typography.bodyMedium)
            }
            // Platzhalterbild, bis Fotos über photoUrl geladen werden
            Image(
                painter = painterResource(R.drawable.placeholder_host_photo),
                contentDescription = stringResource(R.string.host_photo_description, firstName),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .padding(top = 12.dp)
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
    onSubmit: () -> Unit,
) {
    Text(stringResource(R.string.host_feedback_title, firstName), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.host_feedback_subtitle), style = MaterialTheme.typography.bodyMedium)
    StarRating(rating = state.rating, onRate = onRate)
    TextField(
        value = state.opinion,
        onValueChange = onOpinionChange,
        label = { Text(stringResource(R.string.host_feedback_opinion)) },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        modifier = Modifier.fillMaxWidth(),
    )
    PrimaryButton(
        text = stringResource(R.string.host_feedback_submit),
        onClick = onSubmit,
        enabled = state.rating != null,
        modifier = Modifier.fillMaxWidth(),
    )
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
            onSubmit = {},
        )
    }
}
