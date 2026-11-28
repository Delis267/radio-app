package com.iu.radio_app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.iu.radio_app.R
import com.iu.radio_app.ui.theme.RadioappTheme

/**
 * Cover eines Titels aus `Track.coverUrl`, die Größe legt [modifier] fest.
 * Während des Ladens bleibt die Fläche leer, ohne Cover oder bei einem Fehler zeigt sie das Musik-Icon.
 */
@Composable
fun CoverImage(
    coverUrl: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
) {
    val painter = rememberAsyncImagePainter(coverUrl)
    val state by painter.state.collectAsStateWithLifecycle()
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        // Immer zeichnen: Coil lädt erst, wenn das Bild gezeichnet wird
        Image(painter, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        if (state is AsyncImagePainter.State.Error) {
            Icon(
                painter = painterResource(R.drawable.ic_list_music),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoverImageWithoutCoverPreview() {
    RadioappTheme { CoverImage(coverUrl = null, modifier = Modifier.size(54.dp)) }
}
