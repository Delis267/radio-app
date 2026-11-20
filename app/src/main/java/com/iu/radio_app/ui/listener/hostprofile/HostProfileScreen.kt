package com.iu.radio_app.ui.listener.hostprofile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iu.radio_app.R
import com.iu.radio_app.ui.components.HeroContainer
import com.iu.radio_app.ui.components.HeroTitle
import com.iu.radio_app.ui.components.PrimaryButton
import com.iu.radio_app.ui.components.StarRating

// Platzhalter, bis Moderator:innen aus der Datenschicht kommen
private const val HOST_FIRST_NAME = "Elise"
private const val HOST_NAME = "Elise\nMüller"
private const val HOST_BIO =
    "Elise ist schon wach, wenn du noch den Snooze-Knopf suchst. Sie bringt dir Wetter, Stau und " +
        "Ohrwürmer. Gut gelaunt wie sonst niemand vor 7 Uhr. Kaffee hilft, aber Elise hilft mehr."

/** Prototyp des Bildschirms „Moderation“, die Elemente haben noch keine Funktion. */
@Composable
fun HostProfileRoute(modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        LiveHeader()
        AboutSection()
        FeedbackSection()
    }
}

@Composable
private fun LiveHeader() {
    HeroContainer {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.host_live),
                    color = MaterialTheme.colorScheme.inversePrimary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp),
                )
                Spacer(Modifier.height(12.dp))
                HeroTitle(HOST_NAME, showLine = true)
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.host_title), style = MaterialTheme.typography.bodyMedium)
            }
            Image(
                painter = painterResource(R.drawable.placeholder_host_photo),
                contentDescription = stringResource(R.string.host_photo_description, HOST_FIRST_NAME),
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
private fun AboutSection() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 29.dp, bottom = 34.dp)) {
        Text(stringResource(R.string.host_about, HOST_FIRST_NAME), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(HOST_BIO, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
    }
}

@Composable
private fun FeedbackSection() {
    var opinion by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val resources = LocalResources.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 20.dp, vertical = 25.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Text(stringResource(R.string.host_feedback_title, HOST_FIRST_NAME), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.host_feedback_subtitle), style = MaterialTheme.typography.bodyMedium)
        StarRating(
            rating = rating,
            onRate = { stars ->
                rating = stars
                // Prototyp: Toast statt Speichern
                val message = resources.getQuantityString(R.plurals.rating_submitted, stars, stars)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
        )
        TextField(
            value = opinion,
            onValueChange = { opinion = it },
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
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
