# HabitTracker

Aplikacja na Androida do budowania i śledzenia codziennych nawyków. Tworzysz listę
nawyków, odznaczasz ich wykonanie w danym dniu i sprawdzasz postęp oraz statystyki.

Projekt napisany w całości w **Kotlinie** z **Jetpack Compose**. Jest to przepisanie
wcześniejszej wersji z Java + XML na w pełni deklaratywny, jednoaktywnościowy interfejs
z nowoczesnym stackiem Androida (Room-KTX, Coroutines/Flow, ViewModel + StateFlow,
Navigation Compose).

## Demo online

Aplikację można uruchomić bezpośrednio w przeglądarce (emulator Android, bez instalacji):

[https://appetize.io/app/ikba7racxf6ifwg47lxtly57oi](https://appetize.io/app/ikba7racxf6ifwg47lxtly57oi)

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
| Nawigacja | Navigation Compose 2.8.5 |
| Architektura | MVVM: UI, ViewModel, Repository, DAO, Room |
| Baza danych | Room 2.6.1 (kompilator przez KSP), dwie powiązane encje, wersja 2 z migracją |
| Przypomnienia | AlarmManager + NotificationCompat, odtwarzanie alarmów po restarcie |
| Współbieżność | Kotlin Coroutines 1.9.0 + Flow / StateFlow |
| Build | Gradle 8.13 (Kotlin DSL), AGP 8.7.3, version catalog |
| minSdk / targetSdk / compileSdk | 26 / 34 / 34 |

## Funkcje

- Lista nawyków z podsumowaniem postępu na dziś (ile wykonanych z ilu).
- Dodawanie i edycja nawyku: nazwa, opis, kategoria, ikona, kolor, cel dzienny.
- Odznaczanie wykonania nawyku w danym dniu, z automatycznym przeliczeniem postępu.
- Codzienne przypomnienia o wybranej godzinie, z powiadomieniem systemowym.
- Osobny ekran zarządzania nawykami z usuwaniem.
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
│   ├── Habit.kt            @Entity("habits")
│   ├── HabitLog.kt         @Entity("habit_logs"), @ForeignKey CASCADE do Habit
│   ├── HabitDao.kt         odczyt: Flow<List<...>>, zapis: suspend
│   ├── HabitDatabase.kt    RoomDatabase, singleton, MIGRATION_1_2
│   └── HabitRepository.kt  pojedyncze źródło danych (coroutines)
├── viewmodel
│   └── HabitViewModel.kt   AndroidViewModel, StateFlow, combine() strumieni
├── ui
│   ├── HabitListScreen.kt      lista, postęp dnia, gesty
│   ├── AddEditHabitScreen.kt   dodawanie i edycja z walidacją
│   ├── HabitDetailScreen.kt    szczegóły i statystyki
│   ├── ManageHabitsScreen.kt   zarządzanie nawykami, usuwanie
│   └── theme/Theme.kt
├── reminder
│   ├── ReminderScheduler.kt    planowanie alarmów (AlarmManager)
│   ├── HabitNotifier.kt        kanał i wystawianie powiadomień
│   ├── ReminderReceiver.kt     odbiór alarmu
│   └── BootReceiver.kt         odtworzenie alarmów po restarcie urządzenia
├── util
│   ├── HabitIcons.kt       mapowanie nazw ikon i kolorów na typy Compose
│   └── DateUtils.kt        formatowanie dat (yyyy-MM-dd)
└── MainActivity.kt         NavHost (Navigation Compose)
```

### Model danych

Dwie encje powiązane relacją jeden do wielu:

- `Habit` to definicja nawyku (nazwa, kategoria, ikona, kolor, cel, przypomnienie).
- `HabitLog` to pojedynczy wpis wykonania nawyku w danym dniu.

`HabitLog` ma klucz obcy do `Habit` z regułą `onDelete = CASCADE`, więc usunięcie
nawyku automatycznie usuwa powiązane wpisy wykonań. Na kolumnie `habit_id` jest indeks.

Wykonanie nawyku reprezentuje **istnienie wiersza** w `habit_logs`, a nie flaga.
Odznaczenie usuwa wiersz zamiast go aktualizować.

Baza jest w wersji 2. Migracja `MIGRATION_1_2` dodaje kolumny `reminder_enabled`
i `reminder_time` przez `ALTER TABLE`, bez kasowania danych użytkownika. Godzina
przypomnienia jest trzymana jako liczba minut od północy (480 to 8:00).

### Przepływ reaktywny

`HabitViewModel` łączy dwa strumienie z bazy (lista nawyków oraz identyfikatory nawyków
wykonanych dziś) operatorem `combine`, budując gotowy stan ekranu jako `StateFlow`.
UI konsumuje go przez `collectAsStateWithLifecycle`, więc odświeża się automatycznie
po każdej zmianie w bazie.

Data „dziś" też jest reaktywna. `todayFlow` emituje ją co minutę i przechodzi przez
`distinctUntilChanged` oraz `flatMapLatest`, dzięki czemu status wykonania resetuje się
po północy nawet wtedy, gdy aplikacja stoi w tle.

Strumień stanu jest udostępniany przez `stateIn(SharingStarted.WhileSubscribed(5_000))`,
więc przeżywa obrót ekranu bez ponownego odpytywania bazy.

## Przypomnienia

Nawyk może mieć włączone codzienne przypomnienie o wybranej godzinie. Po zapisie
`HabitViewModel` woła `syncReminder`, które planuje alarm w `AlarmManager` albo go
anuluje, zależnie od ustawienia. Usunięcie nawyku najpierw anuluje jego alarm.

Alarmy `AlarmManager` nie przeżywają restartu urządzenia, więc `BootReceiver` reaguje
na `ACTION_BOOT_COMPLETED` i odtwarza je dla wszystkich nawyków z włączonym
przypomnieniem. Robi to w `goAsync()` na `Dispatchers.IO`, bo odczyt z bazy nie może
blokować głównego wątku.

Powiadomienia idą kanałem `habit_reminders` o priorytecie `IMPORTANCE_HIGH`.
Na Androidzie 13 i nowszym `MainActivity` prosi o uprawnienie `POST_NOTIFICATIONS`
przez `registerForActivityResult`.

## Gesty

Trzy niestandardowe gesty na kafelku nawyku (gesty na przyciskach się nie liczą):

- **Swipe w prawo** oznacza nawyk jako wykonany na dziś (zielone tło, ikona check).
- **Swipe w lewo** oznacza go jako niewykonany (żółte tło, ikona close).
- **Long-press** otwiera ekran edycji nawyku.
- **Double-tap** przełącza wykonanie na dziś.

Pojedynczy tap otwiera ekran szczegółów. Usuwanie nawyku jest w osobnym ekranie
`ManageHabitsScreen`, pod ikoną kosza przy wierszu.

Przesunięcie obsługuje `SwipeToDismissBox`, którego `confirmValueChange` zawsze zwraca
`false`. Kafelek nie znika po geście, tylko wykonuje akcję i wraca na miejsce.
Tap, double-tap i long-press obsługuje `detectTapGestures`.

## Kontrolki UI

Formularz i lista używają ponad pięciu różnych komponentów Compose: `OutlinedTextField`
(nazwa i opis), `FilterChip` (kategoria), `Slider` (cel dzienny), `Switch` (przypomnienie),
`TimePicker` (godzina przypomnienia), `LinearProgressIndicator` (postęp dnia),
`FloatingActionButton` (dodawanie), `SwipeToDismissBox` (gesty na kafelku)
oraz `AlertDialog` (okno z wyborem godziny przypomnienia).

## Walidacja

Formularz dodawania i edycji blokuje zapis, gdy nazwa jest pusta lub krótsza niż dwa
znaki. Błąd sygnalizowany jest bezpośrednio w polu (`isError` i `supportingText`),
komunikatem „Nazwa nie moze byc pusta" albo „Nazwa musi miec min. 2 znaki".

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
(obraz systemowy dla swojej architektury, na przykład
`system-images;android-34;google_apis;arm64-v8a` na Apple Silicon), a następnie
uruchomić poleceniem `emulator -avd <nazwa>`.

## Weryfikacja

Projekt buduje się bez błędów i ostrzeżeń (`BUILD SUCCESSFUL`). Aplikacja została
uruchomiona na emulatorze (Android 14, API 34) i sprawdzona ręcznie:

- renderowanie wszystkich czterech ekranów i nawigacji między nimi,
- walidacja pustej nazwy w formularzu,
- dodanie nawyku i zapis do bazy Room z reaktywnym odświeżeniem listy,
- gest long-press otwierający edycję z załadowanymi danymi,
- oznaczanie wykonania swipem w obie strony,
- zaplanowanie przypomnienia i powiadomienie o wybranej godzinie.

## Struktura projektu w repozytorium

Projekt jest samodzielnym modułem aplikacji Android (`:app`). Plik `local.properties`
ze ścieżką do SDK jest celowo pominięty w repozytorium i generowany lokalnie.

## Zasoby i licencje

- Font Inter Tight, Google Fonts, licencja SIL Open Font License 1.1.
- Ikony Phosphor Icons, licencja MIT (SVG skonwertowane na Android vector drawables).
