# Entscheidung
Jeder Bildschirm besteht aus `<Feature>Route`, `<Feature>Screen`, `<Feature>ViewModel` und `<Feature>UiState` (ADR 004).

- Die `Route` holt das ViewModel mit `hiltViewModel()`, sammelt den Zustand mit `collectAsStateWithLifecycle()`
  und gibt ihn mit den ViewModel-Funktionen als Lambdas an den `Screen` weiter. Nur die Navigation ruft die Route auf.
- Der `Screen` ist zustandslos. Er bekommt `UiState` und Lambdas und kennt weder ViewModel noch Hilt.
  Previews und Composable-Tests nutzen nur den `Screen`.
- Das ViewModel bietet einen `StateFlow<UiState>` (ADR 005) und je Eingabe eine Funktion, z. B. `onRate` oder `submit`.
- Einmalige Rückmeldungen wie „Bewertung gespeichert“ stehen als Feld im UiState.
  Die Route zeigt sie an und meldet danach `onMessageShown()`, das ViewModel setzt das Feld zurück.
- Texte im UiState sind String-Ressourcen (`@StringRes Int`), keine fertigen Strings.
- Wie Rückmeldungen aussehen, ist noch offen. Bis dahin zeigt die Route einen Toast.

# Begründung
Den Screen können wir ohne ViewModel, Hilt und Repository in Previews und Tests mit jedem beliebigen Zustand zeigen.
Der Zustand fließt nur in eine Richtung: vom ViewModel zum Screen, Eingaben gehen als Lambda zurück.
Steht eine Meldung im Zustand, geht sie nicht verloren, wenn der Bildschirm gerade nicht sammelt, z. B. beim Drehen.
Das ViewModel braucht dafür keinen `Context`. Google empfiehlt dieses Vorgehen („UI events as state“).
Mit String-Ressourcen bleibt das ViewModel ohne `Context`, und Tests vergleichen nur die Ressourcen-ID.

# Alternativen
- ViewModel direkt im Screen, eine Funktion weniger, aber Previews und Tests brauchen ein ViewModel samt Abhängigkeiten.
- Ereignisse über `Channel` oder `SharedFlow`, kein Zurücksetzen nötig, aber Ereignisse gehen verloren, wenn gerade niemand sammelt.
- Das ViewModel zeigt den Toast selbst, braucht dafür einen `Context` und ist schwer testbar.
