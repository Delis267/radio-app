package com.iu.radio_app.ui.listener.show

import androidx.annotation.StringRes
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show

data class ShowUiState(
    val show: Show? = null,
    val playlist: List<PlayedTrack> = emptyList(),
    val rating: Int? = null,
    val sending: Boolean = false,
    @StringRes val message: Int? = null,
)