package com.iu.radio_app.model

import kotlin.time.Instant

data class Show(
    val id: String,
    val title: String,
    val host: Host,
    val startsAt: Instant,
    val endsAt: Instant,
)

data class Host(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String?,
)
