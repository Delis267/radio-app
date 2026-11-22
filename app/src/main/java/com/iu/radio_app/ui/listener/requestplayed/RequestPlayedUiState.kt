package com.iu.radio_app.ui.listener.requestplayed

import com.iu.radio_app.model.Track

data class RequestPlayedUiState(
    /** Gewünschter Titel, der gerade läuft; `null`, wenn kein Banner zu sehen ist. */
    val track: Track? = null,
)
