package com.iu.radio_app.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iu.radio_app.R
import com.iu.radio_app.ui.listener.hostprofile.HostProfileRoute
import com.iu.radio_app.ui.listener.nowplaying.NowPlayingRoute
import com.iu.radio_app.ui.listener.show.ShowRoute
import com.iu.radio_app.ui.listener.songrequest.SongRequestRoute
import com.iu.radio_app.ui.login.LoginRoute

/** Einstieg der UI: Login, solange niemand angemeldet ist, danach die App mit Navigationsleiste. */
@Composable
fun RadioAppRoot(viewModel: SessionViewModel = hiltViewModel()) {
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    // isSignedIn ausgeschaltet fürs testen.
    when (true) {
        true -> {
            System.err.println("Achtung! Login ist manuell deaktiviert.")
            MainNavigation()
        }
        false -> Scaffold(modifier = Modifier.fillMaxSize()) { padding -> LoginRoute(Modifier.padding(padding)) }
        null -> Unit
    }
}

@Composable
private fun MainNavigation() {
    val backStack = rememberNavBackStack(Destination.NowPlaying)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar() },
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
                    entry<Destination.HostProfile> { HostProfileRoute() }
                    entry<Destination.SongRequest> { SongRequestRoute() }
                },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = { Image(painterResource(R.drawable.logo_funk), contentDescription = stringResource(R.string.logo_description)) },
        actions = {
            // Prototyp: Abmelden hat noch keine Funktion
            TextButton(
                onClick = {},
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Text(stringResource(R.string.sign_out))
                Spacer(Modifier.width(8.dp))
                Icon(painterResource(R.drawable.ic_logout), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        },
    )
}

@Composable
private fun BottomBar(backStack: NavBackStack<NavKey>) {
    val colors = MaterialTheme.colorScheme
    // Aktiver Tab wie in Figma: orange und halbfett statt Pille
    val itemColors =
        NavigationBarItemDefaults.colors(
            selectedIconColor = colors.primary,
            selectedTextColor = colors.primary,
            indicatorColor = Color.Transparent,
        )
    Column {
        HorizontalDivider()
        NavigationBar(containerColor = colors.surfaceContainerLowest) {
            TopLevelDestination.entries.forEach { tab ->
                val selected = backStack.firstOrNull() == tab.destination
                NavigationBarItem(
                    selected = selected,
                    // Ein Tab beginnt immer mit seinem Startbildschirm
                    onClick = {
                        backStack.clear()
                        backStack.add(tab.destination)
                    },
                    icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                    label = {
                        Text(
                            stringResource(tab.label),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    },
                    colors = itemColors,
                )
            }
        }
    }
}
