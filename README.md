# HabitTracker

Aplikacja na Androida do budowania i śledzenia codziennych nawyków. Tworzysz listę
nawyków, odznaczasz ich wykonanie w danym dniu i sprawdzasz postęp oraz statystyki.

Projekt napisany w całości w **Kotlinie** z **Jetpack Compose**. Jest to przepisanie
wcześniejszej wersji z Java + XML na w pełni deklaratywny, jednoaktywnościowy interfejs
z nowoczesnym stackiem Androida (Room-KTX, Coroutines/Flow, ViewModel + StateFlow,
Navigation Compose).

## Zrzuty ekranu

| Lista nawyków | Dodawanie nawyku | Szczegóły i statystyki |
|---|---|---|
| Dashboard z paskiem postępu dnia i kartami nawyków | Formularz z walidacją, wyborem ikony, koloru i celu | Statystyki wykonań oraz historia |

## Stack technologiczny

| Obszar | Technologia |
|---|---|
| Język | Kotlin 2.0.21 |
| UI | Jetpack Compose, Material 3 (Compose BOM 2024.12.01) |
| Typografia | Inter Tight (variable font, Google Fonts, OFL) |
| Ikony | Phosphor Icons (SVG skonwertowane na vector drawables, MIT) |
| Nawigacja | Navigation Compose |
| Architektura | MVVM: UI -> ViewModel -> Repository -> DAO -> Room |
| Baza danych | Room 2.6.1 (kompilator przez KSP), dwie powiązane encje |
| Współbieżność | Kotlin Coroutines + Flow / StateFlow |
| Build | Gradle 8.13 (Kotlin DSL), AGP 8.7.3, version catalog |
| minSdk / targetSdk / compileSdk | 26 / 34 / 34 |

## Funkcje

- Lista nawyków z podsumowaniem postępu na dziś (ile wykonanych z ilu).
- Dodawanie i edycja nawyku: nazwa, opis, kategoria, ikona, kolor, cel dzienny, przypomnienie.
- Odznaczanie wykonania nawyku w danym dniu (z automatycznym przeliczeniem postępu).
- Ekran szczegółów z podstawowymi statystykami i historią wykonań.
- Trwałe przechowywanie danych lokalnie w bazie Room.
- Obsługa trybu jasnego i ciemnego (Material 3).

## Architektura

Aplikacja trzyma się wzorca MVVM z jednokierunkowym przepływem danych. Warstwa UI
obserwuje `StateFlow` z ViewModelu i nigdy nie sięga bezpośrednio do bazy. ViewModel
nie przechowuje referencji do widoków ani `Activity`.

```
com.habittracker
├── data
│   ├── Habit.kt            # @Entity("habits")
│   ├── HabitLog.kt         # @Entity("habit_logs"), @ForeignKey CASCADE -> Habit
│   ├── HabitDao.kt         # odczyt: Flow<List<...>>, zapis: suspend
│   ├── HabitDatabase.kt    # RoomDatabase, singleton
│   └── HabitRepository.kt  # pojedyncze zrodlo danych (coroutines)
├── viewmodel
│   └── HabitViewModel.kt   # AndroidViewModel, StateFlow, combine() strumieni
├── ui
│   ├── HabitListScreen.kt      # ekran 1: lista + postep dnia
│   ├── AddEditHabitScreen.kt   # ekran 2: dodawanie/edycja z walidacja
│   ├── HabitDetailScreen.kt    # ekran 3: szczegoly + statystyki
│   └── theme/Theme.kt
├── util
│   ├── HabitIcons.kt       # mapowanie nazw ikon/kolorow na typy Compose
│   └── DateUtils.kt        # formatowanie dat (yyyy-MM-dd)
└── MainActivity.kt         # NavHost (Navigation Compose)
```

### Model danych

Dwie encje powiązane relacją jeden-do-wielu:

- `Habit` (1) - definicja nawyku (nazwa, kategoria, ikona, kolor, cel, ...).
- `HabitLog` (N) - pojedynczy wpis wykonania nawyku w danym dniu.

`HabitLog` posiada klucz obcy do `Habit` z regułą `onDelete = CASCADE`, więc usunięcie
nawyku automatycznie usuwa powiązane wpisy wykonań.

### Przepływ reaktywny

`HabitViewModel` łączy dwa strumienie z bazy (lista nawyków oraz identyfikatory nawyków
wykonanych dziś) przez operator `combine`, budując gotowy stan ekranu jako `StateFlow`.
UI konsumuje go przez `collectAsStateWithLifecycle`, dzięki czemu odświeża się
automatycznie po każdej zmianie w bazie.

## Gesty

Trzy niestandardowe gesty (gesty na przyciskach się nie liczą):

- **Swipe-to-delete** - przesunięcie kafelka nawyku w bok usuwa go z listy (`SwipeToDismissBox`).
- **Long-press** - przytrzymanie kafelka otwiera ekran edycji nawyku.
- **Double-tap** - dwukrotne tapnięcie kafelka odznacza/zaznacza wykonanie na dziś.

Pojedynczy tap otwiera ekran szczegółów. Wszystkie gesty na kafelku obsługuje
`detectTapGestures` w połączeniu z `SwipeToDismissBox` dla przesunięcia.

## Kontrolki UI

Formularz i lista używają ponad pięciu różnych komponentów Compose: `OutlinedTextField`
(nazwa i opis), `FilterChip` (kategoria), `Slider` (cel dzienny), `Switch` (przypomnienie),
`Button` (zapis), `LinearProgressIndicator` (postęp dnia) oraz `FloatingActionButton`
(dodawanie).

## Walidacja

Formularz dodawania/edycji blokuje zapis, gdy nazwa jest pusta lub krótsza niż dwa znaki.
Błąd sygnalizowany jest bezpośrednio w polu (`isError` + `supportingText`), np.
komunikatem "Nazwa nie moze byc pusta".

## Budowanie i uruchamianie

Wymagania: JDK 17 oraz Android SDK z platformą android-34.

```bash
# zbudowanie APK debug
./gradlew assembleDebug
# wynik: app/build/outputs/apk/debug/app-debug.apk

# instalacja na podłączonym urządzeniu lub emulatorze
./gradlew installDebug
```

Jeśli nie masz emulatora, możesz go utworzyć przez `sdkmanager` i `avdmanager`
(obraz systemowy dla swojej architektury, np. `system-images;android-34;google_apis;arm64-v8a`
na Apple Silicon), a następnie uruchomić poleceniem `emulator -avd <nazwa>`.

## Weryfikacja

Projekt buduje się bez błędów i ostrzeżeń (`BUILD SUCCESSFUL`). Aplikacja została
uruchomiona na emulatorze (Android 14, API 34) i sprawdzona ręcznie:

- renderowanie wszystkich trzech ekranów i nawigacji między nimi,
- walidacja pustej nazwy w formularzu,
- dodanie nawyku i zapis do bazy Room z reaktywnym odświeżeniem listy,
- gest long-press otwierający edycję z załadowanymi danymi.

## Struktura projektu w repozytorium

Projekt jest samodzielnym modułem aplikacji Android (`:app`). Plik `local.properties`
(ze ścieżką do SDK) jest celowo pominięty w repozytorium i generowany lokalnie.
