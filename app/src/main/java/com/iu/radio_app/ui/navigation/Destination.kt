package com.iu.radio_app.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.iu.radio_app.R
import kotlinx.serialization.Serializable

/** Alle Ziele der Navigation. Ein neuer Bildschirm bekommt hier einen Eintrag und in [MainNavigation] ein `entry`. */
@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object NowPlaying : Destination

    @Serializable
    data object Show : Destination

    @Serializable
    data object Host : Destination

    @Serializable
    data object SongRequest : Destination
}

/** Die Tabs der unteren Navigationsleiste, in der Reihenfolge der Wireframes. */
enum class TopLevelDestination(
    val destination: Destination,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
) {
    NOW_PLAYING(Destination.NowPlaying, R.string.nav_now_playing, R.drawable.ic_sensors),
    SHOW(Destination.Show, R.string.nav_show, R.drawable.ic_queue_music),
    HOST(Destination.Host, R.string.nav_host, R.drawable.ic_mic),
    SONG_REQUEST(Destination.SongRequest, R.string.nav_song_request, R.drawable.ic_favorite),
}
