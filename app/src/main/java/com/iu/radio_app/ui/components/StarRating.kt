package com.iu.radio_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iu.radio_app.R
import com.iu.radio_app.ui.theme.RadioappTheme

@Composable
fun StarRating(
    rating: Int?,
    onRate: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (star in 1..5) {
            val filled = rating != null && star <= rating
            IconButton(
                onClick = { onRate(star) },
                // Screenreader sagt bei gefüllten Sternen „ausgewählt“
                modifier = Modifier.size(48.dp).semantics { selected = filled },
            ) {
                Icon(
                    painterResource(if (filled) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                    contentDescription = pluralStringResource(R.plurals.rating_star, star, star),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingEmptyPreview() {
    RadioappTheme { StarRating(rating = null, onRate = {}) }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingRatedPreview() {
    RadioappTheme { StarRating(rating = 4, onRate = {}) }
}
