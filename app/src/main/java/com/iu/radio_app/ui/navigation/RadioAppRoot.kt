package com.iu.radio_app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iu.radio_app.ui.host.HostRoute
import com.iu.radio_app.ui.login.LoginRoute
import com.iu.radio_app.ui.nowplaying.NowPlayingRoute
import com.iu.radio_app.ui.show.ShowRoute
import com.iu.radio_app.ui.songrequest.SongRequestRoute

/** Einstieg der UI: Login, solange niemand angemeldet ist, danach die App mit Navigationsleiste. */
@Composable
fun RadioAppRoot(viewModel: SessionViewModel = hiltViewModel()) {
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    when (isSignedIn) {
        true -> MainNavigation()
        false -> Scaffold(modifier = Modifier.fillMaxSize()) { padding -> LoginRoute(Modifier.padding(padding)) }
        null -> Unit
    }
}

@Composable
private fun MainNavigation() {
    val backStack = rememberNavBackStack(Destination.NowPlaying)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomBar(backStack) },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(padding),
            // Gibt jedem Ziel einen eigenen Zustand und eigene ViewModels
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider =
                entryProvider {
                    entry<Destination.NowPlaying> { NowPlayingRoute() }
                    entry<Destination.Show> { ShowRoute() }
                    entry<Destination.Host> { HostRoute() }
                    entry<Destination.SongRequest> { SongRequestRoute() }
                },
        )
    }
}

@Composable
private fun BottomBar(backStack: NavBackStack<NavKey>) {
    NavigationBar {
        TopLevelDestination.entries.forEach { tab ->
            NavigationBarItem(
                selected = backStack.firstOrNull() == tab.destination,
                // Ein Tab beginnt immer mit seinem Startbildschirm
                onClick = {
                    backStack.clear()
                    backStack.add(tab.destination)
                },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
            )
        }
    }
}
